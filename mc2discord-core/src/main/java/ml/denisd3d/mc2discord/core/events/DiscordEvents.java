package ml.denisd3d.mc2discord.core.events;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import discord4j.rest.util.Color;
import ml.denisd3d.mc2discord.api.M2DPluginHelper;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.entities.Entity;
import ml.denisd3d.mc2discord.core.entities.Member;
import ml.denisd3d.mc2discord.core.entities.Message;

import java.util.*;

public class DiscordEvents {
    private static boolean isAddedCommand = false;

    public static void onDiscordMessageReceived(MessageCreateEvent messageCreateEvent) {
        if (!M2DUtils.canHandleEvent()) return;

        if (M2DPluginHelper.execute(plugin -> plugin.onDiscordMessageReceived(messageCreateEvent))) return;

        if (!messageCreateEvent.getMessage().getAuthor().isPresent()) // It's a webhook
        {
            messageCreateEvent.getMessage()
                    .getWebhook()
                    .map(Webhook::getName)
                    .onErrorReturn(throwable -> true, Optional.of(""))
                    .map(s -> s.orElse(""))
                    .subscribe(s -> {
                        if (Mc2Discord.INSTANCE.config.misc.relay_bot_messages && !s.equals("Mc2Discord - " + Mc2Discord.INSTANCE.botName + "#" + Mc2Discord.INSTANCE.botDiscriminator) && Mc2Discord.INSTANCE.config.channels.channels.stream()
                                .anyMatch(channel -> channel.channel_id == messageCreateEvent.getMessage()
                                        .getChannelId()
                                        .asLong() && (channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord_announcement")))) {
                            Member member = new Member(messageCreateEvent.getMessage().getUserData().username(), messageCreateEvent.getMessage()
                                    .getUserData()
                                    .discriminator(), messageCreateEvent.getMessage().getUserData().username(), messageCreateEvent.getMessage()
                                    .getUserData()
                                    .avatar()
                                    .orElse(""), 16777215);
                            Message message = new Message(messageCreateEvent.getMessage().getContent());
                            HashMap<String, String> attachments = new HashMap<>();
                            for (Attachment attachment : messageCreateEvent.getMessage().getAttachments()) {
                                attachments.put(attachment.getFilename(), attachment.getUrl());
                            }
                            Mc2Discord.INSTANCE.iMinecraft.sendMessage(Entity.replace(Mc2Discord.INSTANCE.config.style.minecraft_chat_format, Arrays.asList(member, message)), attachments, "", null);
                        }
                    });
            return;
        }

        User author = messageCreateEvent.getMessage().getAuthor().get();
        if (author.getId().asLong() == Mc2Discord.INSTANCE.botId) // The message is from ourselves
            return;

        if (!Mc2Discord.INSTANCE.config.misc.relay_bot_messages && author.isBot()) // It's a bot message
            return;

        if (Mc2Discord.INSTANCE.m2dAccount != null && Mc2Discord.INSTANCE.config.features.account_linking && Mc2Discord.INSTANCE.m2dAccount.onMessage(messageCreateEvent))
            return;

        if (messageCreateEvent.getMessage().getContent().startsWith(Mc2Discord.INSTANCE.config.commands.prefix)) // It's a command
        {
            if (Mc2Discord.INSTANCE.config.channels.channels.stream()
                    .anyMatch(channel -> channel.channel_id == messageCreateEvent.getMessage()
                            .getChannelId()
                            .asLong() && channel.subscriptions.contains("command")) && messageCreateEvent.getMember()
                    .isPresent()) // The message is in a command channel. Else treat it as a simple chat message
            {
                messageCreateEvent.getMember()
                        .get()
                        .getRoles()
                        .map(role -> role.getId().asLong())
                        .filter(roleId -> Mc2Discord.INSTANCE.config.commands.rules_map.containsKey(roleId))
                        .collectList()
                        .subscribe(longs -> {
                            List<String> allowedCommands = new ArrayList<>();
                            isAddedCommand = false;
                            int result = Mc2Discord.INSTANCE.config.commands.rules_map.entrySet()
                                    .stream()
                                    .filter(longCommandRuleEntry -> longCommandRuleEntry.getKey() == 0L || longCommandRuleEntry.getKey() == author.getId()
                                            .asLong() || longs.contains(longCommandRuleEntry.getKey()))
                                    .map(Map.Entry::getValue)
                                    .map(commandRule -> {
                                        if (commandRule.commands.stream()
                                                .anyMatch(s -> messageCreateEvent.getMessage()
                                                        .getContent()
                                                        .substring(Mc2Discord.INSTANCE.config.commands.prefix.length())
                                                        .startsWith(s))) {
                                            allowedCommands.addAll(commandRule.commands);
                                            isAddedCommand = true;
                                        }
                                        return commandRule.permission_level;
                                    })
                                    .max(Integer::compareTo)
                                    .orElse(-1);

                            if (isAddedCommand || result != -1) {
                                String command = messageCreateEvent.getMessage()
                                        .getContent()
                                        .substring(Mc2Discord.INSTANCE.config.commands.prefix.length());
                                if (command.equals("help")) {
                                    Mc2Discord.INSTANCE.messageManager.sendMessageInChannel(messageCreateEvent.getMessage()
                                            .getChannelId()
                                            .asLong(), "command", Mc2Discord.INSTANCE.iMinecraft.executeHelpCommand(result, allowedCommands), Mc2Discord.INSTANCE.config.channels.channels_map.get(messageCreateEvent.getMessage()
                                            .getChannelId()
                                            .asLong()).mode, Mc2Discord.INSTANCE.config.commands.use_codeblocks, null);
                                } else {
                                    Mc2Discord.INSTANCE.iMinecraft.executeCommand(command, isAddedCommand ? Integer.MAX_VALUE : result, messageCreateEvent.getMessage()
                                            .getChannelId()
                                            .asLong(), Mc2Discord.INSTANCE.config.channels.channels_map.get(messageCreateEvent.getMessage()
                                            .getChannelId()
                                            .asLong()).mode);
                                }
                            } else {
                                Mc2Discord.INSTANCE.messageManager.sendMessageInChannel(messageCreateEvent.getMessage()
                                        .getChannelId()
                                        .asLong(), "command", Mc2Discord.INSTANCE.config.commands.error_message, Mc2Discord.INSTANCE.config.channels.channels_map.get(messageCreateEvent.getMessage()
                                        .getChannelId()
                                        .asLong()).mode, Mc2Discord.INSTANCE.config.commands.use_codeblocks, null);
                            }
                        });
                return;
            }
        }

        // It's a chat message
        if (Mc2Discord.INSTANCE.config.channels.channels.stream()
                .noneMatch(channel -> channel.channel_id == messageCreateEvent.getMessage()
                        .getChannelId()
                        .asLong() && (channel.subscriptions.contains("chat") || channel.subscriptions.contains("discord_announcement")))) // The message isn't in a chat channel or a discord_announcement channel
            return;

        M2DUtils.getMemberColor(messageCreateEvent.getMember().get()).subscribe(integer -> {
            Member member = new Member(author.getUsername(), author.getDiscriminator(), messageCreateEvent.getMember()
                    .map(discord4j.core.object.entity.Member::getDisplayName)
                    .orElse(author.getUsername()), author.getAvatarUrl(), integer);
            Message message = new Message(M2DUtils.replaceAllMentions(messageCreateEvent.getMessage().getContent(), messageCreateEvent.getMessage()
                    .getMemberMentions()));
            HashMap<String, String> attachments = new HashMap<>();
            for (Attachment attachment : messageCreateEvent.getMessage().getAttachments()) {
                attachments.put(attachment.getFilename(), attachment.getUrl());
            }
            Optional<MessageReference> messageReference = messageCreateEvent.getMessage().getMessageReference();
            if (messageReference.isPresent()) {
                messageReference.get()
                        .getClient()
                        .getMessageById(messageReference.get().getChannelId(), messageReference.get().getMessageId().orElse(Snowflake.of(0)))
                        .flatMap(discord4j.core.object.entity.Message::getAuthorAsMember)
                        .subscribe(member1 -> M2DUtils.getMemberColor(messageCreateEvent.getMember().get())
                                .subscribe(integer2 -> Mc2Discord.INSTANCE.iMinecraft.sendMessage(Entity.replace(Mc2Discord.INSTANCE.config.style.minecraft_chat_format, Arrays.asList(member, message)), attachments, Entity.replace(Optional.ofNullable(Mc2Discord.INSTANCE.config.style.reply_format).orElse(""), Collections.singletonList(new Member(member1.getUsername(), member1.getDiscriminator(), member1.getDisplayName(), member1.getAvatarUrl(), integer2))), author.getId()
                                        .asString())));
            } else {
                Mc2Discord.INSTANCE.iMinecraft.sendMessage(Entity.replace(Mc2Discord.INSTANCE.config.style.minecraft_chat_format, Arrays.asList(member, message)), attachments, "", author.getId()
                        .asString());
            }
        });
    }

    public static void onDiscordMemberLeave(MemberLeaveEvent memberLeaveEvent) {
        if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking || Mc2Discord.INSTANCE.config.account.guild_id == 0) {
            return;
        }
        Mc2Discord.INSTANCE.m2dAccount.onMemberLeave(memberLeaveEvent);
    }
}