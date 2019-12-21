package ml.denis3d.minecraft2discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
        if (chatChannel != null && Config.SERVER.chatChannel.get() != 0L) {
            if (Config.SERVER.useDiscordWebhooks.get()) {
                if (discordWebhookClient == null) {
                    if (discordWebhook == null) {
                        List discordWebhooks = chatChannel.retrieveWebhooks().complete().stream().filter(webhook -> webhook.getName().startsWith("Minecraft2Discord")).collect(Collectors.toList());
                        if (discordWebhooks.size() == 0) {
                            discordWebhook = chatChannel.createWebhook("Minecraft2Discord").complete();
                        } else {
                            discordWebhook = (Webhook) discordWebhooks.get(0);
                        }
                    }
                    discordWebhookClient = WebhookClient.withUrl(discordWebhook.getUrl());
                }
                builder = new WebhookMessageBuilder();
                builder.setContent(event.getMessage())
                        .setUsername(event.getUsername())
                        .setAvatarUrl(Utils.globalVariableReplacement(Config.SERVER.discordPictureAPI.get()).replace("$1", event.getUsername()).replace("$2", event.getPlayer().getUniqueID().toString()));
                discordWebhookClient.send(builder.build());
            } else {
                chatChannel.sendMessage("**" + event.getUsername() + "** : " + event.getMessage()).submit();
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            if (Config.SERVER.sendDeathsMessages.get() && Config.SERVER.infoChannel.get() != 0L) {
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
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && !event.getAdvancement().getId().getPath().startsWith("recipes")) {
            if (Config.SERVER.hideAdvancementList.get().stream().anyMatch(s -> s.startsWith(event.getAdvancement().getId().toString()))) {
                return;
            }
            if (Config.SERVER.sendAdvancementMessages.get() && Config.SERVER.infoChannel.get() != 0L) {
                if (Minecraft2Discord.getDiscordBot() == null)
                    return;

                if (infoChannel == null) {
                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                }
                if (infoChannel != null) {
                    PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                    String message = Utils.globalVariableReplacement(Config.SERVER.advancementMessage.get());
                    message = message.replace("$1", player.getName().getUnformattedComponentText());
                    message = message.replace("$2", event.getAdvancement().getDisplayText().getString());
                    if (event.getAdvancement().getDisplay() != null)
                        message = message.replace("$3", event.getAdvancement().getDisplay().getDescription().getUnformattedComponentText());
                    else
                        message = message.replace("$3", "");
                    infoChannel.sendMessage(message).submit();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Config.SERVER.sendJoinLeftMessages.get() && Config.SERVER.infoChannel.get() != 0L) {
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
        if (Config.SERVER.sendJoinLeftMessages.get() && Config.SERVER.infoChannel.get() != 0L) {
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
                if (Config.SERVER.allowInterModComms.get() && Config.SERVER.infoChannel.get() != 0L) {
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
                if (Config.SERVER.allowInterModComms.get() && Config.SERVER.chatChannel.get() != 0L) {
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
        if (Config.SERVER.sendServerStartStopMessages.get() && Config.SERVER.infoChannel.get() != 0L) {
            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.serverStartMessage.get())).submit();
            }
        }

        Utils.updateDiscordPresence();
    }

    public static boolean isM2DBotOrWebhook(MessageReceivedEvent event) {
        if (event.getAuthor().getIdLong() == Minecraft2Discord.getDiscordBot().getSelfUser().getIdLong()) {
            return true;
        } else return discordWebhook != null && event.getAuthor().getIdLong() == discordWebhook.getIdLong();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Config.SERVER.chatChannel.get() == event.getChannel().getIdLong()) {
            if ((!event.getAuthor().isBot() || Config.SERVER.allowBotSendMessage.get()) && !isM2DBotOrWebhook(event)) {
                if (event.getMessage().getContentRaw().startsWith("/")
                        && ((Config.SERVER.commandAllowedUsersIds.get().contains(event.getAuthor().getIdLong())
                        || event.getMember().getRoles().stream().map(Role::getIdLong).anyMatch(Config.SERVER.commandAllowedRolesIds.get()::contains))
                        || Config.SERVER.allowedCommandForEveryone.get().stream().anyMatch(s -> event.getMessage().getContentDisplay().substring(1).startsWith(s)))) {

                    CommandSource commandSource = new CommandSource(new DiscordCommandSource(event.getChannel()),
                            new Vec3d(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSpawnPoint()),
                            Vec2f.ZERO,
                            ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
                            4,
                            "Discord",
                            new StringTextComponent("Discord"),
                            ServerLifecycleHooks.getCurrentServer(),
                            null);

                    if (event.getMessage().getContentRaw().startsWith("/help")) {
                        Commands commandsManager = ServerLifecycleHooks.getCurrentServer().getCommandManager();

                        RootCommandNode<CommandSource> discordHelpCommandRoot = new RootCommandNode<>();
                        for (String command : Config.SERVER.allowedCommandForEveryone.get()) {
                            CommandNode<CommandSource> node = commandsManager.getDispatcher().getRoot().getChild(command.split(" ")[0]);
                            for (Object child : Arrays.stream(command.split(" ")).skip(1).toArray()) {
                                node.addChild(node.getChild((String) child));
                            }
                            discordHelpCommandRoot.addChild(node);
                        }

                        Map<CommandNode<CommandSource>, String> lvt_2_1_ = commandsManager.getDispatcher().getSmartUsage(discordHelpCommandRoot, commandSource);

                        for (String lvt_4_1_ : lvt_2_1_.values()) {
                            commandSource.sendFeedback(new StringTextComponent("/" + lvt_4_1_), false);
                        }
                    } else {
                        ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(commandSource, event.getMessage().getContentDisplay());
                        if (event.getMessage().getContentRaw().startsWith("/say")) {
                            if (Config.SERVER.infoChannel.get() != 0L) {
                                if (infoChannel == null) {
                                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                                }
                                if (infoChannel != null) {
                                    infoChannel.sendMessage(Utils.globalVariableReplacement(event.getMessage().getContentRaw().substring(5))).submit();
                                }
                            }
                        }
                    }
                } else if (event.getMessage().getContentRaw().startsWith("/")
                        && Config.SERVER.allowedCommandForEveryone.get().stream().noneMatch(s -> event.getMessage().getContentDisplay().substring(1).startsWith(s))) {
                    if (Config.SERVER.infoChannel.get() != 0L && !Config.SERVER.commandMissingPermissionsMessage.get().isEmpty()) {
                        if (infoChannel == null) {
                            infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                        }
                        if (infoChannel != null) {
                            infoChannel.sendMessage(Utils.globalVariableReplacement(Config.SERVER.commandMissingPermissionsMessage.get())).submit();
                        }
                    }
                } else {
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(new StringTextComponent("<Discord - " + event.getAuthor().getName() + "> " + event.getMessage().getContentDisplay()));
                }
            }
        }
    }
}
