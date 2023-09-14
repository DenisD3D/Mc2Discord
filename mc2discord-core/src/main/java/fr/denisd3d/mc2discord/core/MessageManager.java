package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TopLevelGuildMessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateFields;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.spec.WebhookExecuteSpec;
import discord4j.discordjson.possible.Possible;
import fr.denisd3d.mc2discord.core.config.Channels;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.MessageEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

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
        return sendChatMessage(message, Possible.of(username), Possible.of(avatarUrl));
    }

    public static Mono<Void> sendChatMessage(String message, Possible<String> username, Possible<String> avatarUrl) {
        return sendMessage(List.of("chat", "minecraft"), message, username, avatarUrl);
    }

    public static Mono<Void> sendMessage(List<String> types, String message, Possible<String> username, Possible<String> avatarUrl) {
        return sendMessage(types, message, username, avatarUrl, M2DUtils.NIL_SNOWFLAKE, false);
    }

    /**
     * @param types List of types to match
     * @return List of channels matching the types
     */
    @SuppressWarnings("unused")
    public static List<Channels.Channel> getMatchingChannels(List<String> types) {
        return getMatchingChannels(types, M2DUtils.NIL_SNOWFLAKE);
    }

    public static List<Channels.Channel> getMatchingChannels(List<String> types, Snowflake forced_channel) {
        return Mc2Discord.INSTANCE.config.channels.channels.stream()
                .filter(channel -> !channel.channel_id.equals(M2DUtils.NIL_SNOWFLAKE)) // Remove channels with id 0
                .filter(channel -> !Collections.disjoint(channel.subscriptions, types)) // Remove channels without required subscription
                .filter(channel -> forced_channel.equals(M2DUtils.NIL_SNOWFLAKE) || channel.channel_id.equals(forced_channel)) // Remove channels that are not the forced channel if it is set
                .toList();
    }

    public static Mono<Void> sendMessage(List<String> types, String message, Possible<String> username, Possible<String> avatarUrl, Snowflake forced_channel, boolean surroundWithCodeBlock) {
        if (M2DUtils.isNotConfigured()) // Check if mod is configured
            return Mono.empty();
        if (message.isEmpty())
            return Mono.empty();


        return Flux.fromIterable(getMatchingChannels(types, forced_channel))
                .flatMap(channel -> {
                    String message_with_mention = M2DUtils.transformToMention(message, channel.channel_id);
                    return switch (channel.mode) {
                        case WEBHOOK ->
                                createWebhookMessage(channel.channel_id, message_with_mention, username, avatarUrl, surroundWithCodeBlock);
                        case PLAIN_TEXT ->
                                createPlainTextMessage(channel.channel_id, message_with_mention, username, surroundWithCodeBlock);
                        case EMBED ->
                                createEmbedMessage(channel.channel_id, message_with_mention, username, avatarUrl, types);
                    };
                })
                .then();
    }

    /**
     * Send a message using a webhook
     * See {@link #createWebhookMessage(Snowflake, String, Possible, Possible, boolean, Collection, Collection)} for more details
     */
    public static Mono<Void> createWebhookMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, boolean surroundWithCodeBlock) {
        return createWebhookMessage(channel, message, username, avatarUrl, surroundWithCodeBlock, null, null);
    }

    /**
     * Send a message using a webhook
     *
     * @param channel               Discord channel to send the message to
     * @param message               Message to send, may be empty if some others parameters are set
     * @param username              Username to use for the message, Possible.absent() default to the bot name
     * @param avatarUrl             Avatar url to use for the message, Possible.absent() default to the bot avatar
     * @param surroundWithCodeBlock Whether to surround the message with a code block ``` or not
     * @param embeds                Embeds to send with the message, null if none
     * @param files                 Files to send with the message, null if none
     * @return A Mono<Void> that completes when the message is sent
     */
    public static Mono<Void> createWebhookMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, boolean surroundWithCodeBlock, Collection<? extends EmbedCreateSpec> embeds, Collection<? extends MessageCreateFields.File> files) {
        if (username.isAbsent() && avatarUrl.isAbsent())
            return createPlainTextMessage(channel, message, username, surroundWithCodeBlock); // If username and avatar is absent, fallback to plain text for bot account to be used instead of a webhook with same name (allow color)


        return getMc2DiscordWebhook(channel)
                .flatMapMany(webhook -> Flux.fromIterable(M2DUtils.breakStringInMessages(message, 2000, surroundWithCodeBlock))
                        .flatMap(s -> {
                            WebhookExecuteSpec.Builder builder = WebhookExecuteSpec.builder()
                                    .username(username)
                                    .avatarUrl(avatarUrl)
                                    .allowedMentions(Mc2Discord.INSTANCE.vars.allowedMentions)
                                    .content(s);

                            if (embeds != null)
                                builder.embeds(embeds);
                            if (files != null)
                                builder.files(files);

                            return webhook.execute(builder.build());
                        }))
                .then();
    }

    /**
     * Get the webhook used by the mod for a channel
     *
     * @param channel Channel to get the webhook from
     * @return A Mono<Webhook> that completes when the webhook is found or created
     */
    public static Mono<Webhook> getMc2DiscordWebhook(Snowflake channel) {
        Mono<TopLevelGuildMessageChannel> channelMono = Mc2Discord.INSTANCE.client.getChannelById(channel).ofType(TopLevelGuildMessageChannel.class);
        return channelMono.flatMapMany(TopLevelGuildMessageChannel::getWebhooks)
                .filter(webhook -> webhook.getName().isPresent() && webhook.getName().get().equals(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name))
                .switchIfEmpty(channelMono.flatMap(channel1 -> channel1.createWebhook(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name)))
                .single();
    }

    /**
     * Send a message using plain text
     * See {@link #createPlainTextMessage(Snowflake, String, Possible, boolean, Collection, Collection)} for more details
     */
    public static Mono<Void> createPlainTextMessage(Snowflake channel, String message, Possible<String> username, boolean surroundWithCodeBlock) {
        return createPlainTextMessage(channel, message, username, surroundWithCodeBlock, null, null);
    }

    /**
     * Send a message using plain text
     *
     * @param channel               Discord channel to send the message to
     * @param message               Message to send, may be empty if some others parameters are set
     * @param username              Username to use for the message, Possible.absent() default to the bot name
     * @param surroundWithCodeBlock Whether to surround the message with a code block ``` or not
     * @param embeds                Embeds to send with the message, null if none
     * @param files                 Files to send with the message, null if none
     * @return A Mono<Void> that completes when the message is sent
     */
    public static Mono<Void> createPlainTextMessage(Snowflake channel, String message, Possible<String> username, boolean surroundWithCodeBlock, Collection<? extends EmbedCreateSpec> embeds, Collection<? extends MessageCreateFields.File> files) {
        if (!username.isAbsent()) {
            message = Entity.replace(Mc2Discord.INSTANCE.config.style.discord_chat_format, List.of(new MessageEntity(message), new PlayerEntity(username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), new UUID(0L, 0L))));
        }

        String finalMessage = message;
        return Mc2Discord.INSTANCE.client.getChannelById(channel)
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel -> Flux.fromIterable(M2DUtils.breakStringInMessages(finalMessage, 2000, surroundWithCodeBlock))
                        .flatMap(s -> {
                            MessageCreateSpec.Builder builder = MessageCreateSpec.builder()
                                    .allowedMentions(Mc2Discord.INSTANCE.vars.allowedMentions)
                                    .content(s);

                            if (embeds != null)
                                builder.embeds(embeds);
                            if (files != null)
                                builder.files(files);

                            return messageChannel.createMessage(builder.build());
                        }))
                .then();
    }


    /**
     * Send a message using an embed
     * See {@link #createEmbedMessage(Snowflake, String, Possible, Possible, List, Collection, Collection)} for more details
     */
    public static Mono<Void> createEmbedMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, List<String> types) {
        return createEmbedMessage(channel, message, username, avatarUrl, types, null, null);

    }

    /**
     * Send a message using an embed
     *
     * @param channel   Discord channel to send the message to
     * @param message   Message to send, may be empty if some others parameters are set
     * @param username  Username to use for the message, Possible.absent() default to the bot name
     * @param avatarUrl Avatar url to use for the message, Possible.absent() default to the bot avatar
     * @param types     List of types to match
     * @param embeds    Embeds to send with the message, null if none
     * @param files     Files to send with the message, null if none
     * @return A Mono<Void> that completes when the message is sent
     */
    public static Mono<Void> createEmbedMessage(Snowflake channel, String message, Possible<String> username, Possible<String> avatarUrl, List<String> types, Collection<? extends EmbedCreateSpec> embeds, Collection<? extends MessageCreateFields.File> files) {
        return Mc2Discord.INSTANCE.client.getChannelById(channel)
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel ->
                        Flux.fromIterable(M2DUtils.breakStringInMessages(message, 4096, false))
                                .flatMap(s -> {
                                    MessageCreateSpec.Builder mbuilder = MessageCreateSpec.builder();
                                    EmbedCreateSpec.Builder ebuilder = EmbedCreateSpec.builder().description(s);

                                    ebuilder.color(M2DUtils.getColorFromString(types.stream().map(type -> Mc2Discord.INSTANCE.config.style.embed_colors.<String>get(type)).filter(Objects::nonNull).findFirst().orElse("SUMMER_SKY")));

                                    if (!username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name).equals(Mc2Discord.INSTANCE.vars.mc2discord_display_name) || Mc2Discord.INSTANCE.config.style.embed_show_bot_avatar) {
                                        ebuilder.author(username.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_display_name), null, avatarUrl.toOptional().orElse(Mc2Discord.INSTANCE.vars.mc2discord_avatar));
                                    }

                                    mbuilder.addEmbed(ebuilder.build());

                                    if (embeds != null)
                                        mbuilder.addAllEmbeds(embeds);
                                    if (files != null)
                                        mbuilder.files(files);

                                    return messageChannel.createMessage(mbuilder.build());
                                }))
                .then();
    }
}
