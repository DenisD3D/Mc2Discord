package fr.denisd3d.mc2discord.minecraft;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.IMinecraft;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.GlobalEntity;
import fr.denisd3d.mc2discord.minecraft.commands.AccountCommands;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftImpl implements IMinecraft {
    private static final Pattern URL_PATTERN = Pattern.compile("(\\b(?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");
    private static final Pattern COMBINED_PATTERN = Pattern.compile(Entity.VARIABLE_PATTERN.pattern() + "|" + URL_PATTERN.pattern()); // Capturing group 1: variable name, capturing group 2: variable format, capturing group 3: url

    public static MutableComponent convertToComponent(String content) {
        return convertToComponent(content, Collections.emptyMap());
    }

    public static MutableComponent convertToComponent(String content, Map<String, MutableComponent> replacements) {
        Matcher matcher = COMBINED_PATTERN.matcher(content);
        MutableComponent component = Component.empty();
        Style baseStyle = Style.EMPTY;

        int lastAppendPosition = 0;

        while (matcher.find()) {
            String variable_name = matcher.group(1);
            String url = matcher.group(3);

            component.append(Component.literal(content.substring(lastAppendPosition, matcher.start())).withStyle(baseStyle));
            lastAppendPosition = matcher.end();

            if (variable_name != null) {
                switch (variable_name) {
                    case "color_start" -> baseStyle = baseStyle.withColor(TextColor.parseColor(matcher.group(2)).getOrThrow());
                    case "color_end" -> baseStyle = baseStyle.withColor(ChatFormatting.WHITE);
                    case "bold_start" -> baseStyle = baseStyle.withBold(true);
                    case "bold_end" -> baseStyle = baseStyle.withBold(false);
                    case "italic_start" -> baseStyle = baseStyle.withItalic(true);
                    case "italic_end" -> baseStyle = baseStyle.withItalic(false);
                    case "underlined_start" -> baseStyle = baseStyle.withUnderlined(true);
                    case "underlined_end" -> baseStyle = baseStyle.withUnderlined(false);
                    case "strikethrough_start" -> baseStyle = baseStyle.withStrikethrough(true);
                    case "strikethrough_end" -> baseStyle = baseStyle.withStrikethrough(false);
                    case "obfuscated_start" -> baseStyle = baseStyle.withObfuscated(true);
                    case "obfuscated_end" -> baseStyle = baseStyle.withObfuscated(false);
                    default -> {
                        MutableComponent replacement = replacements.get(variable_name);
                        if (replacement != null) {
                            component.append(replacement.withStyle(baseStyle));
                        }
                    }
                }
            } else if (url != null) {
                component.append(Component.literal(url).withStyle(baseStyle).withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))));
            }
        }
        component.append(Component.literal(content.substring(lastAppendPosition)).withStyle(baseStyle));
        return component;
    }

    @Override
    public void sendMessage(String content, HashMap<String, String> attachments, String referencedContent, String senderUsername) {
        HashMap<String, MutableComponent> replacements = new HashMap<>();

        MutableComponent attachementsComponent = Component.literal(" ");
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            attachementsComponent.append(Component.literal("[" + entry.getKey() + "]").withStyle(style -> style
                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(entry.getValue())))
                    .withColor(ChatFormatting.BLUE)
                    .withUnderlined(true)));
        }
        replacements.put("attachments", attachementsComponent);

        if (referencedContent != null) {
            replacements.put("reply", Component.literal(referencedContent));
        }

        MutableComponent component = convertToComponent(content, replacements);
        if (senderUsername != null) {
            component.withStyle(style -> style.withClickEvent(new ClickEvent.SuggestCommand("@" + senderUsername)).withInsertion("@" + senderUsername));
        }

        Mc2DiscordMinecraft.server.getPlayerList().broadcastSystemMessage(component, false);
    }

    @Override
    public String executeHelpCommand(Integer permissionLevel, List<String> commands) {
        String prefix = Mc2Discord.INSTANCE.config.commands.prefix;

        CommandDispatcher<CommandSourceStack> commandDispatcher = Mc2DiscordMinecraft.server.getCommands().getDispatcher();

        StringBuilder response = new StringBuilder();
        response.append("Available commands:\n").append(prefix).append("help\n");

        if (permissionLevel >= 0) {
            Map<CommandNode<CommandSourceStack>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), Mc2DiscordMinecraft.commandSource.withPermission(permissionLevel));

            for (String string : map.values()) {
                response.append(prefix).append(string).append("\n");
            }
        }

        for (String command : commands) {
            CommandNode<CommandSourceStack> node = commandDispatcher.getRoot();
            for (String child : command.split(" ")) {
                node = node.getChild(child);
            }
            if (node != null) {
                Map<CommandNode<CommandSourceStack>, String> smartUsage = commandDispatcher.getSmartUsage(node, Mc2DiscordMinecraft.commandSource);
                if (!smartUsage.isEmpty()) {
                    for (String string : smartUsage.values()) {
                        response.append(prefix).append(command).append(" ").append(string).append("\n");
                    }
                } else {
                    response.append(prefix).append(command).append("\n");
                }
            }
        }

        return response.toString();
    }

    @Override
    public void executeCommand(String command, int permissionLevel, Snowflake channelId) {
        DiscordCommandSource.channelId = channelId;
        Mc2DiscordMinecraft.server.getCommands()
                .performPrefixedCommand(Mc2DiscordMinecraft.commandSource.withPermission(permissionLevel), command);
    }

    @Override
    public String getNewVersion() {
        return null;
    }

    @Override
    public String getEnvInfo() {
        return (new CrashReport("Mc2Discord status command / Not a real error!", new Throwable())).getFriendlyReport(ReportType.TEST);
    }

    @Override
    public GlobalEntity getServerData() {
        int onlinePlayerCount = 0;
        for (ServerPlayer player : Mc2DiscordMinecraft.server.getPlayerList().getPlayers()) {
            if (!Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.getUUID())) {
                onlinePlayerCount++;
            }
        }

        MinecraftServer server = Mc2DiscordMinecraft.server;
        return new GlobalEntity(onlinePlayerCount,
                server.getMaxPlayers(),
                Optional.ofNullable(server.playerDataStorage.playerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".dat"))).map(files -> files.length).orElse(0),
                server.getMotd(),
                server.getServerVersion(),
                server.getLocalIp(),
                String.valueOf(server.getPort()));
    }

    @Override
    public String getPlayerNameFromUUID(UUID uuid) {
        return Optional.ofNullable(Mc2DiscordMinecraft.server.getProfileCache()).flatMap(gameProfileCache -> gameProfileCache.get(uuid)).map(GameProfile::getName).orElse(uuid.toString());
    }

    @Override
    public void registerAccountCommands() {
        AccountCommands.register(Mc2DiscordMinecraft.server.getCommands().getDispatcher());
    }
}
