package ml.denis3d.minecraft2discord;

import ml.denis3d.minecraft2discord.events.DiscordEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Date;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(value = "minecraft2discord")
public class Minecraft2Discord {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static JDA DISCORD_BOT = null;

    public Minecraft2Discord() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerReady);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStarting);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStop);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStopped);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPECS);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()-> Pair.of(()-> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
    }

    public static JDA getDiscordBot() {
        return DISCORD_BOT;
    }

    public void onServerReady(FMLServerStartedEvent event)
    {
        Utils.started_time = new Date().getTime();

        try
        {
            DISCORD_BOT = new JDABuilder(Config.SERVER.botToken.get()).addEventListeners(new DiscordEvents()).build();
        } catch (LoginException e)
        {
            LOGGER.error(e.getMessage());
        }
    }

    public void onServerStarting(FMLServerStartingEvent event)
    {
        if (Config.SERVER.enabledDiscordCommand.get())
        {
            DiscordCommand.register(event.getCommandDispatcher());
        }
    }

    public void onServerStop(FMLServerStoppingEvent event)
    {
        if (Config.SERVER.sendServerStartStopMessages.get() || Config.SERVER.infoChannel.get() == 0)
        {
            if (getDiscordBot() == null)
                return;

            Utils.sendInfoMessage(Config.SERVER.serverStopMessage.get());
        }

        DISCORD_BOT.shutdown();
    }

    public void onServerStopped(FMLServerStoppedEvent event)
    {
        if (DISCORD_BOT != null && DISCORD_BOT.getStatus() != JDA.Status.SHUTDOWN)
            DISCORD_BOT.shutdownNow();
    }
}
