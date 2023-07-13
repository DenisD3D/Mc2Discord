package fr.denisd3d.mc2discord.forge;

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
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Optional;

public class ForgeEvents {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onServerChat(ServerChatEvent event) {
        if (event.isCanceled())
            return;

        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage().getString(), new PlayerEntity(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                .getDisplayName()
                .getString(), event.getPlayer().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerConnectEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerConnectEvent(new PlayerEntity(event.getEntity().getGameProfile().getName(), event.getEntity().getDisplayName().getString(), event.getEntity().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerDisconnectEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerDisconnectEvent(new PlayerEntity(event.getEntity().getGameProfile().getName(), event.getEntity().getDisplayName().getString(), event.getEntity().getGameProfile().getId()));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerDeathEvent(LivingDeathEvent event) {
        if (event.isCanceled())
            return;

        if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
            MinecraftEvents.onPlayerDeathEvent(new PlayerEntity(player.getGameProfile().getName(), player.getDisplayName().getString(), player.getGameProfile().getId()), new DeathEntity(event.getSource()
                    .getMsgId(), player.getCombatTracker().getDeathMessage().getString(), player.getCombatTracker().getCombatDuration(), Optional.of(player.getCombatTracker().mob)
                    .map(livingEntity -> livingEntity.getDisplayName().getString())
                    .orElse(""), Optional.of(player.getCombatTracker().mob).map(LivingEntity::getHealth).orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
            MinecraftEvents.onAdvancementEvent(new PlayerEntity(event.getEntity().getGameProfile().getName(), event.getEntity().getDisplayName().getString(), event.getEntity()
                    .getGameProfile()
                    .getId()), new AdvancementEntity(event.getAdvancement().getId().getPath(), event.getAdvancement().getChatComponent().getString(), event.getAdvancement()
                    .getDisplay()
                    .getTitle()
                    .getString(), event.getAdvancement().getDisplay().getDescription().getString()));
        }
    }

    /**
     * Commands fixes for Discord
     */
    @SubscribeEvent
    public static void onCommandEvent(CommandEvent event) {
        if (M2DUtils.isNotConfigured())
            return;

        if (event.getParseResults().getContext().getNodes().size() == 0)
            return;

        if (!event.getParseResults().getExceptions().isEmpty())
            return;

        String command_name = event.getParseResults().getContext().getNodes().get(0).getNode().getName();

        CommandContext<CommandSourceStack> context = event.getParseResults().getContext().build(event.getParseResults().getReader().getString());

        try {
            String message = switch (command_name) {
                case "tellraw" -> {
                    StringRange selector_range = event.getParseResults().getContext().getArguments().get("targets").getRange();
                    String target = context.getInput().substring(selector_range.getStart(), selector_range.getEnd());

                    if (target.equals("@s") && (context.getSource() == Mc2DiscordForge.commandSource)) {
                        event.setCanceled(true); // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
                    } else if (!target.equals("@a")) {  // Else if the target is not everyone it does not target discord
                        yield null;
                    }

                    yield ComponentArgument.getComponent(context, "message").getString();
                }
                case "say" -> ChatType.bind(ChatType.SAY_COMMAND, context.getSource())
                        .decorate(MessageArgument.getMessage(context, "message"))
                        .getString();
                case "me" -> ChatType.bind(ChatType.EMOTE_COMMAND, context.getSource())
                        .decorate(MessageArgument.getMessage(context, "action"))
                        .getString();
                default -> null;
            };

            if (message == null)
                return;


            if (event.getParseResults().getContext().getSource().getPlayer() != null) {
                PlayerEntity player = new PlayerEntity(event.getParseResults().getContext().getSource().getPlayer().getGameProfile().getName(), event.getParseResults().getContext().getSource().getPlayer().getDisplayName()
                        .getString(), event.getParseResults().getContext().getSource().getPlayer().getGameProfile().getId());
                MessageManager.sendChatMessage(message, player.displayName, Entity.replace(Mc2Discord.INSTANCE.config.style.avatar_api, List.of(player))).subscribe();
            } else {
                MessageManager.sendChatMessage(message, Mc2Discord.INSTANCE.vars.mc2discord_display_name, Mc2Discord.INSTANCE.vars.mc2discord_avatar).subscribe();
            }

        } catch (CommandSyntaxException ignored) {
        }
    }
}
