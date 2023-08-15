package fr.denisd3d.mc2discord.core.events;

import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;

import java.util.List;

public class MinecraftEvents {
    public static void onMinecraftChatMessageEvent(String message, PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid))
            return;

        MessageManager.sendChatMessage(message, Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)), Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player))).subscribe();
    }

    public static void onPlayerConnectEvent(PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid))
            return;
        MessageManager.sendInfoMessage("player_connect", Entity.replace(Mc2Discord.INSTANCE.config.messages.join, List.of(player))).subscribe();
    }

    public static void onPlayerDisconnectEvent(PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid))
            return;
        MessageManager.sendInfoMessage("player_disconnect", Entity.replace(Mc2Discord.INSTANCE.config.messages.leave, List.of(player))).subscribe();
    }

    public static void onPlayerDeathEvent(PlayerEntity player, DeathEntity death) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid))
            return;

        MessageManager.sendInfoMessage("player_death", Entity.replace(Mc2Discord.INSTANCE.config.messages.death, List.of(player, death))).subscribe();
    }

    public static void onAdvancementEvent(PlayerEntity player, AdvancementEntity advancement) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid))
            return;

        MessageManager.sendInfoMessage("player_advancement", Entity.replace(Mc2Discord.INSTANCE.config.messages.advancement, List.of(player, advancement))).subscribe();
    }
}
