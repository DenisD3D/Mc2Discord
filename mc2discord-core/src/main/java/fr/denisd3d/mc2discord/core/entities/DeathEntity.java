package fr.denisd3d.mc2discord.core.entities;

import java.time.Duration;

/**
 * @param attackerName   Empty if the death wasn't caused by an entity
 * @param attackerHealth 0 if the death wasn't caused by a living entity
 */
public record DeathEntity(String damageType, String message, Duration combatDuration, String attackerName, float attackerHealth) implements Entity {
    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "death_damage_type" -> damageType;
            case "death_message" -> message;
            case "death_combat_duration" -> combatDuration;
            case "death_attacker_name" -> attackerName;
            case "death_attacker_health" -> attackerHealth;
            default -> null;
        };
    }
}
