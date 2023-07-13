package fr.denisd3d.mc2discord.core;

import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.LogEntity;
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

public class LoggingManager {
    public static final DiscordAppender da = DiscordAppender.createAppender("DiscordLogging", null);
    public static void init() {
        ((Logger) LogManager.getRootLogger()).addAppender(da);
        da.start();
    }

    public static void stop() {
        ((Logger) LogManager.getRootLogger()).removeAppender(da);
        da.stop();
    }

    @Plugin(name = "DiscordAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE)
    private static class DiscordAppender extends AbstractAppender {
        public static String logs = "";
        private long time;
        private Thread messageScheduler;

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
            if (M2DUtils.isNotConfigured())
                return;

            if (event.getLevel().intLevel() <= Level.getLevel(Mc2Discord.INSTANCE.config.misc.logs_level).intLevel()) {
                logs += Entity.replace(Mc2Discord.INSTANCE.config.misc.logs_format, Collections.singletonList(new LogEntity(event.getLoggerName(), event.getThreadName(), event.getInstant()
                        .getEpochMillisecond(), event.getLevel(), event.getMessage()))) + "\n";
                scheduleMessage();
            }
        }

        @SuppressWarnings("BusyWait")
        private void scheduleMessage() {
            time = System.currentTimeMillis();
            if (messageScheduler == null || !messageScheduler.isAlive()) {
                messageScheduler = new Thread(() -> {
                    while (true) {
                        if (System.currentTimeMillis() - time > 50) {
                            if (M2DUtils.isNotConfigured())
                                return;

                            MessageManager.sendMessage(Collections.singletonList("logs"), logs, MessageManager.default_username, MessageManager.default_avatar).subscribe();
                            logs = "";
                            break;
                        }
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            Mc2Discord.LOGGER.error("An error occurred while sending logs", e);
                        }
                    }
                });
                messageScheduler.start();
            }
        }
    }
}