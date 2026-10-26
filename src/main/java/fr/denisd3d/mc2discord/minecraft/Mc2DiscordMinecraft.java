package fr.denisd3d.mc2discord.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.Vars;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandImpl;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import fr.denisd3d.mc2discord.minecraft.commands.M2DCommandImpl;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class Mc2DiscordMinecraft {
    public static CommandSourceStack commandSource;
    public static MinecraftServer server;

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext buildContext) {
        M2DCommandImpl.register(commandSourceStackCommandDispatcher, buildContext);
        DiscordCommandImpl.register(commandSourceStackCommandDispatcher);
    }

    public static void onServerStarting(MinecraftServer minecraftServer) {
        server = minecraftServer;
        Vars.startTime = System.currentTimeMillis();
        Mc2Discord.INSTANCE = new Mc2Discord(new MinecraftImpl());
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        LifecycleEvents.minecraftReady = true;
        commandSource = VersionCompat.createCommandSource(new DiscordCommandSource(), minecraftServer);
        LifecycleEvents.mcOrDiscordReady();
    }

    public static void onServerStopped(MinecraftServer ignoredMinecraftServer) {
        LifecycleEvents.onShutdown();
        server = null;
    }
}
