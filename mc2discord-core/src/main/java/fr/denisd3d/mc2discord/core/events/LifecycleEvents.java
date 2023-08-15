package fr.denisd3d.mc2discord.core.events;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildEmoji;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.json.response.ErrorResponse;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import fr.denisd3d.mc2discord.core.*;
import fr.denisd3d.mc2discord.core.config.Channels;
import fr.denisd3d.mc2discord.core.config.StatusChannels;
import fr.denisd3d.mc2discord.core.entities.Entity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class LifecycleEvents {
    public static boolean minecraftReady = false;

    public static void onDiscordReady(ReadyEvent readyEvent) {
        Mc2Discord.INSTANCE.vars.bot_name = readyEvent.getSelf().getUsername();
        Mc2Discord.INSTANCE.vars.bot_discriminator = readyEvent.getSelf().getDiscriminator();
        Mc2Discord.INSTANCE.vars.bot_id = readyEvent.getSelf().getId();

        Mc2Discord.INSTANCE.vars.mc2discord_display_name = Mc2Discord.INSTANCE.config.style.bot_name.isEmpty() ? readyEvent.getSelf()
                .getUsername() : Entity.replace(Mc2Discord.INSTANCE.config.style.bot_name);
        Mc2Discord.INSTANCE.vars.mc2discord_avatar = Mc2Discord.INSTANCE.config.style.bot_avatar.isEmpty() ? readyEvent.getSelf()
                .getAvatarUrl() : Entity.replace(Mc2Discord.INSTANCE.config.style.bot_avatar);
        Mc2Discord.INSTANCE.vars.mc2discord_webhook_name = "Mc2Dis Webhook - " + Mc2Discord.INSTANCE.vars.bot_name + "#" + Mc2Discord.INSTANCE.vars.bot_discriminator;

        for (Channels.Channel channel : Mc2Discord.INSTANCE.config.channels.channels) {
            if (channel.channel_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
                Mc2Discord.LOGGER.warn("Invalid channel id for channel " + channel.channel_id.asString());
                Mc2Discord.INSTANCE.errors.add("Invalid channel id for channel " + channel.channel_id.asString());
                continue;
            }

            Mc2Discord.INSTANCE.client.getChannelById(channel.channel_id)
                    .ofType(GuildChannel.class)
                    .doOnError(ClientException.class, e -> {
                        Map<String, Object> stringObjectMap = e.getErrorResponse().map(ErrorResponse::getFields).orElse(Collections.emptyMap());
                        Mc2Discord.LOGGER.warn(stringObjectMap.getOrDefault("message", "") + " (code " + stringObjectMap.getOrDefault("code", "xxx") + ") for channel " + channel.channel_id.asString());
                        Mc2Discord.INSTANCE.errors.add(stringObjectMap.getOrDefault("message", "") + " (code " + stringObjectMap.getOrDefault("code", "xxx") + ") for channel " + channel.channel_id.asString());
                    })
                    .flatMap(guildChannel -> guildChannel.getEffectivePermissions(Mc2Discord.INSTANCE.vars.bot_id).map(permissions -> Tuples.of(guildChannel, permissions)))
                    .subscribe(tuple -> {
                        GuildChannel guildChannel = tuple.getT1();
                        PermissionSet permissionSet = tuple.getT2();

                        ArrayList<String> missingPermissions = new ArrayList<>();
                        if (!permissionSet.contains(Permission.ADMINISTRATOR)) {
                            if (!permissionSet.contains(Permission.VIEW_CHANNEL)) {
                                missingPermissions.add("VIEW_CHANNEL");
                            }
                            if (!permissionSet.contains(Permission.SEND_MESSAGES)) {
                                missingPermissions.add("SEND_MESSAGES");
                            }
                            if (channel.mode == Channels.Channel.SendMode.WEBHOOK && !permissionSet.contains(Permission.MANAGE_WEBHOOKS)) {
                                missingPermissions.add("MANAGE_WEBHOOKS");
                            }
                            if (guildChannel instanceof ThreadChannel && !permissionSet.contains(Permission.SEND_MESSAGES_IN_THREADS)) {
                                missingPermissions.add("SEND_MESSAGES_IN_THREADS");
                            }
                        }

                        if (!missingPermissions.isEmpty()) {
                            Mc2Discord.LOGGER.warn("Missing permissions for message channel " + channel.channel_id.asString() + ": " + String.join(", ", missingPermissions));
                            Mc2Discord.INSTANCE.errors.add("Missing permissions for message channel " + channel.channel_id.asString() + ": " + String.join(", ", missingPermissions));
                        }
                    });
        }

        if (Mc2Discord.INSTANCE.config.features.status_channels) {
            for (StatusChannels.StatusChannel channel : Mc2Discord.INSTANCE.config.statusChannels.channels) {
                if (channel.channel_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
                    Mc2Discord.LOGGER.warn("Invalid channel id for status channel " + channel.channel_id.asString());
                    Mc2Discord.INSTANCE.errors.add("Invalid channel id for status channel " + channel.channel_id.asString());
                    continue;
                }

                Mc2Discord.INSTANCE.client.getChannelById(channel.channel_id)
                        .ofType(GuildChannel.class)
                        .doOnError(ClientException.class, e -> {
                            Map<String, Object> stringObjectMap = e.getErrorResponse().map(ErrorResponse::getFields).orElse(Collections.emptyMap());
                            Mc2Discord.LOGGER.warn(stringObjectMap.getOrDefault("message", "") + " (code " + stringObjectMap.getOrDefault("code", "xxx") + ") for status channel " + channel.channel_id.asString());
                            Mc2Discord.INSTANCE.errors.add(stringObjectMap.getOrDefault("message", "") + " (code " + stringObjectMap.getOrDefault("code", "xxx") + ") for status channel " + channel.channel_id.asString());
                        })
                        .flatMap(guildChannel -> guildChannel.getEffectivePermissions(Mc2Discord.INSTANCE.vars.bot_id))
                        .subscribe(permissionSet -> {
                            ArrayList<String> missingPermissions = new ArrayList<>();
                            if (!permissionSet.contains(Permission.ADMINISTRATOR)) {
                                if (!permissionSet.contains(Permission.VIEW_CHANNEL)) {
                                    missingPermissions.add("VIEW_CHANNEL");
                                }
                                if (!permissionSet.contains(Permission.MANAGE_CHANNELS)) {
                                    missingPermissions.add("MANAGE_CHANNELS");
                                }
                            }

                            if (!missingPermissions.isEmpty()) {
                                Mc2Discord.LOGGER.error("Missing permissions for status channel " + channel.channel_id.asString() + ": " + String.join(", ", missingPermissions));
                                Mc2Discord.INSTANCE.errors.add("Missing permissions for status channel " + channel.channel_id.asString() + ": " + String.join(", ", missingPermissions));
                            }
                        });
            }
        }

        if (Mc2Discord.INSTANCE.config.features.account_linking) {
            if (Mc2Discord.INSTANCE.config.account.guild_id.equals(M2DUtils.NIL_SNOWFLAKE)) {
                Mc2Discord.LOGGER.error("Invalid guild id for account linking");
                Mc2Discord.INSTANCE.errors.add("Invalid guild id for account linking");
            }
        }

        // Allowed mentions
        Mc2Discord.INSTANCE.vars.allowedMentions = Possible.of(AllowedMentions.builder()
                .parseType(Mc2Discord.INSTANCE.config.misc.allowed_mention.stream().map(AllowedMentions.Type::valueOf).toArray(AllowedMentions.Type[]::new))
                .build());

        // Channels, emojis and members caches
        Flux<Guild> guilds = Flux.fromIterable(Mc2Discord.INSTANCE.config.channels.channels)
                .map(channel -> channel.channel_id)
                .filter(channelId -> !channelId.equals(M2DUtils.NIL_SNOWFLAKE))
                .flatMap(channelId -> Mc2Discord.INSTANCE.client.getChannelById(channelId))
                .ofType(GuildChannel.class)
                .flatMap(GuildChannel::getGuild)
                .distinct();

        Flux<GuildEmoji> guildEmojiFlux = guilds.flatMap(Guild::getEmojis).doOnNext(emoji -> Mc2Discord.INSTANCE.vars.emojiCache.put(emoji.getGuildId(), emoji.getName(), emoji.getId()));
        Flux<GuildChannel> guildChannelFlux = guilds.flatMap(Guild::getChannels).doOnNext(channel -> {
            Mc2Discord.INSTANCE.vars.channelCache.put(channel.getGuildId(), channel.getName(), channel.getId());
            Mc2Discord.INSTANCE.vars.channelCacheReverse.put(channel.getId(), channel.getGuildId());
        });
        Flux<Member> guildMemberFlux = guilds.flatMap(Guild::getMembers).doOnError(throwable -> {
            Mc2Discord.LOGGER.error("Missing GUILD_MEMBERS intent, cannot cache members list");
            Mc2Discord.INSTANCE.errors.add("Missing GUILD_MEMBERS intent, cannot cache members list");
        }).doOnNext(M2DUtils::cacheMember);

        Mono.when(guildEmojiFlux, guildChannelFlux, guildMemberFlux).doOnSuccess(unused -> {
            Mc2Discord.LOGGER.info("Mc2Discord started as " + Mc2Discord.INSTANCE.vars.bot_name + "#" + Mc2Discord.INSTANCE.vars.bot_discriminator);
            String newVersion = Mc2Discord.INSTANCE.minecraft.getNewVersion();
            if (newVersion != null) {
                Mc2Discord.LOGGER.info("New Mc2Discord version available: " + newVersion);
            }
            LifecycleEvents.mcOrDiscordReady();
        }).subscribe();
    }

    public static void mcOrDiscordReady() {
        if (Mc2Discord.INSTANCE.client == null || !minecraftReady) return;
        Mc2Discord.INSTANCE.vars.isStarted = true;

        MessageManager.init();
        StatusManager.init();
        LoggingManager.init();
        AccountManager.init();

        MessageManager.sendInfoMessage("server_start", Entity.replace(Mc2Discord.INSTANCE.config.messages.start.asString(), Collections.emptyList())).subscribe();
    }

    public static void onShutdown() {
        if (M2DUtils.isNotConfigured())
            return;

        MessageManager.sendInfoMessage("server_stop", Entity.replace(Mc2Discord.INSTANCE.config.messages.stop.asString(), Collections.emptyList()))
                .then(Mc2Discord.INSTANCE.shutdown())
                .subscribe();
    }
}
