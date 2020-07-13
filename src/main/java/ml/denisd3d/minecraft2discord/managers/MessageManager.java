package ml.denisd3d.minecraft2discord.managers;

import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.base.Splitter;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.variables.IParameterType;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import sun.misc.MessageUtils;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class MessageManager
{
    public static void sendMessage(TextChannel channel, String message, Boolean format, Map<IParameterType<?>, Object> parameters, Consumer<Message> success, Consumer<Throwable> failure, boolean useQuotesBlocks)
    {
        if (Minecraft2Discord.isRunning && channel != null)
        {
            if (format)
            {
                message = VariableManager.replace(message, parameters);
            }

            int currentBeginIndex = 0;

            while (currentBeginIndex < message.length() - 2001 - (useQuotesBlocks ? 6 : 0))
            {
                int currentEndIndex = getMessageEndIndex(message, currentBeginIndex, useQuotesBlocks);
                if (currentEndIndex != -1)
                {
                    String m = message.substring(currentBeginIndex, currentEndIndex);

                    sendTheMessage(channel, (useQuotesBlocks ? "```" : "") + m + (useQuotesBlocks ? "```" : ""), null, null);
                    currentBeginIndex = currentEndIndex;
                }
            }

            if (currentBeginIndex < message.length() - 1)
            {
                sendTheMessage(channel, (useQuotesBlocks ? "```" : "") + message.substring(currentBeginIndex, message.length() - 1) + (useQuotesBlocks ? "```" : ""), success, failure);
            }
        } else
        {
            if (failure != null)
            {
                failure.accept(null);
            }
        }
    }

    private static void sendTheMessage(TextChannel channel, String m, Consumer<Message> success, Consumer<Throwable> failure)
    {
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

                channel.sendMessage(new MessageBuilder(m).stripMentions(channel.getJDA()).build()).queue(success, failure);
            }
        } catch (Exception e)
        {
            Minecraft2Discord.getLogger().error(e);
            failure.accept(null);
        }
    }

    public static void sendMessage(TextChannel channel, String message, Boolean format, Map<IParameterType<?>, Object> parameters, Consumer<Message> success, Consumer<Throwable> failure)
    {
        sendMessage(channel, message, format, parameters, success, failure, false);
    }

    public static void sendQuotesMessage(TextChannel channel, String message, Boolean format, Map<IParameterType<?>, Object> parameters, Consumer<Message> success, Consumer<Throwable> failure)
    {
        sendMessage(channel, message, format, parameters, success, failure, true);
    }

    public static void sendMessage(TextChannel channel, String message, boolean format, Map<IParameterType<?>, Object> parameters)
    {
        sendMessage(channel, message, format, parameters, null, null);
    }

    public static void sendMessage(TextChannel channel, String message)
    {
        sendMessage(channel, message, false, null, null, null);
    }

    public static void sendQuotesMessage(TextChannel channel, String message)
    {
        sendQuotesMessage(channel, message, false, null, null, null);
    }

    public static void sendFormattedMessage(TextChannel channel, String message)
    {
        sendMessage(channel, message, true, new HashMap<>(), null, null);
    }

    public static void sendFormattedMessage(TextChannel channel, String message, Map<IParameterType<?>, Object> parameters)
    {
        sendMessage(channel, message, true, parameters, null, null);
    }

    public static int getMessageEndIndex(final String message, final int currentBeginIndex, boolean useQuotesBlocks)
    {
        int currentEndIndex = lastIndexOf("\n", currentBeginIndex, currentBeginIndex + 2000 - (useQuotesBlocks ? 6 : 0) - "\n".length(), message);
        if (currentEndIndex < 0)
        {
            return -1;
        }
        else
        {
            return currentEndIndex + "\n".length();
        }
    }

    public static int lastIndexOf(@Nonnull CharSequence target, int fromIndex, int endIndex, String message)
    {
        if (fromIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + fromIndex);
        if (endIndex < 0)
            throw new IndexOutOfBoundsException("index out of range: " + endIndex);
        if (fromIndex > message.length())
            throw new IndexOutOfBoundsException("fromIndex > length()");
        if (fromIndex > endIndex)
            throw new IndexOutOfBoundsException("fromIndex > endIndex");

        if (endIndex >= message.length())
        {
            endIndex = message.length() - 1;
        }

        int targetCount = target.length();
        if (targetCount == 0)
        {
            return endIndex;
        }

        int rightIndex = endIndex - targetCount;

        if (fromIndex > rightIndex)
        {
            fromIndex = rightIndex;
        }

        int strLastIndex = targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);

        int min = fromIndex + targetCount - 1;

        lastCharSearch:
        for (int i = endIndex; i >= min; i--)
        {
            if (message.charAt(i) == strLastChar)
            {
                for (int j = strLastIndex - 1, k = 1; j >= 0; j--, k++)
                {
                    if (message.charAt(i - k) != target.charAt(j))
                    {
                        continue lastCharSearch;
                    }
                }
                return i - target.length() + 1;
            }
        }
        return -1;
    }
}
