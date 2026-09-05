package fr.denisd3d.mc2discord.forge;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;

@Mod(Mc2DiscordForge.MOD_ID)
public class Mc2DiscordForge {
    public static final String MOD_ID = "mc2discord";

    public Mc2DiscordForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> IExtensionPoint.DisplayTest.IGNORESERVERONLY, (a, b) -> true));
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ForgeEvents.class);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        Mc2DiscordMinecraft.onRegisterCommands(event.getDispatcher(), event.getBuildContext());
    }

    @SubscribeEvent
    public void onServerAboutToStart(ServerAboutToStartEvent event) {
        Mc2DiscordMinecraft.onServerStarting(event.getServer());

        Mc2Discord.INSTANCE.vars.modLoader = "Forge";
        Mc2Discord.INSTANCE.vars.modLoaderVersion = FMLLoader.versionInfo().forgeVersion();
        Mc2Discord.INSTANCE.vars.modCount = ModList.get().size();
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
