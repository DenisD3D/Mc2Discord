package fr.denisd3d.mc2discord.forge;

//? if forge {
/*import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
//? if >=1.21.6 {
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//?} else {
/^import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
^///?}

import java.util.Optional;

public class ForgeEvents {
    //? if >=1.21.6 {
    @SubscribeEvent(priority = Priority.LOWEST)
    //?} else {
    /^@SubscribeEvent(priority = EventPriority.LOWEST)
    ^///?}
    public static void onServerChat(ServerChatEvent event) {
        //? if <1.21.6 {
        /^if (event.isCanceled())
            return;
        ^///?}
        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage().getString(), new PlayerEntity(event.getPlayer().getGameProfile().name(),
                event.getPlayer().getDisplayName().getString(), event.getPlayer().getGameProfile().id()));
    }

    @SubscribeEvent
    public static void onPlayerConnectEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(event.getEntity().getGameProfile().name(), event.getEntity().getDisplayName().getString(),
                event.getEntity().getGameProfile().id()));
    }

    @SubscribeEvent
    public static void onPlayerDisconnectEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(event.getEntity().getGameProfile().name(), event.getEntity().getDisplayName().getString(),
                event.getEntity().getGameProfile().id()));
    }

    //? if >=1.21.6 {
    @SubscribeEvent(priority = Priority.LOWEST)
    //?} else {
    /^@SubscribeEvent(priority = EventPriority.LOWEST)
    ^///?}
    public static void onPlayerDeathEvent(LivingDeathEvent event) {
        //? if <1.21.6 {
        /^if (event.isCanceled())
            return;
        ^///?}
        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            MinecraftEvents.onPlayerDeathEvent(
                    new PlayerEntity(player.getGameProfile().name(), player.getDisplayName().getString(), player.getGameProfile().id()),
                    new DeathEntity(event.getSource().getMsgId(), player.getCombatTracker().getDeathMessage().getString(),
                            player.getCombatTracker().getCombatDuration(),
                            Optional.ofNullable(event.getSource().getEntity()).map(attacker -> attacker.getDisplayName().getString()).orElse(""),
                            Optional.ofNullable(event.getSource().getEntity())
                                    .filter(LivingEntity.class::isInstance)
                                    .map(attacker -> ((LivingEntity) attacker).getHealth())
                                    .orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getAdvancement().value().display().isPresent() && event.getAdvancement().value().display().get().announceToChat()) {
            MinecraftEvents.onAdvancementEvent(
                    new PlayerEntity(event.getEntity().getGameProfile().name(), event.getEntity().getDisplayName().getString(),
                            event.getEntity().getGameProfile().id()),
                    new AdvancementEntity(event.getAdvancement().id().toString(), event.getAdvancement().value().name().map(Component::getString).orElse(""),
                            event.getAdvancement().value().display().get().title().getString(),
                            event.getAdvancement().value().display().get().description().getString()));
        }
    }
}
*///?}
