package fr.denisd3d.mc2discord.core;

import com.electronwill.nightconfig.core.io.ParsingException;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import fr.denisd3d.mc2discord.core.config.M2DConfig;
import fr.denisd3d.mc2discord.core.events.DiscordEvent;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerList;
import fr.denisd3d.mc2discord.core.storage.LinkedPlayerList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

public class Mc2Discord {
    public static final Logger LOGGER = LoggerFactory.getLogger("Mc2Discord");
    public static Mc2Discord INSTANCE;
    public GatewayDiscordClient client;
    public M2DConfig config;
    public final IMinecraft minecraft;
    public final LangManager langManager;
    public final List<String> errors = new ArrayList<>();
    public final Vars vars = new Vars();
    public final HiddenPlayerList hiddenPlayerList = new HiddenPlayerList();
    public final LinkedPlayerList linkedPlayerList = new LinkedPlayerList();

    public Mc2Discord(IMinecraft minecraft) {
        this.minecraft = minecraft;
        this.langManager = new LangManager();
        if (!loadConfig()) return;

        if (!M2DUtils.isTokenValid(this.config.general.token)) {
            LOGGER.error("Invalid Discord bot token");
            this.errors.add("Invalid Discord bot token");
            return;
        }

        DiscordClientBuilder.create(this.config.general.token)
                .build()
                .gateway()
                .setEnabledIntents(IntentSet.all().andNot(IntentSet.of(Intent.GUILD_PRESENCES)))
                .login()
                .doOnError(throwable -> {
                    LOGGER.error("Can't start Discord bot", throwable);
                    this.errors.add("Error while connecting to Discord");
                })
                .subscribe(gatewayDiscordClient -> {
                    this.client = gatewayDiscordClient;
                    this.client.on(ReadyEvent.class).subscribe(LifecycleEvents::onDiscordReady);
                    this.client.on(MessageCreateEvent.class).subscribe(DiscordEvent::onMessageCreate);
                    this.client.on(MemberJoinEvent.class).subscribe(DiscordEvent::onMemberJoin);
                    this.client.on(MemberLeaveEvent.class).subscribe(DiscordEvent::onMemberLeave);
                });
    }

    private boolean loadConfig() {
        try {
            this.config = new M2DConfig(M2DUtils.CONFIG_FILE, translationKey -> {
                if ("config.lang.comment".equals(translationKey)) {
                    return langManager.formatMessage(translationKey, String.join(", ", LangManager.LANG_CONTRIBUTORS), String.join(", ", LangManager.AVAILABLE_LANG));
                }
                return minecraft.translateKey(langManager, translationKey);
            }).loadAndCorrect();
        } catch (ParsingException parsingException) {
            config = null;
            this.errors.add("Config file has errors. See logs for more.");
            LOGGER.error("Config file has errors", parsingException);
        }

        return config != null;
    }

    public void restart() {
        shutdown().block();
        INSTANCE = new Mc2Discord(this.minecraft);
    }

    public Mono<Void> shutdown() {
        StatusManager.stop();
        LoggingManager.stop();
        AccountManager.stop();

        return this.client != null ? this.client.logout() : Mono.empty();
    }
}
