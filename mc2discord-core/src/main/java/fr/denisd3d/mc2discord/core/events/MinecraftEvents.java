package fr.denisd3d.mc2discord.core.events;

import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;

public class MinecraftEvents {
    public static void onMinecraftChatMessageEvent(String message, PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid()))
            return;

        MessageManager.sendChatMessage(message, player).subscribe();
    }

    public static void onPlayerConnectEvent(PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid()))
            return;
        MessageManager.sendInfoMessage("player_connect", Mc2Discord.INSTANCE.config.messages.join.render(player)).subscribe();
    }

    public static void onPlayerDisconnectEvent(PlayerEntity player) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid()))
            return;
        MessageManager.sendInfoMessage("player_disconnect", Mc2Discord.INSTANCE.config.messages.leave.render(player)).subscribe();
    }

    public static void onPlayerDeathEvent(PlayerEntity player, DeathEntity death) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid()))
            return;

        MessageManager.sendInfoMessage("player_death", Mc2Discord.INSTANCE.config.messages.death.render(player, death)).subscribe();
    }

    public static void onAdvancementEvent(PlayerEntity player, AdvancementEntity advancement) {
        if (M2DUtils.isNotConfigured())
            return;

        if (Mc2Discord.INSTANCE.hiddenPlayerList.contains(player.uuid()))
            return;

        MessageManager.sendInfoMessage("player_advancement", Mc2Discord.INSTANCE.config.messages.advancement.render(player, advancement)).subscribe();
    }
}
