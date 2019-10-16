package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(value = "minecraft2discord")
public class Minecraft2Discord {
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    private static JDA DISCORD_BOT = null;

    public Minecraft2Discord() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerReady);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, this::onServerStop);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPECS);
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, ()-> Pair.of(()-> FMLNetworkConstants.IGNORESERVERONLY, (in, net) -> true));
    }

    public static JDA getDiscordBot() {
        return DISCORD_BOT;
    }

    public void onServerReady(FMLServerStartingEvent event) {
        if (Config.SERVER.discordCommandEnabled.get()) {
            DiscordCommand.register(event.getCommandDispatcher());
        }

        try {
            DISCORD_BOT = new JDABuilder(Config.SERVER.botToken.get()).addEventListener(new BotEvents()).build();
        } catch (LoginException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void onServerStop(FMLServerStoppingEvent event) {
        if (Config.SERVER.sendServerStartStopMessages.get() || Config.SERVER.infoChannel.get() == 0) {
            if (getDiscordBot() == null)
                return;

            TextChannel infoChannel = DISCORD_BOT.getTextChannelById(Config.SERVER.infoChannel.get());
            if (infoChannel != null)
                infoChannel.sendMessage(Config.SERVER.serverStopMessage.get()).queue();
        }

        DISCORD_BOT.shutdown();
    }
}
