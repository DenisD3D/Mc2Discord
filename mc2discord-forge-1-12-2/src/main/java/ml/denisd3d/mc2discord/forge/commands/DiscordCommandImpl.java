package ml.denisd3d.mc2discord.forge.commands;

import mcp.MethodsReturnNonnullByDefault;
import ml.denisd3d.mc2discord.core.M2DCommands;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class DiscordCommandImpl extends CommandBase {

    @Override
    public String getName() {
        return "discord";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/discord";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        sender.sendMessage(new TextComponentString(M2DCommands.getDiscordText()).setStyle(new Style()
                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, M2DCommands.getDiscordLink()))
                .setColor(TextFormatting.BLUE)
                .setUnderlined(true)));
    }
}
