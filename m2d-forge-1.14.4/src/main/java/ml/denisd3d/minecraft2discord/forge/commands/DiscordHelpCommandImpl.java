package ml.denisd3d.minecraft2discord.forge.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import ml.denisd3d.minecraft2discord.forge.Minecraft2DiscordForge;
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
            Map<CommandNode<CommandSource>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), Minecraft2DiscordForge.commandSource.withPermissionLevel(permissionLevel));
            Iterator var3 = map.values().iterator();

            while (var3.hasNext()) {
                String string = (String) var3.next();
                response.append("/").append(string).append("\n");
            }
        }

        for (String command : commands) {
            commandDispatcher.getSmartUsage(commandDispatcher.getRoot().getChild(command.split(" ")[0]), Minecraft2DiscordForge.commandSource).values().forEach(s -> response.append("/").append(s).append("\n"););
        }

        return response.toString();
    }
}
