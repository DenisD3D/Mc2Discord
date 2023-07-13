package fr.denisd3d.mc2discord.core.entities;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.message.Message;

import java.util.Map;
import java.util.function.BiFunction;

public class LogEntity extends Entity {
    private static final String prefix = "log_";
    public final String loggerName;
    public final String threadName;
    public final long time;
    public final Level level;
    public final Message message;


    public LogEntity(String loggerName, String threadName, long time, Level level, Message message) {

        this.loggerName = loggerName;
        this.threadName = threadName;
        this.time = time;
        this.level = level;
        this.message = message;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put(prefix + "logger_name", this.loggerName.substring(this.loggerName.lastIndexOf(".") + 1));
        replacements.put(prefix + "thread_name", this.threadName);
        replacements.put(prefix + "time", String.valueOf(this.time));
        replacements.put(prefix + "level", this.level.name());
        replacements.put(prefix + "message", this.message.getFormattedMessage());

        formatters.put(prefix + "time", (format, value) -> DateFormatUtils.format(Long.parseLong(value), format));

    }
}

