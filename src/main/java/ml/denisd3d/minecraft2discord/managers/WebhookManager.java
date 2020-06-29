package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;

import java.util.HashMap;

public class WebhookManager
{
    private static final HashMap<Long, WebhookClient> webhookClients = new HashMap<>();

    public static void addWebhookClient(long channelId, Runnable whenDone)
    {
        if (webhookClients.containsKey(channelId))
        {
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
                        webhookClients.put(channelId, new WebhookClientBuilder(createdWebhook.getUrl()).setDaemon(true).build());
                        if (whenDone != null)
                        {
                            whenDone.run();
                        }
                    });
                } else
                {
                    webhookClients.put(channelId, new WebhookClientBuilder(webhook.getUrl()).setDaemon(true).build());
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
