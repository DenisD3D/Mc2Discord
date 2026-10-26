package fr.denisd3d.mc2discord.fabric;

//? if fabric {
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.fabric.events.PlayerCompletedAdvancementCallback;
import fr.denisd3d.mc2discord.fabric.events.PlayerDeathCallback;
import fr.denisd3d.mc2discord.minecraft.Entities;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.damagesource.DamageSource;

public class FabricEvents {

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register(FabricEvents::onChatMessageEvent);
        ServerPlayConnectionEvents.JOIN.register(FabricEvents::onPlayerConnectEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(FabricEvents::onPlayerDisconnectEvent);
        PlayerDeathCallback.EVENT.register(FabricEvents::onPlayerDeathEvent);
        PlayerCompletedAdvancementCallback.EVENT.register(FabricEvents::onAdvancementEvent);
    }

    public static void onChatMessageEvent(PlayerChatMessage playerChatMessage, ServerPlayer serverPlayer, ChatType.Bound bound) {
        MinecraftEvents.onMinecraftChatMessageEvent(playerChatMessage.signedContent(), Entities.player(serverPlayer));
    }

    public static void onPlayerConnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerConnectEvent(Entities.player(serverGamePacketListener.getPlayer()));
    }

    public static void onPlayerDisconnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerDisconnectEvent(Entities.player(serverGamePacketListener.getPlayer()));
    }

    public static void onPlayerDeathEvent(ServerPlayer serverPlayer, DamageSource damageSource) {
        MinecraftEvents.onPlayerDeathEvent(Entities.player(serverPlayer),
                Entities.death(serverPlayer, damageSource, damageSource.getLocalizedDeathMessage(serverPlayer)));
    }

    public static void onAdvancementEvent(ServerPlayer serverPlayer, AdvancementHolder advancement) {
        if (advancement.value().display().isPresent() && advancement.value().display().get().announceToChat()) {
            MinecraftEvents.onAdvancementEvent(Entities.player(serverPlayer), Entities.advancement(advancement));
        }
    }
}
//?}
