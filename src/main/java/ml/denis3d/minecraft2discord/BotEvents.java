package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(Dist.DEDICATED_SERVER)
public class BotEvents extends ListenerAdapter {
    private static Webhook discordWebhook;
    private static WebhookClient discordWebhookClient;
    private static WebhookMessageBuilder builder;
    private static TextChannel chatChannel;
    private static TextChannel infoChannel;

    @SubscribeEvent
    public static void onServerChat(final ServerChatEvent event) {
        if (Minecraft2Discord.getDiscordBot() == null)
            return;
        if (chatChannel == null) {
            chatChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
        }
        if (chatChannel != null && Config.SERVER.chatChannel.get() != 0) {
            if (discordWebhookClient == null) {
                if (discordWebhook == null) {
                    List discordWebhooks = chatChannel.getWebhooks().complete().stream().filter(webhook -> webhook.getName().startsWith("Minecraft2Discord")).collect(Collectors.toList());
                    if (discordWebhooks.size() == 0) {
                        discordWebhook = chatChannel.createWebhook("Minecraft2Discord").complete();
                    } else {
                        discordWebhook = (Webhook) discordWebhooks.get(0);
                    }
                }
                discordWebhookClient = discordWebhook.newClient().build();
            }
            builder = new WebhookMessageBuilder();
            builder.setContent(event.getMessage())
                    .setUsername(event.getUsername())
                    .setAvatarUrl(Utils.globalVariableReplacement(Config.SERVER.discordPictureAPI.get()).replace("$1", event.getUsername()).replace("$2", event.getPlayer().getUniqueID().toString()));
            discordWebhookClient.send(builder.build());
        }

        Utils.updateDiscordPresence();
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            if (Config.SERVER.sendDeathsMessages.get() && Config.SERVER.infoChannel.get() != 0) {
                if (Minecraft2Discord.getDiscordBot() == null)
                    return;

                if (infoChannel == null) {
                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                }
                if (infoChannel != null) {
                    PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                    infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.deathMessage.get()).replace("$1", player.getName().getUnformattedComponentText()).replace("$2", player.getCombatTracker().getDeathMessage().getUnformattedComponentText())).submit();
                }
            }
        }

        Utils.updateDiscordPresence();
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && !event.getAdvancement().getId().getPath().startsWith("recipes")) {
            if (Config.SERVER.hideAdvancementList.get().stream().anyMatch(s -> s.startsWith(event.getAdvancement().getId().toString()))) {
                return;
            }
            if (Config.SERVER.sendAdvancementMessages.get() && Config.SERVER.infoChannel.get() != 0) {
                if (Minecraft2Discord.getDiscordBot() == null)
                    return;

                if (infoChannel == null) {
                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                }
                if (infoChannel != null) {
                    PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                    infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.advancementMessage.get()).replace("$1", player.getName().getUnformattedComponentText()).replace("$2", event.getAdvancement().getDisplayText().getString()).replace("$3", (event.getAdvancement().getDisplay().getDescription() != null ? event.getAdvancement().getDisplay().getDescription().getUnformattedComponentText() : ""))).submit();
                }
            }
        }

        Utils.updateDiscordPresence();
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Config.SERVER.sendJoinLeftMessages.get() && Config.SERVER.infoChannel.get() != 0) {
            if (Minecraft2Discord.getDiscordBot() == null)
                return;

            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.joinMessage.get()).replace("$1", event.getPlayer().getName().getUnformattedComponentText())).submit();
            }
        }

        Utils.updateDiscordPresence();
    }

    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (Config.SERVER.sendJoinLeftMessages.get() && Config.SERVER.infoChannel.get() != 0) {
            if (Minecraft2Discord.getDiscordBot() == null)
                return;

            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.leftMessage.get()).replace("$1", event.getPlayer().getName().getUnformattedComponentText())).submit();
            }
        }

        Utils.updateDiscordPresence();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        InterModComms.getMessages("minecraft2discord").forEach(imcMessage -> {
            if (imcMessage.getMethod().equals("info_channel")) {
                if (Config.SERVER.allowInterModComms.get() && Config.SERVER.infoChannel.get() != 0) {
                    if (Minecraft2Discord.getDiscordBot() == null)
                        return;

                    if (infoChannel == null) {
                        infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                    }
                    if (infoChannel != null) {
                        infoChannel.sendMessage(Utils.globalVariableReplacement(imcMessage.getMessageSupplier().get().toString())).submit();
                    }
                }
            }

            if (imcMessage.getMethod().equals("chat_channel")) {
                if (Config.SERVER.allowInterModComms.get() && Config.SERVER.chatChannel.get() != 0) {
                    if (Minecraft2Discord.getDiscordBot() == null)
                        return;

                    if (chatChannel == null) {
                        chatChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
                    }
                    if (chatChannel != null) {
                        chatChannel.sendMessage(Utils.globalVariableReplacement(imcMessage.getMessageSupplier().get().toString())).submit();
                    }
                }
            }

            if (imcMessage.getMethod().matches("\\d+")) {
                if (Config.SERVER.allowInterModComms.get()) {
                    if (Minecraft2Discord.getDiscordBot() == null)
                        return;

                    TextChannel channel = Minecraft2Discord.getDiscordBot().getTextChannelById(imcMessage.getMethod());

                    if (channel != null) {
                        channel.sendMessage(Utils.globalVariableReplacement(imcMessage.getMessageSupplier().get().toString())).submit();
                    }
                }
            }
        });
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Discord bot logged as " + event.getJDA().getSelfUser().getName());
        if (Config.SERVER.sendServerStartStopMessages.get() && Config.SERVER.infoChannel.get() != 0) {
            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.serverStartMessage.get())).submit();
            }
        }

        Utils.updateDiscordPresence();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Config.SERVER.chatChannel.get() == event.getChannel().getIdLong()) {
            if (!event.getAuthor().isBot()) {
                if (event.getMessage().getContentRaw().startsWith("/") && (Config.SERVER.commandAllowedUsersIds.get().contains(event.getAuthor().getIdLong()) || event.getMember().getRoles().stream().map(Role::getIdLong).anyMatch(Config.SERVER.commandAllowedRolesIds.get()::contains))) {
                    ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(
                            new CommandSource(new DiscordCommandSource(event.getChannel()),
                                    ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD) == null ? Vec3d.ZERO : new Vec3d(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSpawnPoint()),
                                    Vec2f.ZERO,
                                    ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
                                    4,
                                    "Discord",
                                    new StringTextComponent("Discord"),
                                    ServerLifecycleHooks.getCurrentServer(),
                                    null), event.getMessage().getContentDisplay());
                    if (event.getMessage().getContentRaw().startsWith("/say")) {
                        if (Config.SERVER.infoChannel.get() != 0) {
                            if (infoChannel == null) {
                                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                            }
                            if (infoChannel != null) {
                                infoChannel.sendMessage(Utils.globalVariableReplacement(event.getMessage().getContentRaw().substring(5))).submit();
                            }
                        }
                    }
                } else {
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(new StringTextComponent("<Discord - " + event.getAuthor().getName() + "> " + event.getMessage().getContentDisplay()));
                }
            }
        }

        Utils.updateDiscordPresence();
    }
}
