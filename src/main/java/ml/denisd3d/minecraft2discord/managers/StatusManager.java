package ml.denisd3d.minecraft2discord.managers;

import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;

import java.util.concurrent.*;

public class StatusManager
{
    public static ScheduledExecutorService ses = Executors.newScheduledThreadPool(1);
    public static ScheduledFuture<?> scheduledFuturePresence;
    public static ScheduledFuture<?> scheduledFutureTopic;
    public static ScheduledFuture<?> scheduledFutureChannelName;

    public static void register()
    {
        if (Config.SERVER.presenceEnabled.get())
        {
            scheduledFuturePresence = ses.scheduleAtFixedRate(() -> updatePresence(VariableManager.replace(Config.SERVER.presenceMessage.get())), 0, Config.SERVER.presenceUpdatePeriod.get(), TimeUnit.SECONDS);
        }

        if (Config.SERVER.topicEnabled.get())
        {
            scheduledFutureTopic = ses.scheduleAtFixedRate(() -> updateTopic(VariableManager.replace(Config.SERVER.topicMessage.get())), 0, Config.SERVER.topicUpdatePeriod.get(), TimeUnit.SECONDS);
        }

        if (Config.SERVER.nameEnabled.get())
        {
            scheduledFutureChannelName = ses.scheduleAtFixedRate(() -> updateName(VariableManager.replace(Config.SERVER.nameMessage.get())), 0, Config.SERVER.nameUpdatePeriod.get(), TimeUnit.SECONDS);
        }
    }

    public static void updatePresence(String message)
    {
        Minecraft2Discord.getDiscordBot().getPresence().setActivity(Activity.playing(message));
    }

    public static void updateTopic(String message)
    {
        if (ChannelManager.getTopicChannel() != null)
            ChannelManager.getTopicChannel().getManager().setTopic(message).queue();
    }

    public static void updateName(String message)
    {
        if (ChannelManager.getNameChannel() != null)
            ChannelManager.getNameChannel().getManager().setName(ChannelManager.getNameChannel().getType() == ChannelType.TEXT ? message.replace(" ", "-") : message).queue();
    }

    public static void shutdown()
    {
        if (scheduledFuturePresence != null)
        {
            scheduledFuturePresence.cancel(true);
        }
        if (scheduledFutureTopic != null)
        {
            scheduledFutureTopic.cancel(true);
        }
        if (scheduledFutureChannelName != null)
        {
            scheduledFutureChannelName.cancel(true);
        }
        if (ses != null)
        {
            ses.shutdownNow();
        }
    }
}
