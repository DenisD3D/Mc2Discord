package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.requests.ErrorResponse;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WebhookManager {
    private static final HashMap<Long, WebhookClient> webhookClients = new HashMap<>();
    public static ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);

    public static void initInfoWebhook(Runnable whenDone) {

        Minecraft2Discord.getDiscordBot().retrieveWebhookById(Config.SERVER.infoWebhookId.get())
                .onErrorFlatMap(ErrorResponse.UNKNOWN_WEBHOOK::test,
                        throwable -> Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get()).createWebhook("Minecraft2Discord")).queue(infoWebhook -> {
            if (!Config.SERVER.infoWebhookId.get().equals(infoWebhook.getIdLong())) {
                Config.SERVER.infoWebhookId.set(infoWebhook.getIdLong());
            }
            createWebhook(Config.SERVER.infoChannel.get(), infoWebhook);

            if (whenDone != null)
                whenDone.run();
        });
    }

    public static void initChatWebhook(Runnable whenDone) {

        Minecraft2Discord.getDiscordBot().retrieveWebhookById(Config.SERVER.chatWebhookId.get())
                .onErrorFlatMap(ErrorResponse.UNKNOWN_WEBHOOK::test,
                        throwable -> Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get()).createWebhook("Minecraft2Discord")).queue(chatWebhook -> {
            if (!Config.SERVER.chatWebhookId.get().equals(chatWebhook.getIdLong())) {
                Config.SERVER.chatWebhookId.set(chatWebhook.getIdLong());
            }
            createWebhook(Config.SERVER.chatChannel.get(), chatWebhook);

            if (whenDone != null)
                whenDone.run();
        });
    }

    public static void createWebhook(long channelId, Webhook createdWebhook) {
        webhookClients.put(channelId, new WebhookClientBuilder(createdWebhook.getUrl()).setThreadFactory((job) -> {
            Thread thread = new Thread(job);
            thread.setName("M2D Webhook");
            thread.setDaemon(true);
            return thread;
        }).setExecutorService(ses).setHttpClient(Minecraft2Discord.getDiscordBot().getHttpClient()).setDaemon(true).build());
    }

    public static HashMap<Long, WebhookClient> getWebhookClients() {
        return webhookClients;
    }

    public static WebhookClient getWebhookClient(long channelId) {
        return webhookClients.get(channelId);
    }
}
