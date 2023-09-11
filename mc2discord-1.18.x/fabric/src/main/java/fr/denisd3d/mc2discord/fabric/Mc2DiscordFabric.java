package fr.denisd3d.mc2discord.fabric;

import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;


public class Mc2DiscordFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(Mc2DiscordMinecraft::onServerStarting);
        ServerLifecycleEvents.SERVER_STARTED.register(Mc2DiscordMinecraft::onServerStarted);
        ServerLifecycleEvents.SERVER_STOPPED.register(Mc2DiscordMinecraft::onServerStopped);
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> Mc2DiscordMinecraft.onRegisterCommands(dispatcher));

        FabricEvents.register();
    }
}
