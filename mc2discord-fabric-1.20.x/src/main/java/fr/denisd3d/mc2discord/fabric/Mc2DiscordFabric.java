package fr.denisd3d.mc2discord.fabric;

import com.mojang.brigadier.CommandDispatcher;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.Vars;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.fabric.commands.DiscordCommandImpl;
import fr.denisd3d.mc2discord.fabric.commands.DiscordCommandSource;
import fr.denisd3d.mc2discord.fabric.commands.M2DCommandImpl;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;


public class Mc2DiscordFabric implements ModInitializer {
    public static CommandSourceStack commandSource;
    public static MinecraftServer server;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
        CommandRegistrationCallback.EVENT.register(this::onRegisterCommands);

        FabricEvents.register();
    }

    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection) {
        M2DCommandImpl.register(commandSourceStackCommandDispatcher);
        DiscordCommandImpl.register(commandSourceStackCommandDispatcher);
    }

    private void onServerStarted(MinecraftServer minecraftServer) {
        server = minecraftServer;

        Mc2Discord.INSTANCE = new Mc2Discord(new MinecraftImpl());

        LifecycleEvents.minecraftReady = true;
        Vars.startTime = System.currentTimeMillis();
        LifecycleEvents.mcOrDiscordReady();

        commandSource = new CommandSourceStack(new DiscordCommandSource(), Vec3.ZERO, Vec2.ZERO, minecraftServer.overworld(), Integer.MAX_VALUE, "Discord", Component.literal("Discord"), minecraftServer, null);
    }


    private void onServerStopped(MinecraftServer minecraftServer) {
        LifecycleEvents.onShutdown();
        server = null;
    }
}
