package ml.denisd3d.mc2discord.forge;

import com.mojang.authlib.GameProfile;
import ml.denisd3d.mc2discord.core.IMinecraft;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.core.entities.Global;
import ml.denisd3d.mc2discord.forge.account.AccountImpl;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandSource;
import ml.denisd3d.mc2discord.forge.commands.HelpCommandImpl;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

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
    final Pattern pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
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
        Matcher matcher = pattern.matcher(content);
        IFormattableTextComponent textComponent = new StringTextComponent("");
        int previous_end = 0;

        while (matcher.find()) {
            textComponent.append(new StringTextComponent(content.substring(previous_end, matcher.start())));
            previous_end = matcher.end();
            textComponent.append(new StringTextComponent(matcher.group()).withStyle(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group()))
                    .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
                    .setUnderlined(true)));
        }
        textComponent.append(new StringTextComponent(content.substring(previous_end) + (attachments.isEmpty() ? "" : " ")));

        attachments.forEach((filename, url) -> textComponent.append(new StringTextComponent("[" + filename + "]").withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
                .setUnderlined(true))));
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(textComponent, ChatType.CHAT, Util.NIL_UUID);
    }

    @Override
    public void executeCommand(String command, int permissionLevel, long messageChannelId, boolean useWebhook) {
        DiscordCommandSource.messageChannelId = messageChannelId;
        DiscordCommandSource.useWebhook = useWebhook;
        ServerLifecycleHooks.getCurrentServer().getCommands()
                .performCommand(Mc2DiscordForge.commandSource.withPermission(permissionLevel), command);
    }

    @Override
    public Global getServerData() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return new Global(server.getPlayerCount(),
                server.getMaxPlayers(),
                Optional.of(server.playerDataStorage.getPlayerDataFolder()).map(file -> file.list((dir, name) -> name.endsWith(".dat"))).map(strings -> strings.length).orElse(0),
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
        VersionChecker.CheckResult versionChecker = VersionChecker.getResult(ModList.get().getModContainerById("mc2discord").orElseThrow(() -> new RuntimeException("Where is Mc2Discord???!")).getModInfo());
        return versionChecker.target == null ? "" : versionChecker.target.toString();
    }

    @Override
    public String getEnvInfo() {
        return new EnvGenerator("Minecraft2Discord debugger. This is not a real crash report !").getFriendlyReport();
    }

    @Override
    public IAccount getIAccount() {
        return iAccount;
    }
}
