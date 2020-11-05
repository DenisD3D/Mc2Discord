package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.WebhookClient;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;

import java.util.HashMap;

public class ShutdownManager {
    static boolean isStop = false;

    public static void stopping() {
        StatusManager.shutdown();

        if (Config.SERVER.startStopEnabled.get()) {
            MessageManager.sendMessage(ChannelManager.getInfoChannel(), Config.SERVER.stopMessage.get(), true, new HashMap<>());
        }
    }

    public static void stopped() {
        if (isStop)
            return;
        isStop = true;
        ShutdownManager.shutdown();
    }

    public static void registerShutdownHook() {

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            if (isStop)
                return;

            System.out.println("Shutdown hook used");

            isStop = true;
            StatusManager.shutdown();
            if (Config.SERVER.startStopEnabled.get()) {
                MessageManager.sendMessage(ChannelManager.getInfoChannel(), Config.SERVER.crashMessage.get(), true, new HashMap<>(), message -> ShutdownManager.shutdown(), throwable -> ShutdownManager.shutdown());
            } else {
                ShutdownManager.shutdown();
            }
        }));
    }

    public static void shutdown() {
        try {
            for (WebhookClient webhookClient : WebhookManager.getWebhookClients().values()) {
                webhookClient.close();
            }
            WebhookManager.ses.shutdown();

            Minecraft2Discord.getDiscordBot().shutdown();
        } catch (Exception e) {
            Minecraft2Discord.getLogger().error("Shutdown Error");
            e.printStackTrace();
        }
    }
}
