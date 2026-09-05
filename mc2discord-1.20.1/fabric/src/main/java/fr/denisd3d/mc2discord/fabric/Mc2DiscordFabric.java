package fr.denisd3d.mc2discord.fabric;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.fabricmc.loader.api.FabricLoader;


public class Mc2DiscordFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(Mc2DiscordFabric::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(Mc2DiscordMinecraft::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(Mc2DiscordMinecraft::onServerStopped);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> Mc2DiscordMinecraft.onRegisterCommands(dispatcher));

        FabricEvents.register();
    }

    public static void onServerStarting(MinecraftServer minecraftServer) {
        Mc2DiscordMinecraft.onServerStarting(minecraftServer);

        Mc2Discord.INSTANCE.vars.modLoader = "Fabric";
        Mc2Discord.INSTANCE.vars.modLoaderVersion = FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().toString();
        Mc2Discord.INSTANCE.vars.modCount = FabricLoader.getInstance().getAllMods().size();
    }
}
