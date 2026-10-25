package fr.denisd3d.mc2discord.core.entities;

public record AdvancementEntity(String path, String displayText, String title, String description) implements Entity {
    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "advancement_path" -> path;
            case "advancement_display_text" -> displayText;
            case "advancement_title" -> title;
            case "advancement_description" -> description;
            default -> null;
        };
    }
}
