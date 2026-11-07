package fr.denisd3d.mc2discord.minecraft;

import com.google.common.math.Stats;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fr.denisd3d.mc2discord.core.IMinecraft;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.Template;
import fr.denisd3d.mc2discord.minecraft.commands.AccountCommands;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.*;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.9 {
import net.minecraft.server.players.NameAndId;
//?} else {
/*import com.mojang.authlib.GameProfile;
*///?}
import net.minecraft.util.TimeUtil;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftImpl implements IMinecraft {
    private static final Pattern URL_PATTERN = Pattern.compile("(\\b(?:https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])");

    public static MutableComponent toComponent(Template template) {
        return toComponent(template, Map.of(), List.of());
    }

    /**
     * Render a template as a Minecraft component: applies the Minecraft formatting variables and makes urls clickable
     * @param components Values of the variables rendered as components, e.g. ${attachments}
     */
    public static MutableComponent toComponent(Template template, Map<String, Component> components, List<Entity> entities) {
        MutableComponent component = Component.empty();
        Style style = Style.EMPTY;

        for (Template.Part part : template.parts()) {
            switch (part) {
                case Template.Text text -> appendWithLinks(component, text.text(), style);
                case Template.Placeholder placeholder -> {
                    switch (placeholder.name()) {
                        case "color_start" -> style = parseColor(placeholder.format(), entities).map(style::withColor).orElse(style);
                        case "color_end" -> style = style.withColor((TextColor) null); // Back to the default color
                        case "bold_start" -> style = style.withBold(true);
                        case "bold_end" -> style = style.withBold(false);
                        case "italic_start" -> style = style.withItalic(true);
                        case "italic_end" -> style = style.withItalic(false);
                        case "underlined_start" -> style = style.withUnderlined(true);
                        case "underlined_end" -> style = style.withUnderlined(false);
                        case "strikethrough_start" -> style = style.withStrikethrough(true);
                        case "strikethrough_end" -> style = style.withStrikethrough(false);
                        case "obfuscated_start" -> style = style.withObfuscated(true);
                        case "obfuscated_end" -> style = style.withObfuscated(false);
                        default -> {
                            Component replacement = components.get(placeholder.name());
                            if (replacement != null) {
                                component.append(replacement.copy().withStyle(style));
                            } else {
                                // Values like Discord messages must not apply Minecraft formatting codes
                                appendWithLinks(component, Template.resolve(placeholder, entities).replaceAll("§.?", ""), style);
                            }
                        }
                    }
                }
            }
        }
        return component;
    }

    /**
     * @param color A color name, a hex color or "top_role" for the color of the member
     */
    private static Optional<TextColor> parseColor(String color, List<Entity> entities) {
        if ("top_role".equals(color)) {
            color = Template.value("member_top_role_color", null, entities);
        }
        return color == null ? Optional.empty() : TextColor.parseColor(color).result();
    }

    private static void appendWithLinks(MutableComponent component, String text, Style style) {
        Matcher matcher = URL_PATTERN.matcher(text);
        int lastAppendPosition = 0;
        while (matcher.find()) {
            String url = matcher.group();
            component.append(Component.literal(text.substring(lastAppendPosition, matcher.start())).withStyle(style));
            component.append(Component.literal(url).withStyle(style).withStyle(urlStyle -> urlStyle.withClickEvent(VersionCompat.openUrl(url))));
            lastAppendPosition = matcher.end();
        }
        component.append(Component.literal(text.substring(lastAppendPosition)).withStyle(style));
    }

    @Override
    public void sendMessage(Template format, List<Entity> entities, Map<String, String> attachments, String reply, String senderUsername) {
        MutableComponent attachmentsComponent = Component.literal(" ");
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            attachmentsComponent.append(Component.literal("[" + entry.getKey() + "]")
                    .withStyle(style -> style.withClickEvent(VersionCompat.openUrl(entry.getValue())).withColor(ChatFormatting.BLUE).withUnderlined(true)));
        }

        Map<String, Component> components = Map.of("attachments", attachmentsComponent, "reply", Component.literal(reply != null ? reply : ""));
        MutableComponent component = toComponent(format, components, entities);
        if (senderUsername != null) {
            component.withStyle(style -> style.withClickEvent(VersionCompat.suggestCommand("@" + senderUsername)).withInsertion("@" + senderUsername));
        }

        Mc2DiscordMinecraft.server.execute(() -> Mc2DiscordMinecraft.server.getPlayerList().broadcastSystemMessage(component, false));
    }

    @Override
    public String executeHelpCommand(Integer permissionLevel, List<String> commands) {
        String prefix = Mc2Discord.INSTANCE.config.commands.prefix;

        CommandDispatcher<CommandSourceStack> commandDispatcher = Mc2DiscordMinecraft.server.getCommands().getDispatcher();

        StringBuilder response = new StringBuilder();
        response.append("Available commands:\n").append(prefix).append("help\n");

        if (permissionLevel >= 0) {
            Map<CommandNode<CommandSourceStack>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(),
                    VersionCompat.withPermissionLevel(Mc2DiscordMinecraft.commandSource, permissionLevel));

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
    public Mono<String> executeCommand(String command, int permissionLevel) {
        return Mono.create(sink -> Mc2DiscordMinecraft.server.execute(() -> {
            DiscordCommandSource discordCommandSource = new DiscordCommandSource(sink::success);
            CommandSourceStack source = VersionCompat.createCommandSource(discordCommandSource, Mc2DiscordMinecraft.server);
            Mc2DiscordMinecraft.server.getCommands().performPrefixedCommand(VersionCompat.withPermissionLevel(source, permissionLevel), command);
            discordCommandSource.scheduleFlush(); // Also complete if the command did not output anything
        }));
    }

    @Override
    public Mono<CommandCompletion> getCommandCompletion(String input, int permissionLevel) {
        CommandDispatcher<CommandSourceStack> commandDispatcher = Mc2DiscordMinecraft.server.getCommands().getDispatcher();
        CommandSourceStack source = VersionCompat.withPermissionLevel(Mc2DiscordMinecraft.commandSource, permissionLevel);

        // Parsed on the server thread as argument suggestions may read the world
        return Mono.fromFuture(() -> Mc2DiscordMinecraft.server.submit(() -> {
            ParseResults<CommandSourceStack> parseResults = commandDispatcher.parse(input, source);
            List<String> usages = getUsages(commandDispatcher, parseResults);
            return commandDispatcher.getCompletionSuggestions(parseResults)
                    .thenApply(
                            suggestions -> new CommandCompletion(suggestions.getList().stream().map(suggestion -> suggestion.apply(input)).toList(), usages));
        }).thenCompose(completion -> completion));
    }

    /**
     * Same as the hints shown in game: the usage of the arguments that can be typed at the end of the input
     */
    private static List<String> getUsages(CommandDispatcher<CommandSourceStack> commandDispatcher, ParseResults<CommandSourceStack> parseResults) {
        String input = parseResults.getReader().getString();
        SuggestionContext<CommandSourceStack> suggestionContext = parseResults.getContext().findSuggestionContext(input.length());
        String start = input.substring(0, suggestionContext.startPos);

        return commandDispatcher.getSmartUsage(suggestionContext.parent, parseResults.getContext().getSource())
                .entrySet()
                .stream()
                .filter(entry -> !(entry.getKey() instanceof LiteralCommandNode)) // Literals are already suggested
                .map(entry -> start + entry.getValue())
                .toList();
    }

    @Override
    public String getEnvInfo() {
        return (new CrashReport("Mc2Discord status command / Not a real error!", new Throwable())).getFriendlyReport(ReportType.TEST);
    }

    @Override
    public int getOnlinePlayerCount() {
        int onlinePlayerCount = 0;
        for (ServerPlayer player : Mc2DiscordMinecraft.server.getPlayerList().getPlayers()) {
            if (!Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.getUUID())) {
                onlinePlayerCount++;
            }
        }
        return onlinePlayerCount;
    }

    @Override
    public int getMaxPlayers() {
        return Mc2DiscordMinecraft.server.getMaxPlayers();
    }

    @Override
    public int getUniquePlayerCount() {
        File[] playerFiles = Mc2DiscordMinecraft.server.playerDataStorage.playerDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".dat"));
        return playerFiles != null ? playerFiles.length : 0;
    }

    @Override
    public String getMotd() {
        return Mc2DiscordMinecraft.server.getMotd();
    }

    @Override
    public String getMinecraftVersion() {
        return Mc2DiscordMinecraft.server.getServerVersion();
    }

    @Override
    public String getServerHostname() {
        return Mc2DiscordMinecraft.server.getLocalIp();
    }

    @Override
    public int getServerPort() {
        return Mc2DiscordMinecraft.server.getPort();
    }

    @Override
    public double getTps() {
        return TimeUtil.MILLISECONDS_PER_SECOND / Math.max(getMspt(), Mc2DiscordMinecraft.server.tickRateManager().millisecondsPerTick());
    }

    @Override
    public double getMspt() {
        return Stats.meanOf(Mc2DiscordMinecraft.server.getTickTimesNanos()) / TimeUtil.NANOSECONDS_PER_MILLISECOND;
    }

    @Override
    public String getPlayerNameFromUUID(UUID uuid) {
        //? if >=1.21.9 {
        return Mc2DiscordMinecraft.server.services().nameToIdCache().get(uuid).map(NameAndId::name).orElse(uuid.toString());
        //?} else {
        /*return Optional.ofNullable(Mc2DiscordMinecraft.server.getProfileCache())
                .flatMap(gameProfileCache -> gameProfileCache.get(uuid))
                .map(GameProfile::getName)
                .orElse(uuid.toString());
        *///?}
    }

    @Override
    public void registerAccountCommands() {
        AccountCommands.register(Mc2DiscordMinecraft.server.getCommands().getDispatcher());
    }
}
