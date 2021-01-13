package ml.denisd3d.minecraft2discord.events;

import club.minnced.discord.webhook.send.AllowedMentions;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.google.common.collect.ImmutableMap;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.ExtensionUtils;
import ml.denisd3d.minecraft2discord.managers.ChannelManager;
import ml.denisd3d.minecraft2discord.managers.MessageManager;
import ml.denisd3d.minecraft2discord.managers.VariableManager;
import ml.denisd3d.minecraft2discord.managers.WebhookManager;
import net.dv8tion.jda.api.MessageBuilder;
import net.minecraft.command.arguments.MessageArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class MinecraftEvents
{
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onPlayerJoin(event))) // Execute extensions
            return;

        if (Config.SERVER.joinLeaveEnabled.get())
            MessageManager.sendFormattedMessage(ChannelManager.getInfoChannel(), Config.SERVER.joinMessage.get(),
                ImmutableMap.of(VariableManager.playerVariables, event.getPlayer()));
    }

    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event)
    {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onPlayerLeft(event))) // Execute extensions
            return;

        if (Config.SERVER.joinLeaveEnabled.get())
            MessageManager.sendFormattedMessage(ChannelManager.getInfoChannel(), Config.SERVER.leaveMessage.get(),
                ImmutableMap.of(VariableManager.playerVariables, event.getPlayer()));
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event)
    {
        if (event.getEntityLiving() instanceof PlayerEntity)
        {
            if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onDeath(event))) // Execute extensions
                return;

            if (Config.SERVER.deathEnabled.get())
            {
                PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                MessageManager.sendFormattedMessage(ChannelManager.getInfoChannel(), Config.SERVER.deathMessage.get(),
                    ImmutableMap.of(VariableManager.playerVariables, player, VariableManager.deathVariables, event));
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event)
    {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onAdvancement(event))) // Execute extensions
            return;

        if (event.getAdvancement().getDisplay() != null && event.getAdvancement().getDisplay().shouldAnnounceToChat())
        {
            if (Config.SERVER.hiddenAdvancementsList.get().stream().anyMatch(s -> s.startsWith(event.getAdvancement().getId().toString())))
                return;

            if (Config.SERVER.advancementEnabled.get())
            {
                MessageManager.sendFormattedMessage(ChannelManager.getInfoChannel(), Config.SERVER.advancementMessage.get(),
                    ImmutableMap.of(VariableManager.playerVariables, event.getPlayer(), VariableManager.advancementVariables, event));
            }
        }
    }

    @SubscribeEvent
    public static void onCommand(CommandEvent event) throws CommandSyntaxException {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onCommand(event))) // Execute extensions
            return;

        if (event.getParseResults().getContext().getCommand() != null) {
            if (event.getParseResults().getContext().getNodes().get(0).getNode().getName().equals("say")) {
                MessageManager.sendMessage(ChannelManager.getInfoChannel(), new TranslationTextComponent("chat.type.announcement", event.getParseResults().getContext().getSource().getName(), ((MessageArgument.Message) event.getParseResults().getContext().getArguments().get("message").getResult()).toComponent(event.getParseResults().getContext().getSource(), true)).getUnformattedComponentText());
            }
            else if (event.getParseResults().getContext().getNodes().get(0).getNode().getName().equals("me") && event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayerEntity)
            {
                sendChatMessage(new TranslationTextComponent("chat.type.emote", event.getParseResults().getContext().getSource().getName(), event.getParseResults().getContext().getArguments().get("action").getResult()).getUnformattedComponentText(), event.getParseResults().getContext().getSource().asPlayer());
            }
        }
    }

    @SubscribeEvent
    public static void onServerChat(final ServerChatEvent event) {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onMinecraftMessage(event))) // Execute extensions
            return;

        sendChatMessage(event.getMessage(), event.getPlayer());



    }

    private static void sendChatMessage(String message, PlayerEntity playerEntity) {
        if (Config.SERVER.chatChannel.get() != null) {
            if (Config.SERVER.webhooksEnabled.get()) {
                WebhookMessageBuilder builder = new WebhookMessageBuilder();
                builder.setContent(VariableManager.messageVariables.get("message", message))
                        .setUsername(VariableManager.replace(Config.SERVER.nameFormat.get(), ImmutableMap.of(VariableManager.playerVariables, playerEntity)))
                        .setAvatarUrl(VariableManager.replace(Config.SERVER.avatarAPI.get(), ImmutableMap.of(VariableManager.playerVariables, playerEntity)));
                builder.setAllowedMentions(new AllowedMentions().withParseEveryone(Config.SERVER.mentionsEnabled.get()).withParseUsers(true).withParseRoles(true));

                if (WebhookManager.getWebhookClient(Config.SERVER.chatChannel.get()) != null) {
                    WebhookManager.getWebhookClient(Config.SERVER.chatChannel.get()).send(builder.build());
                } else {
                    WebhookManager.initChatWebhook(() -> WebhookManager.getWebhookClient(Config.SERVER.chatChannel.get()).send(builder.build()));
                }
            } else {
                ChannelManager.getChatChannel().sendMessage(new MessageBuilder(VariableManager.replace(Config.SERVER.messageFormat.get(), ImmutableMap.of(VariableManager.playerVariables, playerEntity, VariableManager.messageVariables, message))).stripMentions(ChannelManager.getChatChannel().getJDA()).build()).queue();
            }
        }
    }
}