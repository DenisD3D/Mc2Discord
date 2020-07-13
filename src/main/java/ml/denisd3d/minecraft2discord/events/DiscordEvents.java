package ml.denisd3d.minecraft2discord.events;

import com.vdurmont.emoji.EmojiParser;
import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.ExtensionUtils;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.commands.CustomHelpCommand;
import ml.denisd3d.minecraft2discord.commands.DiscordCommandSource;
import ml.denisd3d.minecraft2discord.managers.*;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.command.CommandSource;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class DiscordEvents extends ListenerAdapter
{
    CommandSource commandSource = new CommandSource(new DiscordCommandSource(),
        Vec3d.ZERO,
        Vec2f.ZERO,
        ServerLifecycleHooks.getCurrentServer().getWorld(DimensionType.OVERWORLD),
        4,
        "Discord",
        new StringTextComponent("Discord"),
        ServerLifecycleHooks.getCurrentServer(),
        null);

    public static void sendStartMessage()
    {
        if (Config.SERVER.startStopEnabled.get())
        {
            MessageManager.sendFormattedMessage(ChannelManager.getInfoChannel(), Config.SERVER.startMessage.get());
        }
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        Minecraft2Discord.getLogger().info("Discord bot logged as " + event.getJDA().getSelfUser().getName());
        Minecraft2Discord.setUsername(Config.SERVER.serverName.get().isEmpty() ? event.getJDA().getSelfUser().getName() : Config.SERVER.serverName.get());
        Minecraft2Discord.setAvatarURL(Config.SERVER.serverAvatarURL.get().isEmpty() ? event.getJDA().getSelfUser().getAvatarUrl() : Config.SERVER.serverAvatarURL.get());

        Minecraft2Discord.isRunning = true;

        MinecraftForge.EVENT_BUS.register(MinecraftEvents.class);

        WebhookManager.addWebhookClient(Config.SERVER.infoChannel.get(), DiscordEvents::sendStartMessage);
        WebhookManager.addWebhookClient(Config.SERVER.chatChannel.get(), null);

        ShutdownManager.registerShutdownHook();
        StatusManager.register();

        Minecraft2Discord.extensions.forEach(m2DExtension -> m2DExtension.onReady(event));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        if (ExtensionUtils.executeExtensions(m2DExtension -> m2DExtension.onDiscordMessage(event))) // Execute extensions
            return;

        if (Config.SERVER.chatChannel.get() == event.getChannel().getIdLong())
        {
            // If message isn't a webhook and if config is set to relay bot messages or if the message is not from a bot
            if (!event.isWebhookMessage() && (Config.SERVER.enableBotMessagesRelay.get() || !event.getAuthor().isBot()))
            {
                if (event.getJDA().getSelfUser().getIdLong() != event.getAuthor().getIdLong())
                {
                    if (event.getMessage().getContentRaw().startsWith(Config.SERVER.commandPrefix.get())) // We are processing a command
                    {
                        if (Config.SERVER.commandAllowedUsersIds.get().contains(event.getAuthor().getIdLong()) // User is allowed
                            || event.getMember().getRoles().stream().map(Role::getIdLong).anyMatch(Config.SERVER.commandAllowedRolesIds.get()::contains)) // One role of the user is allowed
                        {
                            ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(commandSource, event.getMessage().getContentDisplay().substring(Config.SERVER.commandPrefix.get().length()));
                        } else if (Config.SERVER.allowedCommandForEveryone.get().stream().anyMatch(s -> event.getMessage().getContentDisplay().substring(1).startsWith(s))) // The command to process is allowed for everyone
                        {
                            if (event.getMessage().getContentRaw().startsWith(Config.SERVER.commandPrefix.get() + "help"))
                            {
                                CustomHelpCommand.execute(commandSource);
                            } else
                            {
                                ServerLifecycleHooks.getCurrentServer().getCommandManager().handleCommand(commandSource, event.getMessage().getContentDisplay().substring(Config.SERVER.commandPrefix.get().length()));
                            }
                        } else // User is missing permission
                        {
                            if (!Config.SERVER.missingPermissionsMessage.get().isEmpty())
                            {
                                MessageManager.sendFormattedMessage(event.getTextChannel(), Config.SERVER.missingPermissionsMessage.get());
                                return; // We have processed the message. If missingPermissionsMessage is empty process like a chat message
                            }
                        }
                        MessageManager.sendQuotesMessage(event.getTextChannel(), DiscordCommandSource.answer);
                        DiscordCommandSource.answer = "";
                        return; // We have processed the message
                    }

                    //If the message haven't already be processed
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().sendMessage(new StringTextComponent(EmojiParser.parseToAliases("<Discord - " + (Config.SERVER.nicknameEnabled.get() ? event.getMember().getEffectiveName() : event.getAuthor().getName()) + "> " + event.getMessage().getContentDisplay())));
                }
            }
        }
    }
}
