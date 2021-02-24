package ml.denisd3d.minecraft2discord.core.events;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.User;
import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.entities.Entity;
import ml.denisd3d.minecraft2discord.core.entities.Member;
import ml.denisd3d.minecraft2discord.core.entities.Message;

import java.util.*;

public class DiscordEvents {
    private static boolean isAddedCommand = false;

    public static void onDiscordMessageReceived(MessageCreateEvent messageCreateEvent) {
        if (!messageCreateEvent.getMessage().getAuthor().isPresent()) // It's a webhook
            return;

        User author = messageCreateEvent.getMessage().getAuthor().get();
        if (author.getId().asLong() == Minecraft2Discord.INSTANCE.botId) // The message is from ourself
            return;

        if (!Minecraft2Discord.INSTANCE.config.relay_bot_messages && author.isBot()) // It's a bot message
            return;

        if (messageCreateEvent.getMessage().getContent().startsWith(Minecraft2Discord.INSTANCE.config.command_prefix)) // It's a command
        {
            if (Minecraft2Discord.INSTANCE.config.channels.stream().anyMatch(channel -> channel.channel_id == messageCreateEvent.getMessage().getChannelId().asLong() && channel.subscriptions.contains("command")) && messageCreateEvent.getMember().isPresent()) // The message is in a command channel. Else treat it as a simple chat message
            {
                messageCreateEvent.getMember().get().getRoles()
                        .map(role -> role.getId().asLong())
                        .filter(roleId -> Minecraft2Discord.INSTANCE.config.command_rules_map.containsKey(roleId)).collectList().subscribe(longs -> {
                    List<String> allowedCommands = new ArrayList<>();

                    int result = Minecraft2Discord.INSTANCE.config.command_rules_map.entrySet().stream()
                            .filter(longCommandRuleEntry -> longCommandRuleEntry.getKey() == 0L || longCommandRuleEntry.getKey() == author.getId().asLong() || longs.contains(longCommandRuleEntry.getKey()))
                            .map(Map.Entry::getValue)
                            .map(commandRule -> {
                                if (commandRule.commands.contains(messageCreateEvent.getMessage().getContent().substring(Minecraft2Discord.INSTANCE.config.command_prefix.length()).split(" ", 2)[0])) {
                                    allowedCommands.addAll(commandRule.commands);
                                    isAddedCommand = true;
                                }
                                return commandRule.permission_level;
                            }).max(Integer::compareTo).orElse(-1);

                    if (isAddedCommand || result != -1) {
                        String command = messageCreateEvent.getMessage().getContent().substring(Minecraft2Discord.INSTANCE.config.command_prefix.length());
                        if (command.equals("help")) {
                            Minecraft2Discord.INSTANCE.messageManager.sendMessageInChannel(messageCreateEvent.getMessage().getChannelId().asLong(), Minecraft2Discord.INSTANCE.iMinecraft.executeHelpCommand(result, allowedCommands), Minecraft2Discord.INSTANCE.config.channels_map.containsKey(messageCreateEvent.getMessage().getChannelId().asLong()) && Minecraft2Discord.INSTANCE.config.channels_map.get(messageCreateEvent.getMessage().getChannelId().asLong()).use_webhook, Minecraft2Discord.INSTANCE.config.use_codeblocks, null);
                        } else {
                            Minecraft2Discord.INSTANCE.iMinecraft.executeCommand(command, result, messageCreateEvent.getMessage().getChannelId().asLong(), Minecraft2Discord.INSTANCE.config.channels_map.containsKey(messageCreateEvent.getMessage().getChannelId().asLong()) && Minecraft2Discord.INSTANCE.config.channels_map.get(messageCreateEvent.getMessage().getChannelId().asLong()).use_webhook);
                        }
                    } else {
                        Minecraft2Discord.INSTANCE.messageManager.sendMessageInChannel(messageCreateEvent.getMessage().getChannelId().asLong(), Minecraft2Discord.INSTANCE.config.command_error_message, Minecraft2Discord.INSTANCE.config.channels_map.containsKey(messageCreateEvent.getMessage().getChannelId().asLong()) && Minecraft2Discord.INSTANCE.config.channels_map.get(messageCreateEvent.getMessage().getChannelId().asLong()).use_webhook, Minecraft2Discord.INSTANCE.config.use_codeblocks, null);
                    }
                });
                return;
            }
        }

        // It's a chat message
        if (Minecraft2Discord.INSTANCE.config.channels.stream().noneMatch(channel -> channel.channel_id == messageCreateEvent.getMessage().getChannelId().asLong() && channel.subscriptions.contains("chat"))) // The message isn't in a chat channel
            return;

        Member member = new Member(author.getUsername(), author.getDiscriminator(), author.getAvatarUrl());
        Message message = new Message(messageCreateEvent.getMessage().getContent());
        HashMap<String, String> attachments = new HashMap<>();
        for (Attachment attachment : messageCreateEvent.getMessage().getAttachments()) {
            attachments.put(attachment.getFilename(), attachment.getUrl());
        }
        Minecraft2Discord.INSTANCE.iMinecraft.sendMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.minecraft_chat_format, Arrays.asList(member, message)), attachments);
    }
}
