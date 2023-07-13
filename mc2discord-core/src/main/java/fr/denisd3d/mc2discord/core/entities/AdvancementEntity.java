package fr.denisd3d.mc2discord.core.entities;

import java.util.Map;
import java.util.function.BiFunction;

public class AdvancementEntity extends Entity {
    private static final String prefix = "advancement_";
    private final String path;
    private final String displayText;
    private final String title;
    private final String description;

    public AdvancementEntity(String path, String displayText, String title, String description) {

        this.path = path;
        this.displayText = displayText;
        this.title = title;
        this.description = description;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put(prefix + "path", this.path);
        replacements.put(prefix + "display_text", this.displayText);
        replacements.put(prefix + "title", this.title);
        replacements.put(prefix + "description", this.description);
    }
}
