package ml.denis3d.minecraft2discord.events;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.denis3d.minecraft2discord.Config;
import ml.denis3d.minecraft2discord.Minecraft2Discord;
import ml.denis3d.minecraft2discord.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Webhook;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.ServerLifecycleEvent;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class ServerEvents
{
    public static WebhookClient discordWebhookClient;

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (Config.SERVER.sendJoinLeftMessages.get())
            Utils.sendInfoMessage(Config.SERVER.joinMessage.get()
                .replace("$1", event.getPlayer().getName().getFormattedText()));
    }

    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (Minecraft2Discord.getDiscordBot() != null && Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.CONNECTED && Config.SERVER.sendJoinLeftMessages.get())
            Utils.sendInfoMessage(Config.SERVER.leftMessage.get()
                .replace("$1", event.getPlayer().getName().getFormattedText()));
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event)
    {
        InterModComms.getMessages("minecraft2discord").forEach(imcMessage ->
        {
            if (Config.SERVER.allowInterModComms.get())
            {
                if (imcMessage.getMethod().equals("info_channel"))
                    Utils.sendInfoMessage(imcMessage.getMessageSupplier().get().toString());

                if (imcMessage.getMethod().equals("chat_channel"))
                    Utils.sendChatMessage(imcMessage.getMessageSupplier().get().toString());

                if (imcMessage.getMethod().matches("\\d+"))
                    Utils.sendMessage(Minecraft2Discord.getDiscordBot().getTextChannelById(imcMessage.getMethod()),
                        imcMessage.getMessageSupplier().get().toString());
            }
        });
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity)
        {
            if (Config.SERVER.sendDeathsMessages.get())
            {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                Utils.sendInfoMessage(Config.SERVER.deathMessage.get()
                    .replace("$1", player.getCombatTracker().getDeathMessage().getFormattedText())
                    .replace("$2", player.getName().getFormattedText())
                    .replace("$3", player.getCombatTracker().getDeathMessage().getUnformattedComponentText())
                    .replace("$4", "death.attack." + event.getSource().damageType)
                );
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity && event.getAdvancement().getDisplay() != null)
        {
            if (Config.SERVER.hideAdvancementList.get().stream().anyMatch(s -> s.startsWith(event.getAdvancement().getId().toString())))
                return;

            if (Config.SERVER.sendAdvancementMessages.get())
            {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                String message = Config.SERVER.advancementMessage.get()
                    .replace("$1", player.getName().getFormattedText())
                    .replace("$2", event.getAdvancement().getDisplayText().getString());
                message = message.replace("$3", event.getAdvancement().getDisplay().getDescription().getUnformattedComponentText());

                Utils.sendInfoMessage(message);
            }
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) throws CommandSyntaxException
    {
        if (event.getParseResults().getContext().getCommand() != null && event.getParseResults().getContext().getNodes().get(0).getNode().getName().equals("say"))
        {
            Utils.sendInfoMessage(Config.SERVER.noneWebhookChatMessageFormat.get()
                .replace("$1", event.getParseResults().getContext().getSource().getDisplayName().getFormattedText())
                .replace("$2", ((MessageArgument.Message) event.getParseResults().getContext().getArguments().get("message").getResult())
                    .toComponent(event.getParseResults().getContext().getSource(), true).getFormattedText()));
        }
    }

    @SubscribeEvent
    public static void onServerChat(final ServerChatEvent event)
    {
        if (Config.SERVER.useDiscordWebhooks.get())
        {
            if (discordWebhookClient == null)
            {
                if (Utils.discordWebhook == null && Config.SERVER.chatChannel.get() != 0L)
                {
                    if (Utils.chatChannel == null)
                        Utils.chatChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
                    if (Utils.chatChannel != null)
                    {
                        List<Webhook> discordWebhooks = Utils.chatChannel.retrieveWebhooks().complete().stream()
                            .filter(webhook -> webhook.getName().startsWith("Minecraft2Discord")).collect(Collectors.toList());
                        if (discordWebhooks.size() == 0)
                        {
                            Utils.discordWebhook = Utils.chatChannel.createWebhook("Minecraft2Discord").complete();
                        } else
                        {
                            Utils.discordWebhook = discordWebhooks.get(0);
                        }
                    }
                }
                discordWebhookClient = WebhookClient.withUrl(Utils.discordWebhook.getUrl());
            }
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            builder.setContent(event.getMessage())
                .setUsername(event.getUsername())
                .setAvatarUrl(Utils.globalVariableReplacement(Config.SERVER.discordPictureAPI.get())
                    .replace("$1", event.getUsername())
                    .replace("$2", event.getPlayer().getUniqueID().toString()));
            discordWebhookClient.send(builder.build());
        } else
        {
            Utils.sendChatMessage(Utils.globalVariableReplacement(Config.SERVER.noneWebhookChatMessageFormat.get()
                .replace("$1", event.getUsername())
                .replace("$2", event.getMessage())));
        }
    }
}
