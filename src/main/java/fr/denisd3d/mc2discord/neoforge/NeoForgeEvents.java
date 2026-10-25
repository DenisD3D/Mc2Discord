package fr.denisd3d.mc2discord.neoforge;

//? if neoforge {
/*import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.minecraft.Entities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.EventPriority;

public class NeoForgeEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerChat(ServerChatEvent event) {
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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathEvent(LivingDeathEvent event) {
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
