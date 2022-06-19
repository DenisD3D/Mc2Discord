package ml.denisd3d.mc2discord.forge;

import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.events.LifecycleEvents;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandImpl;
import ml.denisd3d.mc2discord.forge.commands.DiscordCommandSource;
import ml.denisd3d.mc2discord.forge.commands.M2DCommandImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod("mc2discord")
@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class Mc2DiscordForge {

    private static final Logger LOGGER = LogManager.getLogger();
    public static CommandSourceStack commandSource = null;

    public Mc2DiscordForge() {
        ModLoadingContext.get()
                .registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        if (ModList.get().isLoaded("minecraft2discord")) {
            throw new RuntimeException("An old version of mc2discord is present under the minecraft2discord name. Please delete the old jar (minecraft2discord-forge-x.x.x.jar)");
        }

        File oldConfig = new File("config", "minecraft2discord.toml");
        File newConfig = new File("config", "mc2discord.toml");
        if (!newConfig.exists() && oldConfig.exists() && oldConfig.renameTo(newConfig)) {
            LOGGER.info("Mc2Discord config file moved to new location");
        }

        Mc2Discord.firstInit(true);
        Mc2Discord.INSTANCE = new Mc2Discord(false, new MinecraftImpl());
    }

    @SubscribeEvent
    public static void onCommandRegister(RegisterCommandsEvent event) {
        M2DCommandImpl.register(event.getDispatcher());
        DiscordCommandImpl.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        Mc2Discord.INSTANCE.setMinecraftStarted(true);
        LifecycleEvents.bothReadyEvent();

        commandSource = new CommandSourceStack(new DiscordCommandSource(),
                Vec3.ZERO,
                Vec2.ZERO,
                ServerLifecycleHooks.getCurrentServer().overworld(),
                4,
                "Discord",
                new TextComponent("Discord"),
                ServerLifecycleHooks.getCurrentServer(),
                null);
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        LifecycleEvents.onShutdown();
    }
}
