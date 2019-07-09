package ml.denis3d.minecraft2discord;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.Webhook;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.webhook.WebhookClient;
import net.dv8tion.jda.webhook.WebhookMessageBuilder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
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
        System.out.println(event.getPlayer().getUniqueID().toString());
        if (Minecraft2Discord.getDiscordBot() == null)
            return;
        if (chatChannel == null) {
            chatChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.chatChannel.get());
        }
        if (chatChannel != null) {
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
                    .setAvatarUrl(Config.SERVER.discordPictureAPI.get().replace("$1", event.getUsername()).replace("$2", event.getPlayer().getUniqueID().toString()));
            discordWebhookClient.send(builder.build());
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity) {
            if (Config.SERVER.sendDeathsMessages.get() || Config.SERVER.infoChannel.get() == 0) {
                if (Minecraft2Discord.getDiscordBot() == null)
                    return;

                if (infoChannel == null) {
                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                }
                if (infoChannel != null) {
                    PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                    infoChannel.sendMessage(Config.SERVER.deathMessage.get().replace("$1", player.getName().getUnformattedComponentText()).replace("$2", player.getCombatTracker().getDeathMessage().getUnformattedComponentText())).submit();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent event) {
        if (event.getEntityLiving() instanceof PlayerEntity && !event.getAdvancement().getId().getPath().startsWith("recipes")) {
            if (Config.SERVER.sendAdvancementMessages.get() || Config.SERVER.infoChannel.get() == 0) {
                if (Minecraft2Discord.getDiscordBot() == null)
                    return;

                if (infoChannel == null) {
                    infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
                }
                if (infoChannel != null) {
                    PlayerEntity player = (PlayerEntity) event.getEntityLiving();
                    infoChannel.sendMessage(Config.SERVER.advancementMessage.get().replace("$1", player.getName().getUnformattedComponentText()).replace("$2", event.getAdvancement().getDisplayText().getString())).submit();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Config.SERVER.sendJoinLeftMessages.get() || Config.SERVER.infoChannel.get() == 0) {
            if (Minecraft2Discord.getDiscordBot() == null)
                return;

            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Config.SERVER.joinMessage.get().replace("$1", event.getPlayer().getName().getUnformattedComponentText())).submit();
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLeft(PlayerEvent.PlayerLoggedOutEvent event) {
        if (Config.SERVER.sendJoinLeftMessages.get() || Config.SERVER.infoChannel.get() == 0) {
            if (Minecraft2Discord.getDiscordBot() == null)
                return;

            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Config.SERVER.leftMessage.get().replace("$1", event.getPlayer().getName().getUnformattedComponentText())).submit();
            }
        }
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Discord bot logged as " + event.getJDA().getSelfUser().getName());
        if (Config.SERVER.sendServerStartStopMessages.get() || Config.SERVER.infoChannel.get() == 0) {
            if (infoChannel == null) {
                infoChannel = Minecraft2Discord.getDiscordBot().getTextChannelById(Config.SERVER.infoChannel.get());
            }
            if (infoChannel != null) {
                infoChannel.sendMessage(Config.SERVER.serverStartMessage.get()).submit();
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (Config.SERVER.chatChannel.get() == event.getChannel().getIdLong())
            if (!event.getAuthor().isBot())
                ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(new StringTextComponent("<Discord - " + event.getAuthor().getName() + "> " + event.getMessage().getContentDisplay()));
    }
}
