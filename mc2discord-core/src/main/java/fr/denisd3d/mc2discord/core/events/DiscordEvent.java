package fr.denisd3d.mc2discord.core.events;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import fr.denisd3d.mc2discord.core.AccountManager;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.MemberEntity;
import fr.denisd3d.mc2discord.core.entities.MessageEntity;
import reactor.core.publisher.Mono;

import java.util.*;

public class DiscordEvent {
    public static void onMessageCreate(MessageCreateEvent event) {
        if (!Mc2Discord.INSTANCE.vars.missingMessageContentIntent && event.getMessage().getContent().isEmpty() && event.getMessage().getEmbeds().isEmpty() && event.getMessage().getAttachments().isEmpty() && event.getMessage().getComponents().isEmpty() && !event.getMember().map(User::isBot).orElse(true)) { // Missing message content intent
            Mc2Discord.INSTANCE.vars.missingMessageContentIntent = true;
            Mc2Discord.LOGGER.warn("Missing message content intent");
            Mc2Discord.INSTANCE.errors.add("Missing message content intent");
        }

        if (AccountManager.onMessageCreate(event)) return; // Handled by account manager

        if (event.getMessage().getWebhookId().isPresent()) { // Webhook messages
            if (!Mc2Discord.INSTANCE.config.misc.relay_bot_messages) return; // No bot messages

            if (event.getMessage().getWebhook().blockOptional().flatMap(Webhook::getName).orElse("").equals(Mc2Discord.INSTANCE.vars.mc2discord_webhook_name))
                return; // Self messages

            if (Mc2Discord.INSTANCE.config.channels.channels.stream().filter(channel -> channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord")).anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Chat or Discord
                processMessage(event, event.getMember().get());
            }

        } else { // User messages
            if (event.getMember().isEmpty()) return;
            Member member = event.getMember().get();

            if (member.getId().equals(Mc2Discord.INSTANCE.vars.bot_id)) return; // Self messages

            if (member.isBot() && !Mc2Discord.INSTANCE.config.misc.relay_bot_messages) return; // Bot messages

            if (event.getMessage().getContent().startsWith(Mc2Discord.INSTANCE.config.commands.prefix) && Mc2Discord.INSTANCE.config.channels.channels.stream().filter(channel -> channel.subscriptions.contains("command")).anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Commands
                processCommand(event, member);
            } else if (Mc2Discord.INSTANCE.config.channels.channels.stream().filter(channel -> channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord")).anyMatch(channel -> channel.channel_id.equals(event.getMessage().getChannelId()))) { // Chat or Discord
                processMessage(event, member);
            }
        }
    }

    private static void processCommand(MessageCreateEvent event, Member member) {
        Set<Snowflake> memberIds = member.getRoleIds(); // Roles ids
        memberIds.add(member.getId()); // Self id
        memberIds.add(M2DUtils.NIL_SNOWFLAKE); // Everyone id

        Integer permission_level = Mc2Discord.INSTANCE.config.commands.permissions.stream().filter(commandRule -> memberIds.contains(commandRule.id)).map(commandRule -> commandRule.permission_level).max(Integer::compareTo).orElse(-1);
        List<String> commands = Mc2Discord.INSTANCE.config.commands.permissions.stream().filter(commandRule -> memberIds.contains(commandRule.id)).flatMap(commandRule -> commandRule.commands.stream()).filter(s -> !s.isEmpty()).toList();

        String command = event.getMessage().getContent().substring(Mc2Discord.INSTANCE.config.commands.prefix.length());

        if (command.equals("help")) { // Help command
            String result = Mc2Discord.INSTANCE.minecraft.executeHelpCommand(permission_level, commands);
            MessageManager.sendMessage(Collections.singletonList("command"), result, MessageManager.default_username, MessageManager.default_avatar, event.getMessage().getChannelId(), Mc2Discord.INSTANCE.config.commands.use_codeblocks).subscribe();
        } else if (commands.stream().anyMatch(command::startsWith)) { // Command listed as allowed
            Mc2Discord.INSTANCE.minecraft.executeCommand(command, Integer.MAX_VALUE, event.getMessage().getChannelId());
        } else {
            Mc2Discord.INSTANCE.minecraft.executeCommand(command, permission_level, event.getMessage().getChannelId());
        }

    }

    private static void processMessage(MessageCreateEvent event, Member member) {
        // Content
        MessageEntity messageEntity = new MessageEntity(M2DUtils.replaceAllMentions(event.getMessage().getContent(), event.getMessage().getUserMentions(), event.getMessage().getRoleMentions().collectList().block(), event.getGuildId().orElse(null)));
        MemberEntity memberEntity = new MemberEntity(member.getGlobalName().orElse(member.getUsername()), member.getUsername(), member.getNickname().orElse(""), member.getAvatarUrl(), M2DUtils.getMemberColor(member).blockOptional().orElseThrow());
        String content = Entity.replace(Mc2Discord.INSTANCE.config.style.minecraft_chat_format, Arrays.asList(messageEntity, memberEntity));

        // Attachements
        HashMap<String, String> attachments = event.getMessage().getAttachments().stream().map(attachment -> new HashMap.SimpleEntry<>(attachment.getFilename(), attachment.getUrl())).collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), HashMap::putAll);

        // Referenced content
        Optional<MemberEntity> referencedMemberEntity = event.getMessage().getMessageReference().flatMap(MessageReference::getMessageId).flatMap(snowflake -> Optional.of(Mono.defer(() -> event.getMessage().getChannel().flatMap(channel -> channel.getMessageById(snowflake))))).flatMap(message -> Optional.ofNullable(message.block())).flatMap(message -> Optional.ofNullable(message.getAuthorAsMember().block())).flatMap(referenced_member -> Optional.of(new MemberEntity(referenced_member.getGlobalName().orElse(referenced_member.getUsername()), referenced_member.getUsername(), referenced_member.getNickname().orElse(""), referenced_member.getAvatarUrl(), M2DUtils.getMemberColor(referenced_member).blockOptional().orElseThrow())));
        Optional<String> referencedContent = referencedMemberEntity.flatMap(entity -> Optional.of(Entity.replace(Mc2Discord.INSTANCE.config.style.reply_format, Collections.singletonList(entity))));

        Mc2Discord.INSTANCE.minecraft.sendMessage(content, attachments, referencedContent.orElse(null), "0".equals(member.getDiscriminator()) ? member.getUsername() : null);
    }

    public static void onMemberJoin(MemberJoinEvent memberJoinEvent) {
        M2DUtils.cacheMember(memberJoinEvent.getMember());
    }

    public static void onMemberLeave(MemberLeaveEvent memberLeaveEvent) {
        Mc2Discord.INSTANCE.vars.memberCache.remove(memberLeaveEvent.getGuildId(), memberLeaveEvent.getUser().getUsername());
    }
}
