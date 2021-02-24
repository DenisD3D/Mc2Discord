package ml.denisd3d.minecraft2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.possible.Possible;
import ml.denisd3d.minecraft2discord.core.config.M2DConfig;
import ml.denisd3d.minecraft2discord.core.entities.Entity;

import java.time.Duration;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class StatusManager {
    static Timer timer;

    public static void register() {
        timer = new Timer(true); // Create a new daemon timer
        if (!Minecraft2Discord.INSTANCE.config.presence_message.isEmpty() || Minecraft2Discord.INSTANCE.config.presence_update != 0) {
            timer.schedule(new PresenceUpdateTask(Minecraft2Discord.INSTANCE.config.presence_message), 0, Minecraft2Discord.INSTANCE.config.presence_update * 1000);
        }

        for (M2DConfig.StatusChannel statusChannel : Minecraft2Discord.INSTANCE.config.status_channels) {
            if (statusChannel.channel_id == 0 || statusChannel.update_period == 0 || (statusChannel.name_message.isEmpty() && statusChannel.topic_message.isEmpty()))
                continue;
            timer.schedule(new ChannelUpdateTask(statusChannel), 0, statusChannel.update_period * 1000);
        }
    }

    public static void stop() {
        timer.cancel();
    }

    static class ChannelUpdateTask extends TimerTask {
        private final M2DConfig.StatusChannel statusChannel;
        private boolean shouldStopNextTimeout = false;

        public ChannelUpdateTask(M2DConfig.StatusChannel statusChannel) {
            this.statusChannel = statusChannel;
        }

        public void run() {
            try {
                if (M2DUtils.canHandleEvent()) {
                    Minecraft2Discord.INSTANCE.client.rest().getChannelById(Snowflake.of(this.statusChannel.channel_id))
                            .modify(ChannelModifyRequest.builder()
                                    .name(!this.statusChannel.name_message.isEmpty() ? Possible.of(Entity.replace(this.statusChannel.name_message, Collections.emptyList())) : Possible.absent())
                                    .topic(!this.statusChannel.topic_message.isEmpty() ? Possible.of(Entity.replace(this.statusChannel.topic_message, Collections.emptyList())) : Possible.absent())
                                    .build(), null)
                            .timeout(Duration.ofSeconds(3))
                            .doOnError(throwable -> {
                                if (throwable instanceof TimeoutException) {
                                    if (this.shouldStopNextTimeout) {
                                        Minecraft2Discord.logger.error("Seem that the channel " + this.statusChannel.channel_id + " is updated too quickly. Try increasing the update period");
                                        Minecraft2Discord.INSTANCE.errors.add("Seem that the channel " + this.statusChannel.channel_id + " is updated too quickly. Try increasing the update period");
                                        this.cancel();
                                    } else {
                                        this.shouldStopNextTimeout = true;
                                    }
                                }
                            })
                            .subscribe();
                }
            } catch (Exception e) {
                Minecraft2Discord.logger.error(e);
            }
        }
    }

    private static class PresenceUpdateTask extends TimerTask {
        private final String presence_message;

        public PresenceUpdateTask(String presence_message) {
            this.presence_message = presence_message;
        }

        @Override
        public void run() {
            try {
                if (M2DUtils.canHandleEvent()) {
                    Minecraft2Discord.INSTANCE.client.updatePresence(Presence.online(Activity.playing(Entity.replace(presence_message, Collections.emptyList()))))
                            .timeout(Duration.ofSeconds(3))
                            .doOnError(throwable -> {
                                if (throwable instanceof TimeoutException) {
                                    Minecraft2Discord.logger.error("Seem that the presence is updated too quickly. Try increasing the update period");
                                    Minecraft2Discord.INSTANCE.errors.add("Seem that the presence is updated too quickly. Try increasing the update period");
                                    this.cancel();
                                }
                            })
                            .subscribe();
                }
            } catch (Exception e) {
                Minecraft2Discord.logger.error(e);
            }
        }
    }
}
