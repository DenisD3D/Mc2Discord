package ml.denisd3d.mc2discord.forge;

import ml.denisd3d.mc2discord.core.IMinecraft;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import ml.denisd3d.mc2discord.core.entities.Global;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandSender;
import ml.denisd3d.mc2discord.forge.commands.HelpCommandImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftImpl implements IMinecraft {

    final Pattern pattern = Pattern.compile("\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");

    @Override
    public void sendMessage(String content, HashMap<String, String> attachments) {
        Matcher matcher = pattern.matcher(content);
        ITextComponent textComponent = new TextComponentString("");
        int previous_end = 0;

        while (matcher.find()) {
            textComponent.appendSibling(new TextComponentString(content.substring(previous_end, matcher.start())));
            previous_end = matcher.end();
            textComponent.appendSibling(new TextComponentString(matcher.group()).setStyle(new Style()
                    .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, matcher.group()))
                    .setColor(TextFormatting.BLUE)
                    .setUnderlined(true)));
        }
        textComponent.appendSibling(new TextComponentString(content.substring(previous_end) + (attachments.isEmpty() ? "" : " ")));

        attachments.forEach((filename, url) -> textComponent.appendSibling(new TextComponentString("[" + filename + "]").setStyle(new Style()
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url))
                .setColor(TextFormatting.BLUE)
                .setUnderlined(true))));
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendMessage(textComponent);
    }

    @Override
    public void executeCommand(String command, int permissionLevel, long messageChannelId, Channels.SendMode mode) {
        DiscordCommandSender.messageChannelId = messageChannelId;
        DiscordCommandSender.mode = mode;
        DiscordCommandSender.permissionLevel = permissionLevel;
        FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager().executeCommand(Mc2DiscordForge.commandSender, command);
    }

    @Override
    public Global getServerData() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        return new Global(server.getCurrentPlayerCount(),
                server.getMaxPlayers(),
                Optional.of(new File(((SaveHandler) server.getEntityWorld().getSaveHandler().getPlayerNBTManager()).getWorldDirectory(), "playerdata")).map(file -> file.list((dir, name) -> name.endsWith(".dat"))).map(strings -> strings.length).orElse(0),
                server.getMOTD(),
                server.getMinecraftVersion(),
                server.getServerHostname(),
                String.valueOf(server.getServerPort()),
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(System.currentTimeMillis() - Mc2Discord.INSTANCE.startTime));
    }

    @Override
    public String executeHelpCommand(int permissionLevel, List<String> commands) {
        return HelpCommandImpl.execute(permissionLevel, commands);
    }

    @Override
    public boolean isPlayerHidden(UUID id, String name) {
        return false;
    }

    @Override
    public String getNewVersion() {
        ForgeVersion.CheckResult versionChecker = ForgeVersion.getResult(Loader.instance().getIndexedModList().get("mc2discord"));
        return versionChecker.target == null ? "" : versionChecker.target.toString();
    }

    @Override
    public String getEnvInfo() {
        return new EnvGenerator("Minecraft2Discord debugger. This is not a real crash report !").getFriendlyReport();
    }

    @Override
    public IAccount getIAccount() {
        return null;
    }
}
