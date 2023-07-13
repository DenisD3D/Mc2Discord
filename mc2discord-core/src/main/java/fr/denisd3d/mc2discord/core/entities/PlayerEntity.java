package fr.denisd3d.mc2discord.core.entities;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerEntity extends Entity {
    private static final String prefix = "player_";

    public final String name;
    public final String displayName;
    public final UUID uuid;

    public PlayerEntity(String name, String displayName, UUID uuid) {
        this.name = name;
        this.displayName = displayName.replaceAll("§.", "");
        this.uuid = uuid;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put(prefix + "name", this.name);
        replacements.put(prefix + "display_name", this.displayName);
        replacements.put(prefix + "uuid", this.uuid != null ? this.uuid.toString() : "");
    }
}
