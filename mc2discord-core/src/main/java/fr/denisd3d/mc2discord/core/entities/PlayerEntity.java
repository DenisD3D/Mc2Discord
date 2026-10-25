package fr.denisd3d.mc2discord.core.entities;

import java.util.UUID;

public record PlayerEntity(String name, String displayName, UUID uuid) implements Entity {
    public PlayerEntity {
        displayName = displayName.replaceAll("§.", "");
    }

    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "player_name" -> name;
            case "player_display_name" -> displayName;
            case "player_uuid" -> uuid;
            default -> null;
        };
    }
}
