package fr.denisd3d.mc2discord.fabric.events;

//? if fabric {
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerCompletedAdvancementCallback {
    Event<PlayerCompletedAdvancementCallback> EVENT = EventFactory.createArrayBacked(PlayerCompletedAdvancementCallback.class, (listeners) -> (player, advancement) -> {
        for (PlayerCompletedAdvancementCallback listener : listeners) {
            listener.onPlayerAdvancement(player, advancement);
        }
    });

    void onPlayerAdvancement(ServerPlayer playerEntity, AdvancementHolder advancement);
}
//?}
