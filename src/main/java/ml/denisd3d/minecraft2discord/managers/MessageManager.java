package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.base.Splitter;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.variables.IParameterType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MessageManager
{
    public static void sendMessage(TextChannel channel, String message, Boolean format, Map<IParameterType<?>, Object> parameters, Consumer<Message> success, Consumer<Throwable> failure)
    {
        if (Minecraft2Discord.isRunning && channel != null)
        {
            if (format)
            {
                message = VariableManager.replace(message, parameters);
            }

            for (String m : Splitter.fixedLength(2000).split(message))
            {
                if (m.trim().isEmpty())
                    break;
                try
                {
                    if (Config.SERVER.webhooksEnabled.get())
                    {
                        WebhookMessageBuilder builder = new WebhookMessageBuilder();
                        builder.setContent(VariableManager.messageVariables.get("message", m))
                            .setUsername(Minecraft2Discord.getUsername())
                            .setAvatarUrl(Minecraft2Discord.getAvatarURL());
                        if (ShutdownManager.isStop) // It is the shutdown message
                        {
                            if (WebhookManager.getWebhookClient(channel.getIdLong()) != null)
                                WebhookManager.getWebhookClient(channel.getIdLong()).send(builder.build()).join();
                        } else
                        {
                            if (WebhookManager.getWebhookClient(channel.getIdLong()) != null)
                                WebhookManager.getWebhookClient(channel.getIdLong()).send(builder.build());
                        }

                        if (success != null)
                        {
                            success.accept(null);
                        }
                    } else
                    {
                        channel.sendMessage(m).queue(success, failure);
                    }
                } catch (Exception e)
                {
                    Minecraft2Discord.getLogger().error(e);
                }
            }
        } else
        {
            if (failure != null)
            {
                failure.accept(null);
            }
        }
    }

    public static void sendMessage(TextChannel channel, String message, boolean format, Map<IParameterType<?>, Object> parameters)
    {
        sendMessage(channel, message, format, parameters, null, null);
    }

    public static void sendMessage(TextChannel channel, String message)
    {
        sendMessage(channel, message, false, null, null, null);
    }

    public static void sendFormattedMessage(TextChannel channel, String message)
    {
        sendMessage(channel, message, true, new HashMap<>(), null, null);
    }

    public static void sendFormattedMessage(TextChannel channel, String message, Map<IParameterType<?>, Object> parameters)
    {
        sendMessage(channel, message, true, parameters, null, null);
    }
}
