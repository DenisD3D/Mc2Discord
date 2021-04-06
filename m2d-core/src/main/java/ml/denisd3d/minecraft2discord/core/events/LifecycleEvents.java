package ml.denisd3d.minecraft2discord.core.events;

import discord4j.core.event.domain.lifecycle.ReadyEvent;
import ml.denisd3d.minecraft2discord.api.IM2DPlugin;
import ml.denisd3d.minecraft2discord.api.M2DPluginHelper;
import ml.denisd3d.minecraft2discord.core.M2DUtils;
import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.StatusManager;
import ml.denisd3d.minecraft2discord.core.entities.Entity;

import java.util.Collections;

public class LifecycleEvents {
    public static void onReady(ReadyEvent readyEvent) {
        Minecraft2Discord.INSTANCE.botName = readyEvent.getSelf().getUsername();
        Minecraft2Discord.INSTANCE.botDiscriminator = readyEvent.getSelf().getDiscriminator();
        Minecraft2Discord.INSTANCE.botId = readyEvent.getSelf().getId().asLong();
        Minecraft2Discord.INSTANCE.botDisplayName = Minecraft2Discord.INSTANCE.config.bot_name.isEmpty() ? readyEvent.getSelf().getUsername() : Entity.replace(Minecraft2Discord.INSTANCE.config.bot_name, Collections.emptyList());
        Minecraft2Discord.INSTANCE.botAvatar = Minecraft2Discord.INSTANCE.config.bot_avatar.isEmpty() ? readyEvent.getSelf().getAvatarUrl() : Entity.replace(Minecraft2Discord.INSTANCE.config.bot_avatar, Collections.emptyList());

        Minecraft2Discord.logger.info("Discord bot connected as " + Minecraft2Discord.INSTANCE.botName);
        String newVersion = Minecraft2Discord.INSTANCE.iMinecraft.getNewVersion();
        if (!newVersion.isEmpty()) {
            Minecraft2Discord.logger.info("New version available: " + newVersion);
        }
        LifecycleEvents.bothReadyEvent();
    }

    public static void bothReadyEvent() {
        if (!(Minecraft2Discord.INSTANCE.isDiscordRunning() && Minecraft2Discord.INSTANCE.isMinecraftStarted()))
            return; // This method is called by both when ready. When the last is ready it will execute the rest of the method
        if (!M2DUtils.canHandleEvent())
            return;
        if (M2DPluginHelper.execute(IM2DPlugin::onReady))
            return;

        Minecraft2Discord.INSTANCE.startTime = System.currentTimeMillis();
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.start_message, Collections.emptyList()));
    }

    public static void onShutdown() {
        if (!M2DUtils.canHandleEvent())
            return;

        if (M2DPluginHelper.execute(IM2DPlugin::onShutdown))
            return;

        Minecraft2Discord.INSTANCE.is_stopping = true;
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.stop_message, Collections.emptyList()));
        StatusManager.stop();
    }
}
