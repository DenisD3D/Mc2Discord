package ml.denisd3d.mc2discord.forge.commands;

import java.util.List;

public class HelpCommandImpl {
    public static String execute(int permissionLevel, List<String> commands) {
        /*
        CommandDispatcher<CommandSource> commandDispatcher = ServerLifecycleHooks.getCurrentServer().getCommands().getDispatcher();

        StringBuilder response = new StringBuilder();

        if (permissionLevel >= 0) {
            Map<CommandNode<CommandSource>, String> map = commandDispatcher.getSmartUsage(commandDispatcher.getRoot(), Mc2DiscordForge.commandSource.withPermission(permissionLevel));

            for (String string : map.values()) {
                response.append("/").append(string).append("\n");
            }
        }

        for (String command : commands) {
            CommandNode<CommandSource> node = commandDispatcher.getRoot();
            for (String child : command.split(" "))
            {
                node = node.getChild(child);
            }
            commandDispatcher.getSmartUsage(node, Mc2DiscordForge.commandSource).values().forEach(s -> response.append("/").append(command).append(" ").append(s).append("\n"));
        }

        return response.toString();*/
        return "Not implemented for 1.12.2";
    }
}
