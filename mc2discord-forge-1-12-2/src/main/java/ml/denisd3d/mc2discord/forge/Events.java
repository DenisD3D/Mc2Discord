package ml.denisd3d.mc2discord.forge;

import com.google.gson.JsonParseException;
import com.google.gson.stream.MalformedJsonException;
import ml.denisd3d.mc2discord.core.M2DUtils;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.entities.Advancement;
import ml.denisd3d.mc2discord.core.entities.Death;
import ml.denisd3d.mc2discord.core.entities.Player;
import ml.denisd3d.mc2discord.core.events.MinecraftEvents;
import net.minecraft.command.CommandException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Optional;

import static net.minecraft.command.CommandBase.buildString;
import static net.minecraft.command.CommandBase.getChatComponentFromNthArg;

@Mod.EventBusSubscriber(modid = "mc2discord", value = Side.SERVER)
public class Events {
    @SubscribeEvent
    public static void onMinecraftChatMessageEvent(ServerChatEvent event) {
        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage(), new Player(event.getPlayer().getGameProfile().getName(), event.getPlayer()
                .getDisplayName()
                .getUnformattedText(), Optional.ofNullable(event.getPlayer().getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerJoinEvent(new Player(event.player.getGameProfile().getName(), event.player.getDisplayName()
                .getUnformattedText(), Optional.ofNullable(event.player.getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerLeaveEvent(new Player(event.player.getGameProfile().getName(), event.player.getDisplayName()
                .getUnformattedText(), Optional.ofNullable(event.player.getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerDieEvent(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            MinecraftEvents.onPlayerDieEvent(
                    new Player(player.getGameProfile().getName(), player.getDisplayName()
                            .getUnformattedText(), Optional.ofNullable(player.getGameProfile().getId()).orElse(null)),
                    new Death(event.getSource().damageType, player.getCombatTracker()
                            .getDeathMessage()
                            .getUnformattedText(), player.getCombatTracker().getCombatDuration(), Optional.ofNullable(player.getCombatTracker()
                                    .getBestAttacker())
                            .map(livingEntity -> livingEntity.getDisplayName().getUnformattedText())
                            .orElse(""), Optional.ofNullable(player.getCombatTracker().getBestAttacker())
                            .map(EntityLivingBase::getHealth)
                            .orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent event) {
        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceToChat()) {
            MinecraftEvents.onAdvancementEvent(
                    new Player(event.getEntityPlayer().getGameProfile().getName(), event.getEntityPlayer()
                            .getDisplayName()
                            .getUnformattedText(), Optional.ofNullable(event.getEntityPlayer().getGameProfile().getId()).orElse(null)),
                    new Advancement(event.getAdvancement().getId().getResourcePath(), event.getAdvancement()
                            .getDisplayText()
                            .getUnformattedText(), event.getAdvancement().getDisplay().getTitle().getUnformattedText(), event.getAdvancement()
                            .getDisplay()
                            .getDescription()
                            .getUnformattedText())
            );
        }
    }

    @SubscribeEvent
    public static void onCommandEvent(CommandEvent event) {
        if (!M2DUtils.canHandleEvent() || !Mc2Discord.INSTANCE.config.misc.relay_say_me_tellraw_command)
            return;

        String command_name = event.getCommand().getName();
        if (command_name.equals("say") || command_name.equals("me") || command_name.equals("tellraw")) {
            try {
                boolean should_send_to_discord = true;
                String message;
                if (command_name.equals("tellraw")) {
                    if (event.getParameters().length < 2)
                        return;
                    String target = event.getParameters()[0];
                    String s = buildString(event.getParameters(), 1);

                    if (target.equals("@s") && (event.getSender() == Mc2DiscordForge.commandSender)) {
                        event.setCanceled(true); // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
                    } else if (!target.equals("@a")) {  // Else if the target is not everyone it does not target discord
                        should_send_to_discord = false;
                    }
                    ITextComponent iTextComponent = ITextComponent.Serializer.jsonToComponent(s);
                    if (iTextComponent == null)
                        return;

                    message = iTextComponent.getUnformattedText();
                } else if (command_name.equals("me")) {
                    if (event.getParameters().length <= 0)
                        return;
                    ITextComponent itextcomponent = getChatComponentFromNthArg(event.getSender(), event.getParameters(), 0, !(event.getSender() instanceof EntityPlayer));
                    message = new TextComponentTranslation("chat.type.emote", event.getSender()
                            .getDisplayName(), itextcomponent).getUnformattedText();
                } else {
                    if (event.getParameters().length <= 0 || event.getParameters()[0].length() <= 0)
                        return;
                    ITextComponent itextcomponent = getChatComponentFromNthArg(event.getSender(), event.getParameters(), 0, true);
                    message = new TextComponentTranslation("chat.type.announcement", event.getSender()
                            .getDisplayName(), itextcomponent).getUnformattedText();
                }

                if (should_send_to_discord) {
                    Mc2Discord.INSTANCE.messageManager.sendInfoMessage(message);
                }
            }  // Ignore when the command is malformed
            catch (IllegalArgumentException | CommandException | JsonParseException ignored) {
            }
        }
    }
}