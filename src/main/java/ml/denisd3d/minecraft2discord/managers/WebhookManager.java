package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import okhttp3.OkHttpClient;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class WebhookManager
{
    private static final HashMap<Long, WebhookClient> webhookClients = new HashMap<>();
    public static ScheduledExecutorService ses = Executors.newScheduledThreadPool(0);

    public static void addWebhookClient(long channelId, Runnable whenDone)
    {
        if (webhookClients.containsKey(channelId))
        {
            whenDone.run();
            return;
        }

        TextChannel channel = Minecraft2Discord.getDiscordBot().getTextChannelById(channelId);
        if (channel != null)
        {
            channel.retrieveWebhooks().queue(webhooks ->
            {
                Webhook webhook = webhooks.stream().filter(candidateWebhook -> candidateWebhook.getName().equals("Minecraft2Discord")).findFirst().orElse(null);
                if (webhook == null)
                {
                    channel.createWebhook("Minecraft2Discord").queue(createdWebhook ->
                    {
                        webhookClients.put(channelId, new WebhookClientBuilder(createdWebhook.getUrl()).setThreadFactory((job) -> {
                            Thread thread = new Thread(job);
                            thread.setName("M2D Webhook");
                            thread.setDaemon(true);
                            return thread;
                        }).setExecutorService(ses).setHttpClient(Minecraft2Discord.getDiscordBot().getHttpClient()).setDaemon(true).build());
                        if (whenDone != null)
                        {
                            whenDone.run();
                        }
                    });
                } else
                {
                    webhookClients.put(channelId, new WebhookClientBuilder(webhook.getUrl()).setThreadFactory((job) -> {
                        Thread thread = new Thread(job);
                        thread.setName("M2D Webhook");
                        thread.setDaemon(true);
                        return thread;
                    }).setExecutorService(ses).setHttpClient(Minecraft2Discord.getDiscordBot().getHttpClient()).setDaemon(true).build());
                    if (whenDone != null)
                    {
                        whenDone.run();
                    }
                }
            });
        }
    }

    public static HashMap<Long, WebhookClient> getWebhookClients()
    {
        return webhookClients;
    }

    public static WebhookClient getWebhookClient(long channelId)
    {
        return webhookClients.get(channelId);
    }
}
