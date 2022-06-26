package ml.denisd3d.mc2discord.forge.commands;

import ml.denisd3d.mc2discord.core.M2DCommands;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class M2DCommandImpl extends CommandBase {

    @Override
    public String getName() {
        return "mc2discord";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 3;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/mc2discord <status|restart|upload|invite>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length >= 1) {
            if ("status".equals(args[0])) {
                M2DCommands.getStatus().forEach(s -> sender.sendMessage(new TextComponentString(s)));
                return;
            } else if ("restart".equals(args[0])) {
                M2DCommands.restart().forEach(s -> sender.sendMessage(new TextComponentString(s)));
                return;
            } else if ("upload".equals(args[0])) {
                String[] result = M2DCommands.upload();
                sender.sendMessage(new TextComponentString(result[0])
                        .appendSibling(new TextComponentString(result[1]).setStyle(new Style()
                                .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result[1]))
                                .setColor(TextFormatting.BLUE)
                                .setUnderlined(true))));
                return;
            } else if ("invite".equals(args[0])) {
                String result = M2DCommands.getInviteLink();
                sender.sendMessage(new TextComponentString(result).setStyle(new Style()
                        .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result))
                        .setColor(TextFormatting.BLUE)
                        .setUnderlined(true)));
                return;
            }
        }
        throw new WrongUsageException(getUsage(sender));
    }
}
