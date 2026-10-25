package fr.denisd3d.mc2discord.core;

import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.discordjson.json.ChannelModifyRequest;
import discord4j.discordjson.possible.Possible;
import fr.denisd3d.mc2discord.core.config.StatusChannels;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.time.Duration;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

public class StatusManager {
    private static Timer timer;
    private static boolean isCancelled = false;

    public static void init() {
        isCancelled = false;
        timer = new Timer(true);

        if (!Mc2Discord.INSTANCE.config.style.presence.message.isEmpty() && Mc2Discord.INSTANCE.config.style.presence.update != 0) {
            timer.schedule(new PresenceUpdateTask(Mc2Discord.INSTANCE.config.style.presence.message, Mc2Discord.INSTANCE.config.style.presence.type,
                    Mc2Discord.INSTANCE.config.style.presence.link), 0, Mc2Discord.INSTANCE.config.style.presence.update * 1000);
        }

        for (StatusChannels.StatusChannel statusChannel : Mc2Discord.INSTANCE.config.statusChannels.channels) {
            if (statusChannel.channel_id.equals(M2DUtils.NIL_SNOWFLAKE) || statusChannel.update_period == 0
                    || (statusChannel.name_message.isEmpty() && statusChannel.topic_message.isEmpty()))
                continue;
            timer.schedule(new ChannelUpdateTask(statusChannel), 0, statusChannel.update_period * 1000);
        }
    }

    public static void stop() {
        if (timer != null)
            timer.cancel();
        isCancelled = true;
    }

    static class ChannelUpdateTask extends TimerTask {
        private final StatusChannels.StatusChannel statusChannel;
        private boolean shouldStopNextTimeout = false;

        public ChannelUpdateTask(StatusChannels.StatusChannel statusChannel) {
            this.statusChannel = statusChannel;
        }

        @Override
        public void run() {
            try {
                if (M2DUtils.isNotConfigured()) // Check if mod is configured
                    return;

                if (isCancelled) // If mod is stopped
                    return;

                Template nameMessage = this.statusChannel.name_message.pick();
                Template topicMessage = this.statusChannel.topic_message.pick();

                Mc2Discord.INSTANCE.client.rest()
                        .getChannelById(this.statusChannel.channel_id)
                        .modify(ChannelModifyRequest.builder()
                                .name(!nameMessage.isEmpty() ? Possible.of(nameMessage.render()) : Possible.absent())
                                .topic(!topicMessage.isEmpty() ? Possible.of(topicMessage.render()) : Possible.absent())
                                .build(), null)
                        .timeout(Duration.ofSeconds(3))
                        .doOnError(throwable -> {
                            if (throwable instanceof TimeoutException) {
                                if (this.shouldStopNextTimeout) {
                                    Mc2Discord.LOGGER.error("Seem that the channel " + this.statusChannel.channel_id.asString()
                                            + " is updated too quickly. Try increasing the update period");
                                    Mc2Discord.INSTANCE.errors.add("Seem that the channel " + this.statusChannel.channel_id.asString()
                                            + " is updated too quickly. Try increasing the update period");
                                    this.cancel();
                                } else {
                                    this.shouldStopNextTimeout = true;
                                }
                            }
                        })
                        .subscribe();
            } catch (Exception e) {
                Mc2Discord.LOGGER.error("Error while updating channel " + this.statusChannel.channel_id.asString(), e);
            }
        }
    }

    private static class PresenceUpdateTask extends TimerTask {
        private final RandomTemplate presence_message;
        private final String presence_type;
        private final String presence_link;

        public PresenceUpdateTask(RandomTemplate presence_message, String presence_type, String presence_link) {
            this.presence_message = presence_message;
            this.presence_type = presence_type;
            this.presence_link = presence_link;
        }

        @Override
        public void run() {
            try {
                if (M2DUtils.isNotConfigured()) // Check if mod is configured
                    return;

                if (isCancelled) // If the mod is stopped
                    return;

                String message = this.presence_message.render();
                ClientActivity clientActivity = switch (this.presence_type) {
                    case "PLAYING" :
                    case "LISTENING" :
                    case "WATCHING" :
                        yield ClientActivity.of(Activity.Type.valueOf(presence_type), message, null);
                    case "STREAMING" :
                        yield ClientActivity.streaming(message, this.presence_link.isEmpty() ? null : this.presence_link);
                    case "CUSTOM" :
                        yield ClientActivity.custom(message);
                    default :
                        yield ClientActivity.playing(message);
                };
                Mc2Discord.INSTANCE.client.updatePresence(ClientPresence.online(clientActivity)).timeout(Duration.ofSeconds(3)).doOnError(throwable -> {
                    if (throwable instanceof TimeoutException) {
                        Mc2Discord.LOGGER.error("Seem that the presence is updated too quickly. Try increasing the update period");
                        Mc2Discord.INSTANCE.errors.add("Seem that the presence is updated too quickly. Try increasing the update period");
                        this.cancel();
                    }
                }).subscribe();
            } catch (Exception e) {
                Mc2Discord.LOGGER.error("Error while updating presence", e);
            }
        }
    }
}
