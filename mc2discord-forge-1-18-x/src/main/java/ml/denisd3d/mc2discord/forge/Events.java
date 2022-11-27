package ml.denisd3d.mc2discord.forge;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.entities.Advancement;
import ml.denisd3d.mc2discord.core.entities.Death;
import ml.denisd3d.mc2discord.core.entities.Player;
import ml.denisd3d.mc2discord.core.events.MinecraftEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber()
public class Events {
    @SubscribeEvent
    public static void onMinecraftChatMessageEvent(ServerChatEvent event) {
        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage(), new Player(event.getPlayer()
                .getGameProfile()
                .getName(), event.getPlayer().getDisplayName().getString(), event.getPlayer()
                .getGameProfile()
                .getId()));
    }

    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerJoinEvent(new Player(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                .getDisplayName()
                .getString(), event.getPlayer().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerLeaveEvent(new Player(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                .getDisplayName()
                .getString(), event.getPlayer().getGameProfile().getId()));
    }

    @SubscribeEvent
    public static void onPlayerDieEvent(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof net.minecraft.world.entity.player.Player player) {
            MinecraftEvents.onPlayerDieEvent(
                    new Player(player.getGameProfile().getName(), player.getDisplayName()
                            .getString(), player.getGameProfile().getId()),
                    new Death(event.getSource().msgId, player.getCombatTracker()
                            .getDeathMessage()
                            .getString(), player.getCombatTracker()
                            .getCombatDuration(), Optional.ofNullable(player.getCombatTracker().getKiller())
                            .map(livingEntity -> livingEntity.getDisplayName().getString())
                            .orElse(""), Optional.ofNullable(player.getCombatTracker().getKiller())
                            .map(LivingEntity::getHealth)
                            .orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent event) {
        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceChat()) {
            MinecraftEvents.onAdvancementEvent(
                    new Player(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                            .getDisplayName()
                            .getString(), event.getPlayer().getGameProfile().getId()),
                    new Advancement(event.getAdvancement().getId().getPath(), event.getAdvancement()
                            .getChatComponent()
                            .getString(), event.getAdvancement()
                            .getDisplay()
                            .getTitle()
                            .getString(), event.getAdvancement().getDisplay().getDescription().getString())
            );
        }
    }

    @SubscribeEvent
    public static void onCommandEvent(CommandEvent event) {
        if (!M2DUtils.canHandleEvent() || !Mc2Discord.INSTANCE.config.misc.relay_say_me_tellraw_command)
            return;

        if (event.getParseResults().getContext().getNodes().size() == 0)
            return;

        String command_name = event.getParseResults().getContext().getNodes().get(0).getNode().getName();
        if (command_name.equals("say") || command_name.equals("me") || command_name.equals("tellraw")) {
            CommandContext<CommandSourceStack> context = event.getParseResults().getContext().build(event.getParseResults().getReader().getString());

            try {
                boolean should_send_to_discord = true;
                String message;
                if (command_name.equals("tellraw")) {
                    StringRange selector_range = event.getParseResults().getContext().getArguments().get("targets").getRange();
                    String target = context.getInput().substring(selector_range.getStart(), selector_range.getEnd());

                    if (target.equals("@s") && (context.getSource() == Mc2DiscordForge.commandSource)) {
                        event.setCanceled(true); // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
                    } else if (!target.equals("@a")) {  // Else if the target is not everyone it does not target discord
                        should_send_to_discord = false;
                    }

                    message = ComponentArgument.getComponent(context, "message").getString();
                } else if (command_name.equals("me")) {
                    TranslatableComponent translatableComponent = new TranslatableComponent("chat.type.emote", context.getSource()
                            .getDisplayName(), StringArgumentType.getString(context, "action"));
                    message = translatableComponent.getString();
                } else {
                    Component component = MessageArgument.getMessage(context, "message");
                    TranslatableComponent translatableComponent = new TranslatableComponent("chat.type.announcement", context.getSource()
                            .getDisplayName(), component);
                    message = translatableComponent.getString();
                }

                if (should_send_to_discord) {
                    Mc2Discord.INSTANCE.messageManager.sendInfoMessage(message);
                }
            } catch (IllegalArgumentException | CommandSyntaxException ignored) {
            } // Ignore when the command is malformed
        }
    }
}
