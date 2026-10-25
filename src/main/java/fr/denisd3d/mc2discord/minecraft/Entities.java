package fr.denisd3d.mc2discord.minecraft;

import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.time.Duration;

/**
 * Create the core entities from Minecraft objects
 */
public final class Entities {
    private Entities() {
    }

    public static PlayerEntity player(Player player) {
        return new PlayerEntity(player.getGameProfile().name(), player.getDisplayName().getString(), player.getGameProfile().id());
    }

    public static DeathEntity death(Player player, DamageSource source, Component deathMessage) {
        Entity attacker = source.getEntity();
        return new DeathEntity(source.getMsgId(), deathMessage.getString(),
                Duration.ofMillis(player.getCombatTracker().getCombatDuration() * 50L), // 50 ms per tick
                attacker != null ? attacker.getDisplayName().getString() : "",
                attacker instanceof LivingEntity livingAttacker ? livingAttacker.getHealth() : 0.0f);
    }

    /**
     * The advancement must have a display
     */
    public static AdvancementEntity advancement(AdvancementHolder advancement) {
        return new AdvancementEntity(advancement.id().toString(), advancement.value().name().map(Component::getString).orElse(""),
                advancement.value().display().get().title().getString(), advancement.value().display().get().description().getString());
    }
}
