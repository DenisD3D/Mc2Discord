package fr.denisd3d.mc2discord.core;

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

import java.time.Instant;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
        private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Mc2Discord logs");
            thread.setDaemon(true);
            return thread;
        });
        private final StringBuilder logs = new StringBuilder();
        private ScheduledFuture<?> flushTask;

        protected DiscordAppender(String name, Filter filter) {
            super(name, filter, null, true, Property.EMPTY_ARRAY);
        }

        @PluginFactory
        public static DiscordAppender createAppender(@PluginAttribute("name") String name, @PluginElement("Filter") Filter filter) {
            return new DiscordAppender(name, filter);
        }

        @Override
        public void append(LogEvent event) {
            if (M2DUtils.isNotConfigured())
                return;

            if (event.getLevel().intLevel() <= Level.getLevel(Mc2Discord.INSTANCE.config.misc.logs_level).intLevel()) {
                String loggerName = event.getLoggerName().substring(event.getLoggerName().lastIndexOf(".") + 1);
                String line = Mc2Discord.INSTANCE.config.misc.logs_format.render(new LogEntity(loggerName, event.getThreadName(),
                        Instant.ofEpochMilli(event.getInstant().getEpochMillisecond()), event.getLevel().name(), event.getMessage().getFormattedMessage()));
                // Called from any thread
                synchronized (this) {
                    logs.append(line).append("\n");
                    scheduleFlush();
                }
            }
        }

        /**
         * Send the logs once nothing has been logged for 50ms
         */
        private synchronized void scheduleFlush() {
            if (flushTask != null) {
                flushTask.cancel(false);
            }
            flushTask = SCHEDULER.schedule(this::flush, 50, TimeUnit.MILLISECONDS);
        }

        private void flush() {
            String message;
            synchronized (this) {
                message = logs.toString();
                logs.setLength(0);
            }
            if (M2DUtils.isNotConfigured())
                return;

            MessageManager.sendMessage(Collections.singletonList("logs"), message, MessageManager.default_username, MessageManager.default_avatar).subscribe();
        }
    }
}
