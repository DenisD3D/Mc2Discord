package ml.denisd3d.minecraft2discord.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import ml.denisd3d.minecraft2discord.forge.MinecraftImpl;
import net.minecraft.command.CommandSource;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DiscordHelpCommandImpl {
    public static String execute(int permissionLevel, List<String> commands) {
        CommandDispatcher<CommandSource> commandDispatcher = ServerLifecycleHooks.getCurrentServer().getCommandManager().getDispatcher();

        StringBuilder response = new StringBuilder();

        if (permissionLevel >= 0) {
            Map<CommandNode<CommandSource>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), MinecraftImpl.commandSource.withPermissionLevel(permissionLevel));
            Iterator var3 = map.values().iterator();

            while (var3.hasNext()) {
                String string = (String) var3.next();
                response.append("/").append(string).append("\n");
            }
        }

        RootCommandNode<CommandSource> commandRoot = new RootCommandNode<>();

        for (String command : commands) {
            commandRoot.addChild(commandDispatcher.getRoot().getChild(command.split(" ")[0]));
        }

        for (String command : commandDispatcher.getSmartUsage(commandRoot, MinecraftImpl.commandSource).values()) {
            response.append("/").append(command).append("\n");
        }

        return response.toString();
    }
}
