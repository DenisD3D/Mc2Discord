package fr.denisd3d.mc2discord.core.entities;


import fr.denisd3d.mc2discord.core.Mc2Discord;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Entity {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)(?:\\|(.*?))?}");
    private static final Set<String> invalidFormatsWarned = ConcurrentHashMap.newKeySet(); // Warn only once per invalid format, as logs are also formatted


    public static String replace(String content) {
        return replace(content, Collections.emptyList());
    }

    public static String replace(String content, List<Entity> entities) {
        return replace(content, entities, UnaryOperator.identity());
    }

    /**
     * Same as {@link #replace(String, List)} but for content that will then be parsed by the Minecraft formatter.
     * Each "$" of the replaced values is doubled so that values (e.g. a Discord message) can't inject formatting variables.
     * The Minecraft formatter turns "$$" back into "$".
     */
    public static String replaceForMinecraft(String content, List<Entity> entities) {
        return replace(content, entities, value -> value.replace("$", "$$"));
    }

    private static String replace(String content, List<Entity> entities, UnaryOperator<String> escaper) {
        Map<String, String> replacements = new HashMap<>();
        Map<String, BiFunction<String, String, String>> formatters = new HashMap<>();

        Mc2Discord.INSTANCE.minecraft.getServerData().getReplacements(replacements, formatters);

        for (Entity entity : entities) {
            entity.getReplacements(replacements, formatters);
        }

        return replace(content, replacements, formatters, escaper);
    }

    abstract void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters);

    private static String replace(String content, Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters, UnaryOperator<String> escaper) {
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));
            if (replacement != null) {
                replacement = escaper.apply(replacement);
            }

            if (matcher.group(2) != null) {
                try {
                    replacement = formatters.getOrDefault(matcher.group(1), (format, value) -> value).apply(matcher.group(2), replacement);
                } catch (RuntimeException e) {
                    if (invalidFormatsWarned.add(matcher.group())) {
                        Mc2Discord.LOGGER.warn("Invalid format for variable {}: {}", matcher.group(), e.toString());
                    }
                }
            }

            if (replacement != null) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(builder);
        return builder.toString();
    }
}
