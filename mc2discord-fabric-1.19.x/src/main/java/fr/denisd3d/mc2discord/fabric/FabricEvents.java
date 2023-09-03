package fr.denisd3d.mc2discord.fabric;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.entities.AdvancementEntity;
import fr.denisd3d.mc2discord.core.entities.DeathEntity;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.MinecraftEvents;
import fr.denisd3d.mc2discord.fabric.events.CommandExecuteCallback;
import fr.denisd3d.mc2discord.fabric.events.PlayerCompletedAdvancementCallback;
import fr.denisd3d.mc2discord.fabric.events.PlayerDeathCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.Advancement;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

public class FabricEvents {

    public static void register() {
        ServerMessageEvents.CHAT_MESSAGE.register(FabricEvents::onChatMessageEvent);
        ServerPlayConnectionEvents.JOIN.register(FabricEvents::onPlayerConnectEvent);
        ServerPlayConnectionEvents.DISCONNECT.register(FabricEvents::onPlayerDisconnectEvent);
        PlayerDeathCallback.EVENT.register(FabricEvents::onPlayerDeathEvent);
        PlayerCompletedAdvancementCallback.EVENT.register(FabricEvents::onAdvancementEvent);
        CommandExecuteCallback.EVENT.register(FabricEvents::onCommandEvent);
    }

    public static void onChatMessageEvent(PlayerChatMessage playerChatMessage, ServerPlayer serverPlayer, ChatType.Bound bound) {
        MinecraftEvents.onMinecraftChatMessageEvent(playerChatMessage.signedContent(), new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId()));
    }

    public static void onPlayerConnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, PacketSender packetSender, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(serverGamePacketListener.getPlayer().getGameProfile().getName(), serverGamePacketListener.getPlayer().getDisplayName().getString(), serverGamePacketListener.getPlayer().getGameProfile().getId()));
    }

    public static void onPlayerDisconnectEvent(ServerGamePacketListenerImpl serverGamePacketListener, MinecraftServer minecraftServer) {
        MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(serverGamePacketListener.getPlayer().getGameProfile().getName(), serverGamePacketListener.getPlayer().getDisplayName().getString(), serverGamePacketListener.getPlayer().getGameProfile().getId()));
    }

    public static void onPlayerDeathEvent(ServerPlayer serverPlayer, DamageSource damageSource) {
        MinecraftEvents.onPlayerDeathEvent(new PlayerEntity(serverPlayer.getGameProfile().getName(), serverPlayer.getDisplayName().getString(), serverPlayer.getGameProfile().getId()), new DeathEntity(damageSource.getMsgId(), serverPlayer.getCombatTracker().getDeathMessage().getString(), serverPlayer.getCombatTracker().getCombatDuration(), Optional.of(serverPlayer.getCombatTracker().mob).map(livingEntity -> livingEntity.getDisplayName().getString()).orElse(""), Optional.of(serverPlayer.getCombatTracker().mob).map(LivingEntity::getHealth).orElse(0.0f)));
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
        if (M2DUtils.isNotConfigured()) return InteractionResult.PASS;

        if (parseResults.getContext().getNodes().isEmpty()) return InteractionResult.PASS;

        if (!parseResults.getExceptions().isEmpty()) return InteractionResult.PASS;

        String command_name = parseResults.getContext().getNodes().get(0).getNode().getName();

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains(command_name)) return InteractionResult.PASS;

        CommandContext<CommandSourceStack> context = parseResults.getContext().build(parseResults.getReader().getString());

        try {
            String message = switch (command_name) {
                case "tellraw" -> {
                    StringRange selector_range = parseResults.getContext().getArguments().get("targets").getRange();
                    String target = context.getInput().substring(selector_range.getStart(), selector_range.getEnd());

                    if (target.equals("@s") && (context.getSource() == Mc2DiscordFabric.commandSource)) {
                        yield null; // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
                    } else if (!target.equals("@a")) {  // Else if the target is not everyone it does not target discord
                        yield "";
                    }

                    yield ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getComponent(context, "message"), null, 0).getString();
                }
                case "say" ->
                        ChatType.bind(ChatType.SAY_COMMAND, context.getSource()).decorate(MessageArgument.getMessage(context, "message")).getString();
                case "me" ->
                        ChatType.bind(ChatType.EMOTE_COMMAND, context.getSource()).decorate(MessageArgument.getMessage(context, "action")).getString();
                default -> "";
            };

            if (message == null) return InteractionResult.FAIL;
            if (message.isEmpty()) return InteractionResult.PASS;


            if (parseResults.getContext().getSource().getPlayer() != null) {
                PlayerEntity player = new PlayerEntity(parseResults.getContext().getSource().getPlayer().getGameProfile().getName(), parseResults.getContext().getSource().getPlayer().getDisplayName().getString(), parseResults.getContext().getSource().getPlayer().getGameProfile().getId());
                MessageManager.sendChatMessage(message, Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)), Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player))).subscribe();
            } else {
                MessageManager.sendChatMessage(message, Mc2Discord.INSTANCE.vars.mc2discord_display_name, Mc2Discord.INSTANCE.vars.mc2discord_avatar).subscribe();
            }
        } catch (CommandSyntaxException ignored) {
        }

        return InteractionResult.PASS;
    }
}
