package fr.denisd3d.mc2discord.core.events;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import fr.denisd3d.mc2discord.core.AccountManager;
import fr.denisd3d.mc2discord.core.CommandManager;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.MemberEntity;
import fr.denisd3d.mc2discord.core.entities.MessageEntity;
import reactor.core.publisher.Mono;

import java.util.*;

public class DiscordEvent {
    public static void onMessageCreate(MessageCreateEvent event) {
        if (!Mc2Discord.INSTANCE.vars.missingMessageContentIntent && event.getMessage().getContent().isEmpty() && event.getMessage().getEmbeds().isEmpty()
                && event.getMessage().getAttachments().isEmpty() && event.getMessage().getComponents().isEmpty()
                && !event.getMember().map(User::isBot).orElse(true)) { // Missing message content intent
            Mc2Discord.INSTANCE.vars.missingMessageContentIntent = true;
            Mc2Discord.LOGGER.warn("Missing MESSAGE CONTENT intent");
            Mc2Discord.INSTANCE.errors.add("Missing MESSAGE CONTENT intent");
        }

        if (AccountManager.onMessageCreate(event))
            return; // Handled by account manager

        if (event.getMessage().getWebhookId().isPresent()) { // Webhook messages
            if (!Mc2Discord.INSTANCE.config.misc.relay_bot_messages)
                return; // No bot messages

            if (Objects.equals(event.getMessage()
                    .getWebhook()
                    .map(Webhook::getName)
                    .onErrorResume(throwable -> Mono.just(Optional.of(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name)))
                    .map(name -> name.orElse(""))
                    .block(), Mc2Discord.INSTANCE.vars.mc2discord_webhook_name))
                return; // Self messages or slash commands

            if (Mc2Discord.INSTANCE.config.channels.channels.stream()
                    .filter(channel -> channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord"))
                    .anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Chat or Discord
                processMessage(event, MemberEntity.of(getWebhookAuthor(event.getMessage())), null); // Webhooks aren't members
            }

        } else { // User messages
            if (event.getMember().isEmpty())
                return;
            Member member = event.getMember().get();

            if (member.getId().equals(Mc2Discord.INSTANCE.vars.bot_id))
                return; // Self messages

            if (member.isBot() && !Mc2Discord.INSTANCE.config.misc.relay_bot_messages)
                return; // Bot messages

            if (event.getMessage().getContent().isEmpty())
                return; // no text content to process

            if (event.getMessage().getContent().startsWith(Mc2Discord.INSTANCE.config.commands.prefix) && Mc2Discord.INSTANCE.config.channels.channels.stream()
                    .filter(channel -> channel.subscriptions.contains("command"))
                    .anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Commands
                processCommand(event, member);
            } else if (Mc2Discord.INSTANCE.config.channels.channels.stream()
                    .filter(channel -> channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord"))
                    .anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Chat or Discord
                processMessage(event, MemberEntity.of(member, M2DUtils.getMemberColor(member).blockOptional().orElseThrow()),
                        "0".equals(member.getDiscriminator()) ? member.getUsername() : null);
            }
        }
    }

    /**
     * {@link Message#getAuthor()} is empty for webhooks, but the message contains the name and avatar used
     */
    private static User getWebhookAuthor(Message message) {
        return new User(message.getClient(), message.getData().author());
    }

    private static void processCommand(MessageCreateEvent event, Member member) {
        String command = event.getMessage().getContent().substring(Mc2Discord.INSTANCE.config.commands.prefix.length());

        CommandManager.execute(command, member)
                .flatMap(result -> MessageManager.sendMessage(Collections.singletonList("command"), result, MessageManager.default_username,
                        MessageManager.default_avatar, event.getMessage().getChannelId(), Mc2Discord.INSTANCE.config.commands.use_codeblocks))
                .subscribe();
    }

    /**
     * @param senderUsername The Discord username of the sender for quick @, null if it can't be mentioned
     */
    private static void processMessage(MessageCreateEvent event, MemberEntity memberEntity, String senderUsername) {
        // Content
        MessageEntity messageEntity = new MessageEntity(M2DUtils.replaceAllMentions(event.getMessage().getContent(), event.getMessage().getUserMentions(),
                event.getMessage().getRoleMentions().collectList().block(), event.getGuildId().orElse(null)));

        // Attachements
        HashMap<String, String> attachments = event.getMessage()
                .getAttachments()
                .stream()
                .map(attachment -> new HashMap.SimpleEntry<>(attachment.getFilename(), attachment.getUrl()))
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

        // Referenced content
        Optional<String> reply = event.getMessage()
                .getMessageReference()
                .flatMap(MessageReference::getMessageId)
                .flatMap(snowflake -> Optional.of(Mono.defer(() -> event.getMessage().getChannel().flatMap(channel -> channel.getMessageById(snowflake)))))
                .flatMap(message -> Optional.ofNullable(message.block()))
                .flatMap(message -> Optional.ofNullable(message.getAuthorAsMember().block()))
                .map(referencedMember -> MemberEntity.of(referencedMember, M2DUtils.getMemberColor(referencedMember).blockOptional().orElseThrow()))
                .map(referencedMemberEntity -> Mc2Discord.INSTANCE.config.style.reply_format.render(referencedMemberEntity));

        Mc2Discord.INSTANCE.minecraft.sendMessage(Mc2Discord.INSTANCE.config.style.minecraft_chat_format, List.of(messageEntity, memberEntity), attachments,
                reply.orElse(null), senderUsername);
    }

    public static void onMemberJoin(MemberJoinEvent memberJoinEvent) {
        M2DUtils.cacheMember(memberJoinEvent.getMember());
    }

    public static void onMemberLeave(MemberLeaveEvent memberLeaveEvent) {
        Mc2Discord.INSTANCE.vars.memberCache.remove(memberLeaveEvent.getGuildId(), memberLeaveEvent.getUser().getUsername());
    }
}
