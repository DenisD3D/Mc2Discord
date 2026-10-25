package fr.denisd3d.mc2discord.forge;

//? if forge {
/*import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.minecraft.Entities;
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
        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage().getString(), Entities.player(event.getPlayer()));
    }

    @SubscribeEvent
    public static void onPlayerConnectEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerConnectEvent(Entities.player(event.getEntity()));
    }

    @SubscribeEvent
    public static void onPlayerDisconnectEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerDisconnectEvent(Entities.player(event.getEntity()));
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
            MinecraftEvents.onPlayerDeathEvent(Entities.player(player), Entities.death(player, event.getSource(), player.getCombatTracker().getDeathMessage()));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getAdvancement().value().display().isPresent() && event.getAdvancement().value().display().get().announceToChat()) {
            MinecraftEvents.onAdvancementEvent(Entities.player(event.getEntity()), Entities.advancement(event.getAdvancement()));
        }
    }
}
*///?}
