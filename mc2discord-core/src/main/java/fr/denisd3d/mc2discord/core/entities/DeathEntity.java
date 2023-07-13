package fr.denisd3d.mc2discord.core.entities;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Map;
import java.util.function.BiFunction;

public class DeathEntity extends Entity {
    private static final String prefix = "death_";
    public final String damageType;
    public final String message;
    public final int combatDuration;
    public final String attackerName;
    public final float attackerHealth;

    public DeathEntity(String damageType, String message, int combatDuration, String attackerName, float attackerHealth) {
        this.damageType = damageType;
        this.message = message;
        this.combatDuration = combatDuration;
        this.attackerName = attackerName;
        this.attackerHealth = attackerHealth;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put(prefix + "damage_type", this.damageType);
        replacements.put(prefix + "message", this.message);
        replacements.put(prefix + "combat_duration", Integer.toString(this.combatDuration));
        replacements.put(prefix + "attacker_name", this.attackerName);
        replacements.put(prefix + "attacker_health", Float.toString(this.attackerHealth));

        formatters.put(prefix + "combat_duration", (format, value) -> DurationFormatUtils.formatDuration(Long.parseLong(value), format));

    }
}
