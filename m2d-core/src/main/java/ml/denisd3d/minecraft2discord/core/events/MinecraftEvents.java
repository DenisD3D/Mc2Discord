package ml.denisd3d.minecraft2discord.core.events;

import ml.denisd3d.minecraft2discord.core.M2DUtils;
import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.core.entities.*;

import java.util.Arrays;
import java.util.Collections;

public class MinecraftEvents {
    public static void onMinecraftChatMessageEvent(String message, Player player) {
        if (!M2DUtils.canHandleEvent() || Minecraft2Discord.INSTANCE.iMinecraft.isPlayerHidden(player.uuid, player.name))
            return;
        Minecraft2Discord.INSTANCE.messageManager.sendChatMessage(message, Entity.replace(Minecraft2Discord.INSTANCE.config.discord_chat_format, Arrays.asList(player, new Message(message))),
                player.displayName,
                Entity.replace(Minecraft2Discord.INSTANCE.config.avatar_api, Collections.singletonList(player)));
    }

    public static void onPlayerJoinEvent(Player player) {
        if (!M2DUtils.canHandleEvent() || Minecraft2Discord.INSTANCE.iMinecraft.isPlayerHidden(player.uuid, player.name))
            return;
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.join_message, Collections.singletonList(player)));
    }

    public static void onPlayerLeaveEvent(Player player) {
        if (!M2DUtils.canHandleEvent() || Minecraft2Discord.INSTANCE.iMinecraft.isPlayerHidden(player.uuid, player.name))
            return;
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.leave_message, Collections.singletonList(player)));
    }

    public static void onPlayerDieEvent(Player player, Death death) {
        if (!M2DUtils.canHandleEvent() || Minecraft2Discord.INSTANCE.iMinecraft.isPlayerHidden(player.uuid, player.name))
            return;
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.death_message, Arrays.asList(player, death)));
    }

    public static void onAdvancementEvent(Player player, Advancement advancement) {
        if (!M2DUtils.canHandleEvent() || Minecraft2Discord.INSTANCE.iMinecraft.isPlayerHidden(player.uuid, player.name))
            return;
        Minecraft2Discord.INSTANCE.messageManager.sendInfoMessage(Entity.replace(Minecraft2Discord.INSTANCE.config.advancement_message, Arrays.asList(player, advancement)));
    }
}
