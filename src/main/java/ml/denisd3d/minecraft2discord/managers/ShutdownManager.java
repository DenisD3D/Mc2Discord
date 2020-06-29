package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.WebhookClient;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;

import java.util.HashMap;

public class ShutdownManager
{
    static boolean isStop = false;

    public static void onStop()
    {
        if (isStop || !Minecraft2Discord.isRunning)
            return;

        isStop = true;
        StatusManager.shutdown();
        if (Config.SERVER.startStopEnabled.get())
        {
            MessageManager.sendMessage(ChannelManager.getInfoChannel(), Config.SERVER.stopMessage.get(), true, new HashMap<>(), message -> ShutdownManager.shutdown(), throwable -> ShutdownManager.shutdown());
        } else
        {
            ShutdownManager.shutdown();
        }
    }

    public static void registerShutdownHook()
    {

        Runtime.getRuntime().addShutdownHook(new Thread(() ->
        {
            System.out.println("Shutdown hook called");
            if (isStop || !Minecraft2Discord.isRunning)
                return;

            isStop = true;
            StatusManager.shutdown();
            if (Config.SERVER.startStopEnabled.get())
            {
                MessageManager.sendMessage(ChannelManager.getInfoChannel(), Config.SERVER.crashMessage.get(), true, new HashMap<>(), message -> ShutdownManager.shutdown(), throwable -> ShutdownManager.shutdown());
            } else
            {
                ShutdownManager.shutdown();
            }
        }));
    }

    public static void shutdown()
    {
        try
        {
            Thread.sleep(1000); // Let previous action be executed => TODO : found a not blocking way
            Minecraft2Discord.isRunning = false;
            for (WebhookClient webhookClient : WebhookManager.getWebhookClients().values())
            {
                webhookClient.close();
            }
            Minecraft2Discord.getDiscordBot().shutdownNow();
        } catch (Exception e)
        {
            Minecraft2Discord.getLogger().warn("Shutdown forced");
        }
    }
}
