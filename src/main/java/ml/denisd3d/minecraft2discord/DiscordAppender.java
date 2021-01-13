package ml.denisd3d.minecraft2discord;

import ml.denisd3d.minecraft2discord.managers.ChannelManager;
import ml.denisd3d.minecraft2discord.managers.MessageManager;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Plugin(name = "DiscordAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class DiscordAppender extends AbstractAppender {
    public static String logs = "";

    protected DiscordAppender(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static DiscordAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new DiscordAppender(name, filter);
    }

    @Override
    public void append(LogEvent event) {
        if (event.getLevel().intLevel() <= Level.getLevel("INFO").intLevel()) {
            boolean init_send = logs.equals("");
            logs += "[SERVER | " + DateFormatUtils.format(event.getInstant().getEpochMillisecond(), Config.SERVER.dateFormat.get()) + " | " + event.getLevel().toString() + "] ---> " + event.getMessage().getFormattedMessage() + "\n";

            if (init_send)
            {
                sendLogMessage();
            }
        }
    }

    private void sendLogMessage() {
        if (logs.equals("") || Minecraft2Discord.getDiscordBot() == null)
            return;

        int end_index = MessageManager.getMessageEndIndex(logs, 0, false);
        Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.consoleChannel.get()).sendMessage(logs.substring(0, end_index)).queue(message -> sendLogMessage()); // TODO : More check here + move in channel manager
        logs = logs.substring(end_index);
    }
}
