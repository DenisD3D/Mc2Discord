package ml.denisd3d.minecraft2discord.core;

import ml.denisd3d.minecraft2discord.core.config.M2DConfig;
import ml.denisd3d.minecraft2discord.core.entities.Entity;
import ml.denisd3d.minecraft2discord.core.entities.Log;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;

import java.util.Collections;

@Plugin(name = "DiscordLogging", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
public class DiscordLogging extends AbstractAppender {
    public static String logs = "";
    private long time;
    private Thread messageScheduler;

    protected DiscordLogging(String name, Filter filter) {
        super(name, filter, null, true, Property.EMPTY_ARRAY);
    }

    @PluginFactory
    public static DiscordLogging createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter) {
        return new DiscordLogging(name, filter);
    }

    public static void init() {
        LoggerContext lc = (LoggerContext) LogManager.getContext(false);
        DiscordLogging da = DiscordLogging.createAppender("DiscordLogging", null);
        da.start();
        lc.getConfiguration().addAppender(da);
        for (Logger logger : lc.getLoggers()) {
            if (!logger.getName().startsWith("discord4j.limitter"))
                logger.addAppender(lc.getConfiguration().getAppender(da.getName()));
        }
        lc.updateLoggers();
    }

    @Override
    public void append(LogEvent event) {
        if (M2DUtils.canHandleEvent() && event.getLevel().intLevel() <= Level.getLevel("INFO").intLevel()) { // TODO: log level in config
            logs += Entity.replace(M2DConfig.logs_format, Collections.singletonList(new Log(event.getLoggerName(), event.getThreadName(), event.getInstant().getEpochMillisecond(), event.getLevel(), event.getMessage()))) + "\n";
            scheduleMessage();
        }
    }

    private void scheduleMessage() {
        time = System.currentTimeMillis();
        if (messageScheduler == null || !messageScheduler.isAlive()) {
            messageScheduler = new Thread(() -> {
                while (true) {
                    if (System.currentTimeMillis() - time > 50) {
                        Minecraft2Discord.INSTANCE.messageManager.sendMessageOfType("log", logs, "", Minecraft2Discord.INSTANCE.botDisplayName, Minecraft2Discord.INSTANCE.botAvatar, null);

                        logs = "";
                        break;
                    }
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Minecraft2Discord.logger.error(e);
                    }
                }
            });
            messageScheduler.start();
        }
    }
}