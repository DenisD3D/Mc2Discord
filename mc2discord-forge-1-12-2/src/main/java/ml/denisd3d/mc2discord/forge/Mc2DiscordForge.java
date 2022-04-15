package ml.denisd3d.mc2discord.forge;

import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.events.LifecycleEvents;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandImpl;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandSender;
import ml.denisd3d.mc2discord.forge.commands.M2DCommandImpl;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = "mc2discord", name = "Mc2Discord", version = "0", serverSideOnly = true, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber(Side.SERVER)
public class Mc2DiscordForge {
    private static final Logger LOGGER = LogManager.getLogger();
    public static DiscordCommandSender commandSender;

    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        Mc2Discord.firstInit(false);
        Mc2Discord.INSTANCE = new Mc2Discord(false, new MinecraftImpl());
        event.registerServerCommand(new M2DCommandImpl());
        event.registerServerCommand(new DiscordCommandImpl());
    }

    @Mod.EventHandler
    public static void onServerStarted(FMLServerStartedEvent event) {
        Mc2Discord.INSTANCE.setMinecraftStarted(true);
        LifecycleEvents.bothReadyEvent();
        commandSender = new DiscordCommandSender(FMLCommonHandler.instance().getMinecraftServerInstance());
    }

    @Mod.EventHandler
    public static void onServerStopping(FMLServerStoppingEvent event) {
        LifecycleEvents.onShutdown();
    }
}
