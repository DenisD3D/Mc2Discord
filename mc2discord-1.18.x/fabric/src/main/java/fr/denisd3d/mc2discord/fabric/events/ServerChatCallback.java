package fr.denisd3d.mc2discord.fabric.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;

public interface ServerChatCallback {
    Event<ServerChatCallback> EVENT = EventFactory.createArrayBacked(ServerChatCallback.class, (listeners) -> (message, serverPlayer) -> {
        for (ServerChatCallback listener : listeners) {
            listener.onServerChatMessage(message, serverPlayer);
        }
    });

    void onServerChatMessage(String message, ServerPlayer serverPlayer);
}