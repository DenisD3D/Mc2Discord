package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class Utils
{
    public static TextChannel chatChannel;
    public static TextChannel infoChannel;
    public static TextChannel editableTopicChannel;

    public static Webhook discordWebhook;

    public static Map<String, Callable<String>> global_variables = new HashMap<>();
    public static long started_time;

    public static SimpleDateFormat uptimeDateFormater = new SimpleDateFormat("HH:mm");

    public static String globalVariableReplacement(String message)
    {
        String[] m = new String[1];
        m[0] = message;

        global_variables.forEach((name, getter) ->
        {
            try
            {
                m[0] = m[0].replace("$" + name + "$", getter.call());
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        return m[0];
    }

    public static boolean sendChatMessage(String message)
    {
        if (Config.SERVER.chatChannel.get() != 0L)
        {
            if (chatChannel == null)
                chatChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
            return sendMessage(chatChannel, message);
        }
        return false;
    }

    public static boolean sendInfoMessage(String message)
    {
        if (Config.SERVER.infoChannel.get() != 0L)
        {
            if (infoChannel == null)
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            return sendMessage(infoChannel, message);
        }
        return false;
    }

    public static boolean sendMessage(TextChannel channel, String message)
    {
        return sendMessage(channel, message, true);
    }

    public static boolean sendMessage(TextChannel channel, String message, Boolean global_variable_replacement)
    {
        if (Minecraft2Discord.getDiscordBot() == null || channel == null)
            return false;
        if (global_variable_replacement)
            message = globalVariableReplacement(message);

        try
        {
            channel.sendMessage(message).submit();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return true;
    }

    public static void updateDiscordPresence()
    {
        if (Config.SERVER.enableDiscordPresence.get() && Minecraft2Discord.getDiscordBot() != null)
        {
            Minecraft2Discord.getDiscordBot().getPresence().setActivity(Activity.playing(globalVariableReplacement(Config.SERVER.discordPresence.get())));
        }
    }

    public static void updateChannelTopic()
    {
        if (Config.SERVER.editableTopicChannel.get() != 0L && Minecraft2Discord.getDiscordBot() != null)
        {
            if (editableTopicChannel == null)
                editableTopicChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.editableTopicChannel.get());
            if (editableTopicChannel != null)
                editableTopicChannel.getManager().setTopic(Utils.globalVariableReplacement(Config.SERVER.editableChannelTopicUpdateMessage.get())).queue();
        }

        if (Config.SERVER.enableEditableChannelTopicUpdate.get() && Minecraft2Discord.getDiscordBot() != null)
        {
            Minecraft2Discord.getDiscordBot().getPresence().setActivity(Activity.playing(globalVariableReplacement(Config.SERVER.editableChannelTopicUpdateMessage.get())));
        }
    }

    public static boolean isM2DBotOrWebhook(User author, Webhook webhook)
    {
        if (author.getIdLong() == Minecraft2Discord.getDiscordBot().getSelfUser().getIdLong())
        {
            return true;
        } else return webhook != null && author.getIdLong() == webhook.getIdLong();
    }
}
