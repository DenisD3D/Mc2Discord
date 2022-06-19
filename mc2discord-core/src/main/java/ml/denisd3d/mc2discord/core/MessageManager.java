package ml.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.WebhookExecuteSpec;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import reactor.core.publisher.Mono;

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

    public void sendMessageOfType(String type, String content, String nonWebhookContent, String username, String avatarUrl, Runnable successConsumer, boolean forceChannelMessage) {
        content = content.replaceAll("\u00A7.", "");

        if (type.isEmpty() || (content.isEmpty() && nonWebhookContent.isEmpty()) || username.isEmpty() || avatarUrl.isEmpty())
            return;
        for (Channels.Channel channel : instance.config.channels.channels) {
            if (channel.channel_id == 0) continue;

            if (channel.subscriptions.contains(type)) {
                String message = !nonWebhookContent.isEmpty() ? nonWebhookContent : content;
                if (channel.mode == Channels.SendMode.WEBHOOK) {
                    if (!forceChannelMessage)
                        this.sendWebhookMessage(channel.channel_id, content, username, avatarUrl, false, successConsumer);
                    else this.sendChannelMessage(channel.channel_id, message, false, successConsumer);
                } else if (channel.mode == Channels.SendMode.PLAIN_TEXT) {
                    this.sendChannelMessage(channel.channel_id, message, false, successConsumer);
                } else if (channel.mode == Channels.SendMode.EMBED) {
                    this.sendEmbedMessage(channel.channel_id, type, content, username, avatarUrl, successConsumer);
                }
            }
        }
    }

    public void sendMessageInChannel(long channelId, String type, String content, Channels.SendMode mode, boolean useCodeblocks, Runnable successConsumer) {
        if (mode == Channels.SendMode.WEBHOOK) {
            this.sendWebhookMessage(channelId, content, this.instance.botDisplayName, this.instance.botAvatar, useCodeblocks, successConsumer);
        } else if (mode == Channels.SendMode.PLAIN_TEXT) {
            this.sendChannelMessage(channelId, content, useCodeblocks, successConsumer);
        } else if (mode == Channels.SendMode.EMBED) {
            this.sendEmbedMessage(channelId, type, content, this.instance.botDisplayName, this.instance.botAvatar, successConsumer);
        }
    }

    private void sendWebhookMessage(long channelId, String content, String username, String avatarUrl, boolean useCodeblocks, Runnable successConsumer) {
        Mono<TextChannel> channelMono = this.instance.client.getChannelById(Snowflake.of(channelId))
                .ofType(TextChannel.class);
        channelMono.flatMapMany(textChannel -> textChannel.getWebhooks())
                .filter(webhook -> webhook.getName()
                        .filter(s -> s.equals("Mc2Discord - " + Mc2Discord.INSTANCE.botName + "#" + Mc2Discord.INSTANCE.botDiscriminator))
                        .isPresent())
                .switchIfEmpty(Mono.defer(() -> channelMono.flatMap(textChannel -> textChannel.createWebhook(webhookCreateSpec -> webhookCreateSpec.setName("Mc2Discord - " + Mc2Discord.INSTANCE.botName + "#" + Mc2Discord.INSTANCE.botDiscriminator)))))
                .next()
                .subscribe(webhook -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks)
                        .forEach(s -> webhook.execute(WebhookExecuteSpec.builder()
                                        .content(s)
                                        .username(username)
                                        .avatarUrl(avatarUrl)
                                        .allowedMentions(AllowedMentions.builder()
                                                .parseType(Mc2Discord.INSTANCE.config.misc.allowed_mention.stream()
                                                        .map(AllowedMentions.Type::valueOf)
                                                        .toArray(AllowedMentions.Type[]::new))
                                                .build())
                                        .build())

                                .doOnError(Mc2Discord.logger::error)
                                .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null)));
    }

    private void sendChannelMessage(long channelId, String content, boolean useCodeblocks, Runnable successConsumer) {
        this.instance.client.getChannelById(Snowflake.of(channelId))
                .ofType(TextChannel.class)
                .subscribe(textChannel -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks)
                        .forEach(s -> textChannel.createMessage(messageCreateSpec -> messageCreateSpec.setContent(s)
                                        .setAllowedMentions(AllowedMentions.builder()
                                                .parseType(Mc2Discord.INSTANCE.config.misc.allowed_mention.stream()
                                                        .map(AllowedMentions.Type::valueOf)
                                                        .toArray(AllowedMentions.Type[]::new))
                                                .build()))
                                .doOnError(Mc2Discord.logger::error)
                                .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null)));
    }

    private void sendEmbedMessage(long channel_id, String type, String message, String username, String avatarUrl, Runnable successConsumer) {
        this.instance.client.getChannelById(Snowflake.of(channel_id))
                .ofType(TextChannel.class)
                .flatMap(textChannel -> textChannel.createMessage(messageCreateSpec -> messageCreateSpec.addEmbed(embed -> {
                    embed.setDescription(message);
                    if ("info".equals(type)) {
                        embed.setColor(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_info));
                    } else if ("chat".equals(type)) {
                        embed.setColor(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_chat));
                    } else if ("command".equals(type)) {
                        embed.setColor(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_command));
                    } else if ("log".equals(type)) {
                        embed.setColor(M2DUtils.getColorFromString(Mc2Discord.INSTANCE.config.style.embed_color_log));
                    } else {
                        embed.setColor(Color.WHITE);
                    }

                    if (!username.equals(this.instance.botDisplayName)) {
                        embed.setAuthor(username, null, avatarUrl);
                    }
                })))
                .doOnError(Mc2Discord.logger::error)
                .subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null);
    }
}