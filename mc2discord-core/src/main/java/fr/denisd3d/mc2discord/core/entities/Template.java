package fr.denisd3d.mc2discord.core.entities;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nullable;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A config text containing variables, parsed once when the config is loaded.
 * <ul>
 * <li>{@code ${name}} is replaced by the value of the variable</li>
 * <li>{@code ${name|format}} formats the value: a date pattern for dates, a duration pattern for durations, a {@link String#format} pattern for
 * numbers</li>
 * <li>"$${" is a literal "${"</li>
 * </ul>
 */
public final class Template {
    private static final Pattern PATTERN = Pattern.compile("\\$\\$\\{|\\$\\{([^}|]*)(?:\\|([^}]*))?}");
    private static final Set<String> invalidFormatsWarned = ConcurrentHashMap.newKeySet(); // Warn only once per invalid format, as logs are also formatted

    public sealed interface Part permits Text, Placeholder {
    }

    public record Text(String text) implements Part {
    }

    /**
     * @param source The placeholder as written in the config, kept in the output when no entity provides the variable
     */
    public record Placeholder(String name, @Nullable String format, String source) implements Part {
    }

    private final String source;
    private final List<Part> parts;

    private Template(String source, List<Part> parts) {
        this.source = source;
        this.parts = parts;
    }

    public static Template parse(String source) {
        List<Part> parts = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        Matcher matcher = PATTERN.matcher(source);
        int last = 0;
        while (matcher.find()) {
            text.append(source, last, matcher.start());
            last = matcher.end();
            if (matcher.group(1) == null) { // Escaped "$${"
                text.append("${");
                continue;
            }
            if (!text.isEmpty()) {
                parts.add(new Text(text.toString()));
                text.setLength(0);
            }
            parts.add(new Placeholder(matcher.group(1), matcher.group(2), matcher.group()));
        }
        text.append(source.substring(last));
        if (!text.isEmpty()) {
            parts.add(new Text(text.toString()));
        }
        return new Template(source, List.copyOf(parts));
    }

    public String source() {
        return source;
    }

    public boolean isEmpty() {
        return source.isEmpty();
    }

    public List<Part> parts() {
        return parts;
    }

    /**
     * Render the template as plain text. Global variables are always available.
     */
    public String render(Entity... entities) {
        return render(Arrays.asList(entities));
    }

    public String render(List<Entity> entities) {
        StringBuilder builder = new StringBuilder();
        for (Part part : parts) {
            switch (part) {
                case Text text -> builder.append(text.text());
                case Placeholder placeholder -> builder.append(resolve(placeholder, entities));
            }
        }
        return builder.toString();
    }

    /**
     * @return The formatted value of the placeholder, or its source if no entity provides the variable
     */
    public static String resolve(Placeholder placeholder, List<Entity> entities) {
        String value = value(placeholder.name(), placeholder.format(), entities);
        return value != null ? value : placeholder.source();
    }

    /**
     * @return The formatted value of the variable, or null if no entity provides it
     */
    @Nullable
    public static String value(String name, @Nullable String format, List<Entity> entities) {
        Object value = null;
        for (Entity entity : entities) {
            value = entity.get(name);
            if (value != null)
                break;
        }
        if (value == null) {
            value = GlobalEntity.INSTANCE.get(name);
            if (value == null)
                return null;
        }

        try {
            return format(value, format);
        } catch (RuntimeException e) {
            if (invalidFormatsWarned.add(name + "|" + format)) {
                Mc2Discord.LOGGER.warn("Invalid format for variable ${{}|{}}: {}", name, format, e.toString());
            }
            return format(value, null);
        }
    }

    private static String format(Object value, @Nullable String format) {
        return switch (value) {
            // Dates and durations are in milliseconds when not formatted
            case Instant instant -> format == null ? String.valueOf(instant.toEpochMilli()) : DateFormatUtils.format(instant.toEpochMilli(), format);
            case Duration duration -> format == null ? String.valueOf(duration.toMillis()) : DurationFormatUtils.formatDuration(duration.toMillis(), format);
            case Number number when format != null -> String.format(Locale.ROOT, format, number);
            case Double number -> String.format(Locale.ROOT, "%.3f", number); // Doubles have 3 decimals when not formatted
            default -> value.toString();
        };
    }

    @Override
    public String toString() {
        return source;
    }
}
