package ml.denisd3d.minecraft2discord.core;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.GatewayObserver;
import ml.denisd3d.minecraft2discord.core.config.M2DConfig;
import ml.denisd3d.minecraft2discord.core.events.DiscordEvents;
import ml.denisd3d.minecraft2discord.core.events.LifecycleEvents;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import reactor.netty.ConnectionObserver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Minecraft2Discord {
    public static final Logger logger = LogManager.getLogger("minecraft2discord");
    public static Minecraft2Discord INSTANCE;
    public static File CONFIG_FILE = new File("config", "minecraft2discord.toml");
    public final M2DConfig config;
    public List<String> errors = new ArrayList<>();
    public GatewayDiscordClient client;
    public long botId = -1;
    public String botName = "Undefined";
    public String botDiscriminator = "0000";
    public String botAvatar;
    public String botDisplayName;
    public IMinecraft iMinecraft;
    public MessageManager messageManager = new MessageManager(this);
    public long startTime;
    private ConnectionObserver.State state = GatewayObserver.DISCONNECTED;
    private boolean isMinecraftStarted;

    public Minecraft2Discord(boolean minecraftReady, IMinecraft iMinecraft) {
        this.isMinecraftStarted = minecraftReady;
        this.iMinecraft = iMinecraft;

        this.config = M2DConfig.load(CONFIG_FILE);
        if (!M2DUtils.isTokenValid(this.config.token)) {
            logger.error("Invalid Discord bot token");
            this.errors.add("Invalid Discord bot token");
            return;
        }

        Thread thread = new Thread(() -> {
            this.client = DiscordClientBuilder.create(this.config.token).build().gateway().setGatewayObserver((newState, client) -> {
                if (newState == GatewayObserver.RETRY_SUCCEEDED) {
                    state = GatewayObserver.CONNECTED;
                } else if (newState != GatewayObserver.SEQUENCE) {
                    state = newState;
                }
            })
                    .login().doOnError(throwable -> errors.add(throwable.getLocalizedMessage()))
                    .block();

            if (this.client == null) {
                this.errors.add("Login error");
                return;
            }

            this.registerEvents(client.getEventDispatcher());
            this.client.onDisconnect().block();
            StatusManager.stop();
        });
        thread.setName("Minecraft2Discord");
        thread.setDaemon(true);
        thread.start();
    }

    public static void firstInit() {
        Configurator.setLevel("discord4j.rest", Level.INFO);
        DiscordLogging.init();
    }

    public void registerEvents(EventDispatcher eventDispatcher) {
        eventDispatcher.on(ReadyEvent.class).subscribe(LifecycleEvents::onReady);
        eventDispatcher.on(MessageCreateEvent.class).subscribe(DiscordEvents::onDiscordMessageReceived);
        StatusManager.register();
        DiscordLogging.logs = ""; // Clear pending logs on restart
    }

    public boolean isDiscordRunning() {
        return this.state == GatewayObserver.CONNECTED;
    }

    public ConnectionObserver.State getState() {
        return state;
    }

    public boolean isMinecraftStarted() {
        return isMinecraftStarted;
    }

    public void setMinecraftStarted(boolean minecraftStarted) {
        isMinecraftStarted = minecraftStarted;
    }

    public long getBotId() {
        return this.botId;
    }

    public void restart() {
        if (client != null) {
            this.client.logout().subscribe(null, null, () -> Minecraft2Discord.INSTANCE = new Minecraft2Discord(true, this.iMinecraft));
        } else {
            Minecraft2Discord.INSTANCE = new Minecraft2Discord(true, this.iMinecraft);
        }
    }
}
