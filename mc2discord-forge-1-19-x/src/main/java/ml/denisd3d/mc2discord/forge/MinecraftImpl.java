package ml.denisd3d.mc2discord.forge;

import com.mojang.authlib.GameProfile;
import ml.denisd3d.mc2discord.core.IMinecraft;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import ml.denisd3d.mc2discord.core.entities.Global;
import ml.denisd3d.mc2discord.forge.account.AccountImpl;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandSource;
import ml.denisd3d.mc2discord.forge.commands.HelpCommandImpl;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerList;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftImpl implements IMinecraft {

    private static final File FILE_HIDDEN_PLAYERS = new File("hidden-players.json");
    public final HiddenPlayerList hiddenPlayerList = new HiddenPlayerList(FILE_HIDDEN_PLAYERS);
    final Pattern url_pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
    final Pattern color_pattern = Pattern.compile("\\$\\{color_start_(#[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})}(.+?)\\$\\{color_end}");
    final Pattern both_pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]|\\$\\{color_start_(#[A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})}(.+?)\\$\\{color_end}");
    private final IAccount iAccount = new AccountImpl();

    public MinecraftImpl() {
        this.readHiddenPlayerList();
        this.saveHiddenPlayerList();
    }

    public void readHiddenPlayerList() {
        try {
            this.hiddenPlayerList.load();
        } catch (Exception exception) {
            Mc2Discord.logger.warn("Failed to load hidden player list: ", exception);
        }
    }

    public void saveHiddenPlayerList() {
        try {
            this.hiddenPlayerList.save();
        } catch (Exception exception) {
            Mc2Discord.logger.warn("Failed to save hidden player list: ", exception);
        }
    }

    @Override
    public void sendMessage(String content, HashMap<String, String> attachments) {
        Matcher matcher = both_pattern.matcher(content);
        MutableComponent textComponent = Component.literal("");

        int previous_end = 0;
        while (matcher.find()) {
            textComponent.append(Component.literal(content.substring(previous_end, matcher.start())));
            previous_end = matcher.end();


            Matcher url_matcher = url_pattern.matcher(content.substring(matcher.start(), matcher.end()));
            Matcher color_matcher = color_pattern.matcher(content.substring(matcher.start(), matcher.end()));

            if (url_matcher.matches()) {
                textComponent.append(Component.literal(matcher.group()).withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group()))
                        .withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                        .withUnderlined(true)));
            } else if (color_matcher.matches()) {
                textComponent.append(Component.literal(color_matcher.group(2)).withStyle(style -> style
                        .withColor(TextColor.parseColor(color_matcher.group(1)))));
            }
        }
        textComponent.append(Component.literal(content.substring(previous_end) + (attachments.isEmpty() ? "" : " ")));

        attachments.forEach((filename, url) -> textComponent.append(Component.literal("[" + filename + "]").withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE))
                .withUnderlined(true))));


        ServerLifecycleHooks.getCurrentServer()
                .getPlayerList()
                .broadcastSystemMessage(textComponent, false);
    }

    @Override
    public void executeCommand(String command, int permissionLevel, long messageChannelId, Channels.SendMode mode) {
        DiscordCommandSource.messageChannelId = messageChannelId;
        DiscordCommandSource.mode = mode;
        ServerLifecycleHooks.getCurrentServer().getCommands()
                .performPrefixedCommand(Mc2DiscordForge.commandSource.withPermission(permissionLevel), command);
    }

    @Override
    public Global getServerData() {
        int onlinePlayerCount = 0;
        for (ServerPlayer player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (!this.hiddenPlayerList.isHidden(player.getGameProfile())) {
                onlinePlayerCount++;
            }
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return new Global(onlinePlayerCount,
                server.getMaxPlayers(),
                Optional.of(server.playerDataStorage.getPlayerDataFolder())
                        .map(file -> file.list((dir, name) -> name.endsWith(".dat")))
                        .map(strings -> strings.length)
                        .orElse(0),
                server.getMotd(),
                server.getServerVersion(),
                server.getLocalIp(),
                String.valueOf(server.getPort()),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(System.currentTimeMillis() - Mc2Discord.INSTANCE.startTime));
    }

    @Override
    public String executeHelpCommand(int permissionLevel, List<String> commands) {
        return HelpCommandImpl.execute(permissionLevel, commands);
    }

    @Override
    public boolean isPlayerHidden(UUID id, String name) {
        return hiddenPlayerList.contains(new GameProfile(id, name));
    }

    @Override
    public String getNewVersion() {
        VersionChecker.CheckResult versionChecker = VersionChecker.getResult(ModList.get()
                .getModContainerById("mc2discord")
                .orElseThrow(() -> new RuntimeException("Where is Mc2Discord???!"))
                .getModInfo());
        return versionChecker.target() == null ? "" : versionChecker.target().toString();
    }

    @Override
    public String getEnvInfo() {
        return (new CrashReport("Don't panic!", new Throwable())).getFriendlyReport();
    }

    @Override
    public IAccount getIAccount() {
        return iAccount;
    }
}
