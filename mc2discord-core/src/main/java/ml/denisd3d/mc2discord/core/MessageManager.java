package ml.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookCreateSpec;
import discord4j.core.spec.WebhookExecuteSpec;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import reactor.core.publisher.Mono;

import javax.annotation.Nullable;
import java.util.HashMap;

public class MessageManager {

    Mc2Discord instance;

    public MessageManager(Mc2Discord instance) {
        this.instance = instance;
    }

    public void sendInfoMessage(String content) {
        this.sendMessageOfType("info", content, "", this.instance.botDisplayName, this.instance.botAvatar, null, this.instance.config.style.bot_name.isEmpty() && this.instance.config.style.bot_avatar.isEmpty());
    }

    public void sendChatMessage(String content, String nonWebhookContent, String username, String avatarUrl) {
        this.sendMessageOfType("chat", content, nonWebhookContent, username, avatarUrl, null, false);
    }

    public void sendInfoMessage(String content, @Nullable EmbedCreateSpec embed) {
        this.sendMessageOfType("info", content, "", this.instance.botDisplayName, this.instance.botAvatar, null, this.instance.config.style.bot_name.isEmpty() && this.instance.config.style.bot_avatar.isEmpty(), embed);
    }

    public void sendChatMessage(String content, String nonWebhookContent, String username, String avatarUrl, @Nullable EmbedCreateSpec embed) {
        this.sendMessageOfType("chat", content, nonWebhookContent, username, avatarUrl, null, false, embed);
    }

    public void sendMessageOfType(String type, String content, String nonWebhookContent, String username, String avatarUrl, Runnable successConsumer, boolean forceChannelMessage) {
        this.sendMessageOfType(type, content, nonWebhookContent, username, avatarUrl, successConsumer, forceChannelMessage, null);
    }

    public void sendMessageOfType(String type, String content, String nonWebhookContent, String username, String avatarUrl, Runnable successConsumer, boolean forceChannelMessage, @Nullable EmbedCreateSpec embed) {
        content = content.replaceAll("\u00A7.", "");

        if (type.isEmpty() || (content.isEmpty() && nonWebhookContent.isEmpty() && embed == null) || username.isEmpty() || avatarUrl.isEmpty()) return;
        for (Channels.Channel channel : instance.config.channels.channels) {
            if (channel.channel_id == 0) continue;

            HashMap<Long, String> channelEmojis = Mc2Discord.emojiCache.get(channel.channel_id);
            if (channelEmojis != null) {
                for (Long emojiId : channelEmojis.keySet()) {
                    String emojiName = channelEmojis.get(emojiId);
                    content = content.replaceAll(":" + emojiName + ":", "<:" + emojiName + ":" + emojiId + ">");
                }
            }

            if (channel.subscriptions.contains(type)) {
                String message = !nonWebhookContent.isEmpty() ? nonWebhookContent : content;
                if (channel.mode == Channels.SendMode.WEBHOOK) {
                    if (!forceChannelMessage)
                        this.sendWebhookMessage(channel.channel_id, content, username, avatarUrl, false, successConsumer, embed);
                    else this.sendChannelMessage(channel.channel_id, message, false, successConsumer, embed);
                } else if (channel.mode == Channels.SendMode.PLAIN_TEXT) {
                    this.sendChannelMessage(channel.channel_id, message, false, successConsumer, embed);
                } else if (channel.mode == Channels.SendMode.EMBED) {
                    this.sendEmbedMessage(channel.channel_id, type, content, username, avatarUrl, successConsumer, embed);
                }
            }
        }
    }

    public void sendMessageInChannel(long channelId, String type, String content, Channels.SendMode mode, boolean useCodeblocks, Runnable successConsumer) {
        this.sendMessageInChannel(channelId, type, content, mode, useCodeblocks, successConsumer, null);
    }

    public void sendMessageInChannel(long channelId, String type, String content, Channels.SendMode mode, boolean useCodeblocks, Runnable successConsumer, @Nullable EmbedCreateSpec embed) {
        if (mode == Channels.SendMode.WEBHOOK) {
            this.sendWebhookMessage(channelId, content, this.instance.botDisplayName, this.instance.botAvatar, useCodeblocks, successConsumer, embed);
        } else if (mode == Channels.SendMode.PLAIN_TEXT) {
            this.sendChannelMessage(channelId, content, useCodeblocks, successConsumer, embed);
        } else if (mode == Channels.SendMode.EMBED) {
            this.sendEmbedMessage(channelId, type, content, this.instance.botDisplayName, this.instance.botAvatar, successConsumer, embed);
        }
    }

    private void sendWebhookMessage(long channelId, String content, String username, String avatarUrl, boolean useCodeblocks, Runnable successConsumer, @Nullable EmbedCreateSpec embed) {
        Mono<TopLevelGuildMessageChannel> channelMono = this.instance.client.getChannelById(Snowflake.of(channelId))
                .ofType(TopLevelGuildMessageChannel.class);
        channelMono.flatMapMany(TopLevelGuildMessageChannel::getWebhooks)
                .filter(webhook -> webhook.getName()
                        .filter(s -> s.equals("Mc2Dis Webhook - " + Mc2Discord.INSTANCE.botName + "#" + Mc2Discord.INSTANCE.botDiscriminator))
                        .isPresent())
                .switchIfEmpty(Mono.defer(() -> channelMono.flatMap(textChannel -> textChannel.createWebhook(WebhookCreateSpec.builder()
                        .name("Mc2Dis Webhook - " + Mc2Discord.INSTANCE.botName + "#" + Mc2Discord.INSTANCE.botDiscriminator)
                        .build()))))
                .next()
                .subscribe(webhook -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks)
                        .forEach(s -> {
                            WebhookExecuteSpec.Builder builder = WebhookExecuteSpec.builder()
                                    .username(username)
                                    .avatarUrl(avatarUrl)
                                    .allowedMentions(AllowedMentions.builder()
                                            .parseType(Mc2Discord.INSTANCE.config.misc.allowed_mention.stream()
                                                    .map(AllowedMentions.Type::valueOf)
                                                    .toArray(AllowedMentions.Type[]::new))
                                            .build());
                            if (!s.equals("")) builder.content(s);
                            if (embed != null) builder.addEmbed(embed);
                            webhook.execute(builder.build())

                                    .doOnError(Mc2Discord.logger::error)
                                    .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null);
                        }));
    }

    private void sendChannelMessage(long channelId, String content, boolean useCodeblocks, Runnable successConsumer, @Nullable EmbedCreateSpec embed) {
        this.instance.client.getChannelById(Snowflake.of(channelId))
                .ofType(MessageChannel.class)
                .subscribe(textChannel -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks)
                        .forEach(s -> {
                            MessageCreateSpec.Builder builder = MessageCreateSpec.builder()
                                    .allowedMentions(AllowedMentions.builder()
                                            .parseType(Mc2Discord.INSTANCE.config.misc.allowed_mention.stream()
                                                    .map(AllowedMentions.Type::valueOf)
                                                    .toArray(AllowedMentions.Type[]::new))
                                            .build());
                            if (!s.equals("")) builder.content(s);
                            if (embed != null) builder.addEmbed(embed);
                            textChannel.createMessage(builder.build())
                                    .doOnError(Mc2Discord.logger::error)
                                    .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null);
                        }));
    }

    private void sendEmbedMessage(long channel_id, String type, String message, String username, String avatarUrl, Runnable successConsumer, @Nullable EmbedCreateSpec embed) {
        EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();

        if ("info".equals(type)) {
            builder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_info));
        } else if ("chat".equals(type)) {
            builder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_chat));
        } else if ("command".equals(type)) {
            builder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_command));
        } else if ("log".equals(type)) {
            builder.color(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_log));
        } else {
            builder.color(Color.WHITE);
        }

        if (!username.equals(this.instance.botDisplayName) || Mc2Discord.INSTANCE.config.style.embed_show_server_avatar) {
            builder.author(username, null, avatarUrl);
        }

        this.instance.client.getChannelById(Snowflake.of(channel_id))
                .ofType(MessageChannel.class)
                .subscribe(textChannel -> M2DUtils.breakStringToLines(message, 2000, false)
                        .forEach(s -> {
                            MessageCreateSpec.Builder builder1 = MessageCreateSpec.builder();
                            if (!s.equals("")) builder1.addEmbed(builder.description(s).build());
                            if (embed != null) builder1.addEmbed(embed);
                            textChannel.createMessage(builder1.build())
                                    .doOnError(Mc2Discord.logger::error)
                                    .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null);
                        }));
    }
}