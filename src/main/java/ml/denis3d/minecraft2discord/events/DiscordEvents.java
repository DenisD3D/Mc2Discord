package ml.denis3d.minecraft2discord.events;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.vdurmont.emoji.EmojiParser;
import ml.denis3d.minecraft2discord.Config;
import ml.denis3d.minecraft2discord.DiscordCommandSource;
import ml.denis3d.minecraft2discord.Minecraft2Discord;
import ml.denis3d.minecraft2discord.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DiscordEvents extends ListenerAdapter
{
    @Override
    public void onReady(ReadyEvent event)
    {
        System.out.println("Discord bot logged as " + event.getJDA().getSelfUser().getName());

        Utils.global_variables.put("online_players", () -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getCurrentPlayerCount()));
        Utils.global_variables.put("max_players", () -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getMaxPlayers()));
        Utils.global_variables.put("unique_player", () -> String.valueOf(Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSaveHandler().getPlayerFolder().list()).map(list -> list.length).orElse(0)));
        Utils.global_variables.put("motd", () -> ServerLifecycleHooks.getCurrentServer().getMOTD());
        Utils.global_variables.put("mc_version", () -> ServerLifecycleHooks.getCurrentServer().getMinecraftVersion());
        Utils.global_variables.put("server_hostname", () -> ServerLifecycleHooks.getCurrentServer().getServerHostname());
        Utils.global_variables.put("server_port", () -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getServerPort()));
        Utils.global_variables.put("date", () -> DateTimeFormatter.ofPattern("dd/MM/yyyy").format(LocalDateTime.now()));
        Utils.global_variables.put("time", () -> DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalDateTime.now()));
        Utils.global_variables.put("uptime", () -> Utils.uptimeDateFormater.format(new Date().getTime() - Utils.started_time));

        if (Config.SERVER.sendServerStartStopMessages.get())
            Utils.sendInfoMessage(Config.SERVER.serverStartMessage.get());

        if (Config.SERVER.enableDiscordPresence.get())
        {
            Timer presence_timer = new Timer();
            TimerTask presence_timer_task = new TimerTask()
            {
                @Override
                public void run()
                {
                    if (Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.CONNECTED)
                        Utils.updateDiscordPresence();
                    else if (Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.SHUTDOWN || Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.SHUTTING_DOWN)
                        this.cancel();
                }
            };
            presence_timer.schedule(presence_timer_task, 0, Config.SERVER.discordBotPresenceUpdatePeriod.get() * 1000);
        }

        if (Config.SERVER.enableEditableChannelTopicUpdate.get())
        {
            Timer channel_topic_timer = new Timer();
            TimerTask channel_topic_timer_task = new TimerTask()
            {
                @Override
                public void run()
                {
                    if (Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.CONNECTED)
                        Utils.updateChannelTopic();
                    else if (Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.SHUTDOWN || Minecraft2Discord.getDiscordBot().getStatus() == JDA.Status.SHUTTING_DOWN)
                        this.cancel();
                }
            };
            channel_topic_timer.schedule(channel_topic_timer_task, 0, Config.SERVER.editableChannelTopicUpdatePeriod.get() * 1000);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (Config.SERVER.chatChannel.get() == event.getChannel().getIdLong() && (!event.getAuthor().isBot() || Config.SERVER.allowBotSendMessage.get()) && !Utils.isM2DBotOrWebhook(event.getAuthor(), Utils.discordWebhook) && event.getMember() != null)
        {
            if (event.getMessage().getContentRaw().startsWith("/")
                && ((Config.SERVER.commandAllowedUsersIds.get().contains(event.getAuthor().getIdLong())
                || event.getMember().getRoles().stream().map(Role::getIdLong).anyMatch(Config.SERVER.commandAllowedRolesIds.get()::contains))
                || Config.SERVER.allowedCommandForEveryone.get().stream().anyMatch(s -> event.getMessage().getContentDisplay().substring(1).startsWith(s))))
            {

                CommandSource commandSource = new CommandSource(new DiscordCommandSource(event.getChannel()),
                    new Vec3d(ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD).getSpawnPoint()),
                    Vec2f.ZERO,
                    ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
                    4,
                    "Discord",
                    new StringTextComponent("Discord"),
                    ServerLifecycleHooks.getCurrentServer(),
                    null);

                if (event.getMessage().getContentRaw().startsWith("/help"))
                {
                    Commands commandsManager = ServerLifecycleHooks.getCurrentServer().getCommandManager();

                    RootCommandNode<CommandSource> discordHelpCommandRoot = new RootCommandNode<>();
                    for (String command : Config.SERVER.allowedCommandForEveryone.get())
                    {
                        CommandNode<CommandSource> node = commandsManager.getDispatcher().getRoot().getChild(command.split(" ")[0]);
                        for (Object child : Arrays.stream(command.split(" ")).skip(1).toArray())
                        {
                            node.addChild(node.getChild((String) child));
                        }
                        discordHelpCommandRoot.addChild(node);
                    }

                    Map<CommandNode<CommandSource>, String> lvt_2_1_ = commandsManager.getDispatcher().getSmartUsage(discordHelpCommandRoot, commandSource);

                    for (String lvt_4_1_ : lvt_2_1_.values())
                    {
                        commandSource.sendFeedback(new StringTextComponent("/" + lvt_4_1_), false);
                    }
                } else
                {
                    ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(commandSource, event.getMessage().getContentDisplay());
                }
            } else if (event.getMessage().getContentRaw().startsWith("/")
                && Config.SERVER.allowedCommandForEveryone.get().stream().noneMatch(s -> event.getMessage().getContentDisplay().substring(1).startsWith(s)))
            {
                if (!Config.SERVER.commandMissingPermissionsMessage.get().isEmpty())
                    Utils.sendMessage(event.getTextChannel(), Config.SERVER.commandMissingPermissionsMessage.get());
            } else
            {
                ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(new StringTextComponent(EmojiParser.parseToAliases("<Discord - " + (Config.SERVER.useNickname.get() ? event.getMember().getEffectiveName() : event.getAuthor().getName()) + "> " + event.getMessage().getContentDisplay())));
            }
        }
    }
}
