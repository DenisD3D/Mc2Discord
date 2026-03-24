package fr.denisd3d.mc2discord.neoforge;

import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;

import java.util.Optional;

public class NeoForgeEvents {
        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onServerChat(ServerChatEvent event) {
                MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage().getString(),
                                new PlayerEntity(event.getPlayer().getGameProfile().name(), event.getPlayer()
                                                .getDisplayName()
                                                .getString(), event.getPlayer().getGameProfile().id()));
        }

        @SubscribeEvent
        public static void onPlayerConnectEvent(PlayerEvent.PlayerLoggedInEvent event) {
                MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(event.getEntity().getGameProfile().name(),
                                event.getEntity().getDisplayName().getString(),
                                event.getEntity().getGameProfile().id()));
        }

        @SubscribeEvent
        public static void onPlayerDisconnectEvent(PlayerEvent.PlayerLoggedOutEvent event) {
                MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(event.getEntity().getGameProfile().name(),
                                event.getEntity().getDisplayName().getString(),
                                event.getEntity().getGameProfile().id()));
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void onPlayerDeathEvent(LivingDeathEvent event) {
                if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
                        MinecraftEvents.onPlayerDeathEvent(
                                        new PlayerEntity(player.getGameProfile().name(),
                                                        player.getDisplayName().getString(),
                                                        player.getGameProfile().id()),
                                        new DeathEntity(event.getSource()
                                                        .getMsgId(),
                                                        player.getCombatTracker().getDeathMessage().getString(),
                                                        player.getCombatTracker().getCombatDuration(),
                                                        Optional.of(player.getCombatTracker().mob)
                                                                        .map(livingEntity -> livingEntity
                                                                                        .getDisplayName().getString())
                                                                        .orElse(""),
                                                        Optional.of(player.getCombatTracker().mob)
                                                                        .map(LivingEntity::getHealth).orElse(0.0f)));
                }
        }

        @SubscribeEvent
        public static void onAdvancementEvent(AdvancementEvent.AdvancementEarnEvent event) {
                if (event.getAdvancement().value().display().isPresent()
                                && event.getAdvancement().value().display().get().shouldAnnounceChat()) {
                        MinecraftEvents.onAdvancementEvent(
                                        new PlayerEntity(event.getEntity().getGameProfile().name(),
                                                        event.getEntity().getDisplayName().getString(),
                                                        event.getEntity().getGameProfile().id()),
                                        new AdvancementEntity(event.getAdvancement().id().toString(),
                                                        event.getAdvancement().value().name().map(Component::getString)
                                                                        .orElse(""),
                                                        event.getAdvancement().value().display().get().getTitle()
                                                                        .getString(),
                                                        event.getAdvancement().value().display().get().getDescription()
                                                                        .getString()));
                }
        }
}
