package fr.denisd3d.mc2discord.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class, (listeners) -> (player, source) -> {
        for (PlayerDeathCallback listener : listeners) {
            listener.onPlayerDeath(player, source);
        }
    });

    void onPlayerDeath(ServerPlayer playerEntity, DamageSource source);
}