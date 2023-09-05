package fr.denisd3d.mc2discord.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;

public interface PlayerCompletedAdvancementCallback {
    Event<PlayerCompletedAdvancementCallback> EVENT = EventFactory.createArrayBacked(PlayerCompletedAdvancementCallback.class, (listeners) -> (player, advancement) -> {
        for (PlayerCompletedAdvancementCallback listener : listeners) {
            listener.onPlayerAdvancement(player, advancement);
        }
    });

    void onPlayerAdvancement(ServerPlayer playerEntity, Advancement advancement);
}