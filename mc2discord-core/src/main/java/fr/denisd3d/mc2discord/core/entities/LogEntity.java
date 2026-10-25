package fr.denisd3d.mc2discord.core.entities;

import java.time.Instant;

public record LogEntity(String loggerName, String threadName, Instant time, String level, String message) implements Entity {
    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "log_logger_name" -> loggerName;
            case "log_thread_name" -> threadName;
            case "log_time" -> time;
            case "log_level" -> level;
            case "log_message" -> message;
            default -> null;
        };
    }
}
