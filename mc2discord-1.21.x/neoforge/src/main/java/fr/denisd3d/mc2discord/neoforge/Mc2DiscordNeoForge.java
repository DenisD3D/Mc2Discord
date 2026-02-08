package fr.denisd3d.mc2discord.neoforge;

import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Mc2DiscordNeoForge.MOD_ID)
public class Mc2DiscordNeoForge {
    public static final String MOD_ID = "mc2discord";

    public Mc2DiscordNeoForge() {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(NeoForgeEvents.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Mc2DiscordMinecraft.onRegisterCommands(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        Mc2DiscordMinecraft.onServerStarting(event.getServer());
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        Mc2DiscordMinecraft.onServerStarted(event.getServer());
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        Mc2DiscordMinecraft.onServerStopped(event.getServer());
    }
}
