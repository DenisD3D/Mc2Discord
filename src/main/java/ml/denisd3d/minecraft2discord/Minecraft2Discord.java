package ml.denisd3d.minecraft2discord;

import com.mojang.datafixers.kinds.App;
import ml.denisd3d.minecraft2discord.api.M2DExtension;
import ml.denisd3d.minecraft2discord.api.M2DUtils;
import ml.denisd3d.minecraft2discord.commands.DiscordCommand;
import ml.denisd3d.minecraft2discord.events.DiscordEvents;
import ml.denisd3d.minecraft2discord.managers.ShutdownManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;

@Mod(value = "minecraft2discord")
public class Minecraft2Discord {
    private static final Logger LOGGER = LogManager.getLogger();
    public static ArrayList<M2DExtension> extensions = new ArrayList<>();
    private static JDA DISCORD_BOT;
    private static long startedTime;
    private static String username;
    private static String avatarURL;

    public Minecraft2Discord() {
        if (Config.SERVER.consoleChannel.get() != 0L)
        {
            LoggerContext lc = (LoggerContext) LogManager.getContext(false); // TODO : Move this code in a better location eg : just after bot start
            DiscordAppender da = DiscordAppender.createAppender("Discord", null);
            da.start();
            lc.getConfiguration().addAppender(da);
            lc.getRootLogger().addAppender(lc.getConfiguration().getAppender(da.getName()));
            lc.updateLoggers();
        }

        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerReady);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStopped);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPECS, "minecraft2discord.toml");
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
    }

    public static JDA getDiscordBot() {
        return DISCORD_BOT;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static long getStartedTime() {
        return startedTime;
    }

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        Minecraft2Discord.username = username;
    }

    public static String getAvatarURL() {
        return avatarURL;
    }

    public static void setAvatarURL(String avatarURL) {
        Minecraft2Discord.avatarURL = avatarURL;
    }

    public void onServerReady(FMLServerStartedEvent event) {
        startedTime = System.currentTimeMillis();

        try {
            DISCORD_BOT = JDABuilder.createDefault(Config.SERVER.token.get())
                    .addEventListeners(new DiscordEvents())
                    .addEventListeners(M2DUtils.eListeners.toArray()).build();
        } catch (LoginException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void onRegisterCommands(RegisterCommandsEvent event) {
        if (Config.SERVER.discordCommandEnabled.get()) {
            DiscordCommand.register(event.getDispatcher());
        }
    }

    public void onServerStopping(FMLServerStoppingEvent event) {
        if (DISCORD_BOT != null) {
            Minecraft2Discord.extensions.forEach(m2DExtension -> m2DExtension.onStop(event));
            ShutdownManager.stopping();
        }
    }

    public void onServerStopped(FMLServerStoppedEvent event) {
        if (DISCORD_BOT != null) {
            ShutdownManager.stopped();
        }
    }
}
