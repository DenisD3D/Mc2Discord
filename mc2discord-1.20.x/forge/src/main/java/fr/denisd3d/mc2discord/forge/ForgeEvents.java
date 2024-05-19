package fr.denisd3d.mc2discord.forge;

import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class ForgeEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerChat(ServerChatEvent event) {
        if (event.isCanceled())
            return;

        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage().getString(), new PlayerEntity(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                .getDisplayName()
                .getString(), event.getPlayer().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerConnectEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(event.getEntity().getGameProfile().getName(), event.getEntity().getDisplayName().getString(), event.getEntity().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerDisconnectEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(event.getEntity().getGameProfile().getName(), event.getEntity().getDisplayName().getString(), event.getEntity().getGameProfile().getId()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathEvent(LivingDeathEvent event) {
        if (event.isCanceled())
            return;

        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            MinecraftEvents.onPlayerDeathEvent(new PlayerEntity(player.getGameProfile().getName(), player.getDisplayName().getString(), player.getGameProfile().getId()), new DeathEntity(event.getSource()
                    .getMsgId(), player.getCombatTracker().getDeathMessage().getString(), player.getCombatTracker().getCombatDuration(), Optional.of(player.getCombatTracker().mob)
                    .map(livingEntity -> livingEntity.getDisplayName().getString())
                    .orElse(""), Optional.of(player.getCombatTracker().mob).map(LivingEntity::getHealth).orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getAdvancement().value().display().isPresent() && event.getAdvancement().value().display().get().shouldAnnounceChat()) {
            MinecraftEvents.onAdvancementEvent(
                    new PlayerEntity(event.getEntity().getGameProfile().getName(),
                            event.getEntity().getDisplayName().getString(),
                            event.getEntity().getGameProfile().getId()),
                    new AdvancementEntity(event.getAdvancement().id().toString(),
                            event.getAdvancement().value().name().map(Component::getString).orElse(""),
                            event.getAdvancement().value().display().get().getTitle().getString(),
                            event.getAdvancement().value().display().get().getDescription().getString()));
        }
    }
}
