package fr.denisd3d.mc2discord.core.entities;


import fr.denisd3d.mc2discord.core.Mc2Discord;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Entity {
    public static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{(.*?)(?:\\|(.*?))?}");


    public static String replace(String content) {
        return replace(content, Collections.emptyList());
    }

    public static String replace(String content, List<Entity> entities) {
        Map<String, String> replacements = new HashMap<>();
        Map<String, BiFunction<String, String, String>> formatters = new HashMap<>();

        Mc2Discord.INSTANCE.minecraft.getServerData().getReplacements(replacements, formatters);

        for (Entity entity : entities) {
            entity.getReplacements(replacements, formatters);
        }

        return replace(content, replacements, formatters);
    }

    abstract void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters);

    private static String replace(String content, Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String replacement = replacements.get(matcher.group(1));

            if (matcher.group(2) != null) {
                replacement = formatters.getOrDefault(matcher.group(1), (format, value) -> value).apply(matcher.group(2), replacement);
            }

            if (replacement != null) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
            }
        }

        matcher.appendTail(builder);
        return builder.toString();
    }
}
