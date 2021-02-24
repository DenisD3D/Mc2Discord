package ml.denisd3d.minecraft2discord.forge;

import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.events.LifecycleEvents;
import ml.denisd3d.minecraft2discord.forge.commands.DiscordCommandImpl;
import ml.denisd3d.minecraft2discord.forge.commands.M2DCommandImpl;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod("minecraft2discord")
@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class Minecraft2DiscordForge {
    public Minecraft2DiscordForge() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        Minecraft2Discord.firstInit();
        Minecraft2Discord.INSTANCE = new Minecraft2Discord(false, new MinecraftImpl());
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        M2DCommandImpl.register(event.getDispatcher());
        DiscordCommandImpl.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(FMLServerStartedEvent event) {
        Minecraft2Discord.INSTANCE.setMinecraftStarted(true);
        LifecycleEvents.bothReadyEvent();
    }

    @SubscribeEvent
    public static void onServerStopping(FMLServerStoppingEvent event) {
        LifecycleEvents.onShutdown();
    }
}
