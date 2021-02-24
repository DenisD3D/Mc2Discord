package ml.denisd3d.minecraft2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.channel.TextChannel;
import ml.denisd3d.minecraft2discord.core.config.M2DConfig;
import reactor.core.publisher.Mono;

public class MessageManager {

    Minecraft2Discord instance;

    public MessageManager(Minecraft2Discord instance) {
        this.instance = instance;
    }

    public void sendInfoMessage(String content) {
        this.sendMessageOfType("info", content, "", this.instance.botDisplayName, this.instance.botAvatar, null);
    }

    public void sendChatMessage(String content, String nonWebhookContent, String username, String avatarUrl) {
        this.sendMessageOfType("chat", content, nonWebhookContent, username, avatarUrl, null);
    }

    public void sendMessageOfType(String type, String content, String nonWebhookContent, String username, String avatarUrl, Runnable successConsumer) {
        if (type.isEmpty() || (content.isEmpty() && nonWebhookContent.isEmpty()) || username.isEmpty() || avatarUrl.isEmpty())
            return;

        for (M2DConfig.Channel channel : instance.config.channels) {
            if (channel.channel_id == 0)
                return;

            if (channel.subscriptions.contains(type)) {
                if (channel.use_webhook) {
                    this.sendWebhookMessage(channel.channel_id, content, username, avatarUrl, false, successConsumer);
                } else {
                    this.sendChannelMessage(channel.channel_id, !nonWebhookContent.isEmpty() ? nonWebhookContent : content, false, successConsumer);
                }
            }
        }
    }

    public void sendMessageInChannel(long channelId, String content, boolean useWebhook, boolean useCodeblocks, Runnable successConsumer) {
        if (useWebhook)
            this.sendWebhookMessage(channelId, content, this.instance.botDisplayName, this.instance.botAvatar, useCodeblocks, successConsumer);
        else
            this.sendChannelMessage(channelId, content, useCodeblocks, successConsumer);
    }

    private void sendWebhookMessage(long channelId, String content, String username, String avatarUrl, boolean useCodeblocks, Runnable successConsumer) {
        Mono<TextChannel> channelMono = this.instance.client.getChannelById(Snowflake.of(channelId)).ofType(TextChannel.class);
        channelMono.flatMapMany(textChannel -> textChannel.getWebhooks())
                .filter(webhook -> webhook.getName().filter(s -> s.equals("Minecraft2Discord")).isPresent())
                .switchIfEmpty(Mono.defer(() -> channelMono.flatMap(textChannel ->
                        textChannel.createWebhook(webhookCreateSpec ->
                                webhookCreateSpec.setName("Minecraft2Discord")))))
                .next()
                .subscribe(webhook -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks).forEach(s -> webhook.execute(webhookExecuteSpec -> webhookExecuteSpec.setContent(s).setUsername(username).setAvatarUrl(avatarUrl)).subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null)));
    }

    private void sendChannelMessage(long channelId, String content, boolean useCodeblocks, Runnable successConsumer) {
        this.instance.client.getChannelById(Snowflake.of(channelId)).ofType(TextChannel.class)
                .subscribe(textChannel -> M2DUtils.breakStringToLines(content, 2000, useCodeblocks).forEach(s -> textChannel.createMessage(messageCreateSpec -> messageCreateSpec.setContent(s)).subscribe(unused -> successConsumer.run(), throwable -> DiscordLogging.logs = "", null)));
    }
}