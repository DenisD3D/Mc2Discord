package ml.denisd3d.mc2discord.forge;

import ml.denisd3d.mc2discord.core.entities.Advancement;
import ml.denisd3d.mc2discord.core.entities.Death;
import ml.denisd3d.mc2discord.core.entities.Player;
import ml.denisd3d.mc2discord.core.events.MinecraftEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = "mc2discord", value = Side.SERVER)
public class Events {
    @SubscribeEvent
    public static void onMinecraftChatMessageEvent(ServerChatEvent event) {
        MinecraftEvents.onMinecraftChatMessageEvent(event.getMessage(), new Player(event.getPlayer().getGameProfile().getName(), event.getPlayer().getDisplayName().getUnformattedText(), Optional.ofNullable(event.getPlayer().getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerJoinEvent(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftEvents.onPlayerJoinEvent(new Player(event.player.getGameProfile().getName(), event.player.getDisplayName().getUnformattedText(), Optional.ofNullable(event.player.getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerLeaveEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftEvents.onPlayerLeaveEvent(new Player(event.player.getGameProfile().getName(), event.player.getDisplayName().getUnformattedText(), Optional.ofNullable(event.player.getGameProfile().getId()).orElse(null)));
    }

    @SubscribeEvent
    public static void onPlayerDieEvent(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntityLiving();
            MinecraftEvents.onPlayerDieEvent(
                    new Player(player.getGameProfile().getName(), player.getDisplayName().getUnformattedText(), Optional.ofNullable(player.getGameProfile().getId()).orElse(null)),
                    new Death(event.getSource().damageType, player.getCombatTracker().getDeathMessage().getUnformattedText(), player.getCombatTracker().getCombatDuration(), Optional.ofNullable(player.getCombatTracker().getBestAttacker()).map(livingEntity -> livingEntity.getDisplayName().getUnformattedText()).orElse(""), Optional.ofNullable(player.getCombatTracker().getBestAttacker()).map(EntityLivingBase::getHealth).orElse(0.0f)));
        }
    }

    @SubscribeEvent
    public static void onAdvancementEvent(AdvancementEvent event) {
        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceToChat()) {
            MinecraftEvents.onAdvancementEvent(
                    new Player(event.getEntityPlayer().getGameProfile().getName(), event.getEntityPlayer().getDisplayName().getUnformattedText(), Optional.ofNullable(event.getEntityPlayer().getGameProfile().getId()).orElse(null)),
                    new Advancement(event.getAdvancement().getId().getResourcePath(), event.getAdvancement().getDisplayText().getUnformattedText(), event.getAdvancement().getDisplay().getTitle().getUnformattedText(), event.getAdvancement().getDisplay().getDescription().getUnformattedText())
            );
        }
    }
}
