package ml.denisd3d.minecraft2discord.forge;

import com.mojang.authlib.GameProfile;
import ml.denisd3d.minecraft2discord.core.IMinecraft;
import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.entities.Global;
import ml.denisd3d.minecraft2discord.forge.commands.DiscordCommandSource;
import ml.denisd3d.minecraft2discord.forge.commands.DiscordHelpCommandImpl;
import ml.denisd3d.minecraft2discord.forge.storage.HiddenPlayerList;
import net.minecraft.command.CommandSource;
import net.minecraft.crash.CrashReport;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
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

public class MinecraftImpl implements IMinecraft {
    public static final CommandSource commandSource = new CommandSource(new DiscordCommandSource(),
            Vector3d.ZERO,
            Vector2f.ZERO,
            ServerLifecycleHooks.getCurrentServer().func_241755_D_(),
            4,
            "Discord",
            new StringTextComponent("Discord"),
            ServerLifecycleHooks.getCurrentServer(),
            null);

    private static final File FILE_HIDDEN_PLAYERS = new File("hidden-players.json");
    public final HiddenPlayerList hiddenPlayerList = new HiddenPlayerList(FILE_HIDDEN_PLAYERS);

    public MinecraftImpl() {
        this.readHiddenPlayerList();
        this.saveHiddenPlayerList();
    }

    public void readHiddenPlayerList() {
        try {
            this.hiddenPlayerList.readSavedFile();
        } catch (Exception exception) {
            Minecraft2Discord.logger.warn("Failed to load white-list: ", exception);
        }
    }

    public void saveHiddenPlayerList() {
        try {
            this.hiddenPlayerList.writeChanges();
        } catch (Exception exception) {
            Minecraft2Discord.logger.warn("Failed to save white-list: ", exception);
        }
    }

    @Override
    public void sendMessage(String content, HashMap<String, String> attachments) {
        IFormattableTextComponent textComponent = new StringTextComponent(content + (attachments.isEmpty() ? "" : " "));
        attachments.forEach((filename, url) -> textComponent.append(new StringTextComponent("[" + filename + "]").modifyStyle(style -> style
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .setColor(Color.fromTextFormatting(TextFormatting.BLUE))
                .setUnderlined(true))));
        ServerLifecycleHooks.getCurrentServer().getPlayerList().func_232641_a_(textComponent, ChatType.CHAT, Util.DUMMY_UUID);

    }

    @Override
    public void executeCommand(String command, int permissionLevel, long messageChannelId, boolean useWebhook) {
        DiscordCommandSource.messageChannelId = messageChannelId;
        DiscordCommandSource.useWebhook = useWebhook;
        ServerLifecycleHooks.getCurrentServer().getCommandManager()
                .handleCommand(commandSource.withPermissionLevel(permissionLevel), command);
    }

    @Override
    public Global getServerData() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        return new Global(server.getCurrentPlayerCount(),
                server.getMaxPlayers(),
                Optional.of(server.playerDataManager.getPlayerDataFolder()).map(file -> file.list((dir, name) -> name.endsWith(".dat"))).map(strings -> strings.length).orElse(0),
                server.getMOTD(),
                server.getMinecraftVersion(),
                server.getServerHostname(),
                String.valueOf(server.getServerPort()),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(Minecraft2Discord.INSTANCE.startTime - System.currentTimeMillis()));
    }

    @Override
    public String executeHelpCommand(int permissionLevel, List<String> commands) {
        return DiscordHelpCommandImpl.execute(permissionLevel, commands);
    }

    @Override
    public boolean isPlayerHidden(UUID id, String name) {
        return hiddenPlayerList.hasEntry(new GameProfile(id, name));
    }

    @Override
    public String getNewVersion() {
        VersionChecker.CheckResult versionChecker = VersionChecker.getResult(ModList.get().getModContainerById("minecraft2discord").orElseThrow(() -> new RuntimeException("Where is Minecraft2Discord???!")).getModInfo());
        return versionChecker.target == null ? "" : versionChecker.target.toString();
    }

    @Override
    public String getEnvInfo() {
        Minecraft2Discord.logger.error("The following error isn't a real crash report !"); // TODO : replace by manual code
        CrashReport crashReport = new CrashReport("Minecraft2Discord debugger. This is not a real crash report !", new Exception("None"));
        return crashReport.getCompleteReport();
    }
}
