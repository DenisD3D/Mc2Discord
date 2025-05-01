package fr.denisd3d.mc2discord.fabric;

import com.mojang.brigadier.ParseResults;
import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.fabric.events.CommandExecuteCallback;
import fr.denisd3d.mc2discord.fabric.events.PlayerCompletedAdvancementCallback;
import fr.denisd3d.mc2discord.fabric.events.PlayerDeathCallback;
import fr.denisd3d.mc2discord.fabric.events.ServerChatCallback;
import fr.denisd3d.mc2discord.minecraft.Mc2DiscordMinecraft;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class FabricEvents {

    public static void register() {
        ServerChatCallback.EVENT.register(FabricEvents::onChatMessageEvent);
        ServerPlayConnectionEvents.JOIN.register(FabricEvents::onPlayerConnectEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(FabricEvents::onPlayerDisconnectEvent);
        PlayerDeathCallback.EVENT.register(FabricEvents::onPlayerDeathEvent);
        PlayerCompletedAdvancementCallback.EVENT.register(FabricEvents::onAdvancementEvent);
        CommandExecuteCallback.EVENT.register(FabricEvents::onCommandEvent);
    }

    public static void onChatMessageEvent(String message, ServerPlayer serverPlayer) {
        MinecraftEvents.onMinecraftChatMessageEvent(message, new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId()));
    }

    public static void onPlayerConnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(serverGamePacketListener.getPlayer().getGameProfile().getName(), serverGamePacketListener.getPlayer().getDisplayName().getString(), serverGamePacketListener.getPlayer().getGameProfile().getId()));
    }

    public static void onPlayerDisconnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(serverGamePacketListener.getPlayer().getGameProfile().getName(), serverGamePacketListener.getPlayer().getDisplayName().getString(), serverGamePacketListener.getPlayer().getGameProfile().getId()));
    }

    public static void onPlayerDeathEvent(ServerPlayer serverPlayer, DamageSource damageSource) {
        MinecraftEvents.onPlayerDeathEvent(new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId()), new DeathEntity(damageSource.getMsgId(), damageSource.getLocalizedDeathMessage(serverPlayer).getString(), serverPlayer.getCombatTracker().getCombatDuration(), Optional.of(serverPlayer.getCombatTracker().mob).map(livingEntity -> livingEntity.getDisplayName().getString()).orElse(""), Optional.of(serverPlayer.getCombatTracker().mob).map(LivingEntity::getHealth).orElse(0.0f)));
    }

    public static void onAdvancementEvent(ServerPlayer serverPlayer, Advancement advancement) {
        if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceChat()) {
            MinecraftEvents.onAdvancementEvent(new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId()), new AdvancementEntity(advancement.getId().getPath(), advancement.getChatComponent().getString(), advancement.getDisplay().getTitle().getString(), advancement.getDisplay().getDescription().getString()));
        }
    }

    /**
     * Commands fixes for Discord
     */
    public static InteractionResult onCommandEvent(ParseResults<CommandSourceStack> parseResults) {
        Mc2DiscordMinecraft.onCommand(parseResults);
        return InteractionResult.PASS;
    }
}
