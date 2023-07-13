package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookExecuteSpec;
import discord4j.discordjson.possible.Possible;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.MessageEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class MessageManager {
    public static Possible<String> default_username = Possible.absent();
    public static Possible<String> default_avatar = Possible.absent();

    public static void init() {
        default_username = Mc2Discord.INSTANCE.config.style.bot_name.isEmpty() ? Possible.absent() : Possible.of(Mc2Discord.INSTANCE.config.style.bot_name);
        default_avatar = Mc2Discord.INSTANCE.config.style.bot_avatar.isEmpty() ? Possible.absent() : Possible.of(Mc2Discord.INSTANCE.config.style.bot_avatar);
    }

    public static Mono<Void> sendInfoMessage(String type, String message) {
        return sendMessage(Arrays.asList(type, "info"), message, default_username, default_avatar);
    }

    public static Mono<Void> sendChatMessage(String message, String username, String avatarUrl) {
        return sendMessage(List.of("chat", "minecraft"), message, Possible.of(username), Possible.of(avatarUrl));
    }

    public static Mono<Void> sendMessage(List<String> types, String message, Possible<String> username, Possible<String> avatarUrl) {
        return sendMessage(types, message, username, avatarUrl, M2DUtils.NIL_SNOWFLAKE, false);
    }

    public static Mono<Void> sendMessage(List<String> types, String message, Possible<String> username, Possible<String> avatarUrl, Snowflake forced_channel, boolean surroundWithCodeBlock) {
        if (M2DUtils.isNotConfigured()) // Check if mod is configured
            return Mono.empty();

        return Flux.fromIterable(Mc2Discord.INSTANCE.config.channels.channels)
                .filter(channel -> !channel.channel_id.equals(M2DUtils.NIL_SNOWFLAKE)) // Remove channels with id 0
                .filter(channel -> !Collections.disjoint(channel.subscriptions, types)) // Remove channels without required subscription
                .filter(channel -> forced_channel.equals(M2DUtils.NIL_SNOWFLAKE) || channel.channel_id.equals(forced_channel)) // Remove channels that are not the forced channel if it is set
                .map(channel -> Tuples.of(channel, M2DUtils.transformToMention(message, channel.channel_id)))
                .flatMap(channelStringTuple2 -> switch (channelStringTuple2.getT1().mode) {
                    case WEBHOOK -> username.isAbsent() || avatarUrl.isAbsent() ?
                            createPlainTextMessage(channelStringTuple2.getT1().channel_id, channelStringTuple2.getT2(), username, surroundWithCodeBlock) :
                            createWebhookMessage(channelStringTuple2.getT1().channel_id, channelStringTuple2.getT2(), username, avatarUrl, surroundWithCodeBlock);
                    case PLAIN_TEXT -> {
                        String formatted_message = Entity.replace(Mc2Discord.INSTANCE.config.style.discord_chat_format, List.of(new MessageEntity(channelStringTuple2.getT2()), new PlayerEntity(username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), new UUID(0L, 0L))));
                        yield createPlainTextMessage(channelStringTuple2.getT1().channel_id, formatted_message, username, surroundWithCodeBlock);
                    }
                    case EMBED ->
                            createEmbedMessage(channelStringTuple2.getT1().channel_id, channelStringTuple2.getT2(), username, avatarUrl, types);
                })
                .then();
    }

    private static Mono<Void> createPlainTextMessage(Snowflake channel, String message, Possible<String> username, boolean surroundWithCodeBlock) {
        return Mc2Discord.INSTANCE.client.getChannelById(channel)
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel -> Flux.fromIterable(M2DUtils.breakStringInMessages(message, 2000, surroundWithCodeBlock))
                        .flatMap(s -> messageChannel.createMessage(MessageCreateSpec.builder().allowedMentions(Mc2Discord.INSTANCE.vars.allowedMentions).content(s).build())))
                .then();
    }

    private static Mono<Void> createWebhookMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, boolean surroundWithCodeBlock) {
        Mono<TopLevelGuildMessageChannel> channelMono = Mc2Discord.INSTANCE.client.getChannelById(channel).ofType(TopLevelGuildMessageChannel.class);
        return channelMono.flatMapMany(TopLevelGuildMessageChannel::getWebhooks)
                .filter(webhook -> webhook.getName().isPresent() && webhook.getName().get().equals(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name))
                .switchIfEmpty(channelMono.flatMap(channel1 -> channel1.createWebhook(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name)))
                .single()
                .flatMapMany(webhook -> Flux.fromIterable(M2DUtils.breakStringInMessages(message, 2000, surroundWithCodeBlock))
                        .flatMap(s -> webhook.execute(WebhookExecuteSpec.builder()
                                .username(username)
                                .avatarUrl(avatarUrl)
                                .allowedMentions(Mc2Discord.INSTANCE.vars.allowedMentions)
                                .content(s)
                                .build())))
                .then();
    }

    private static Mono<Void> createEmbedMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, List<String> types) {
        EmbedCreateSpec.Builder emBuilder = EmbedCreateSpec.builder();

        if (types.contains("info")) {
            emBuilder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_colors.info));
        } else if (types.contains("chat")) {
            emBuilder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_colors.chat));
        } else if (types.contains("log")) {
            emBuilder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_colors.log));
        } else if (types.contains("command")) {
            emBuilder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_colors.command));
        }


        if (!username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name).equals(Mc2Discord.INSTANCE.vars.mc2discord_display_name) || Mc2Discord.INSTANCE.config.style.embed_show_bot_avatar) {
            emBuilder.author(username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), null, avatarUrl.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_avatar));
        }

        return Mc2Discord.INSTANCE.client.getChannelById(channel)
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel ->
                        Flux.fromIterable(M2DUtils.breakStringInMessages(message, 4096, false))
                                .flatMap(s -> messageChannel.createMessage(emBuilder.description(s).build())))
                .then();
    }
}
