package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.core.entities.Game;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class Utils {

    public static String globalVariableReplacement(String message) {
        return message
                .replace("$online_players$", String.valueOf(ServerLifecycleHooks.getCurrentServer().getCurrentPlayerCount()))
                .replace("$max_players$", String.valueOf(ServerLifecycleHooks.getCurrentServer().getMaxPlayers()))
                .replace("$motd$", ServerLifecycleHooks.getCurrentServer().getMOTD())
                .replace("$mc_version$", ServerLifecycleHooks.getCurrentServer().getMinecraftVersion())
                .replace("$server_hostname$", ServerLifecycleHooks.getCurrentServer().getServerHostname())
                .replace("$server_port$", String.valueOf(ServerLifecycleHooks.getCurrentServer().getServerPort()));
    }

    public static void updateDiscordPresence() {
        if (Config.SERVER.enableDiscordPresence.get()) {
            Minecraft2Discord.getDiscordBot().getPresence().setGame(Game.playing(globalVariableReplacement(Config.SERVER.discordPresence.get())));
        }

    }
}
