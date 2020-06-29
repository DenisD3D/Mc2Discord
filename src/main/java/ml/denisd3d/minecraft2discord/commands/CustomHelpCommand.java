package ml.denisd3d.minecraft2discord.commands;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import ml.denisd3d.minecraft2discord.Config;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Map;

public class CustomHelpCommand
{
    public static void execute(CommandSource commandSource)
    {

        Commands commandsManager = ServerLifecycleHooks.getCurrentServer().getCommandManager();

        RootCommandNode<CommandSource> commandRoot = new RootCommandNode<>();

        for (String command : Config.SERVER.allowedCommandForEveryone.get())
        {
            CommandNode<CommandSource> node = commandsManager.getDispatcher().getRoot().getChild(command.split(" ")[0]);

            commandRoot.addChild(node);
        }

        Map<CommandNode<CommandSource>, String> lvt_2_1_ = commandsManager.getDispatcher().getSmartUsage(commandRoot, commandSource);

        for (String lvt_4_1_ : lvt_2_1_.values())
        {
            commandSource.sendFeedback(new StringTextComponent(Config.SERVER.commandPrefix.get() + lvt_4_1_), false);
        }
    }
}
