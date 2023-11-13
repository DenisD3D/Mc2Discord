package fr.denisd3d.mc2discord.minecraft;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.Vars;
import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.PlayerEntity;
import fr.denisd3d.mc2discord.core.events.LifecycleEvents;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandImpl;
import fr.denisd3d.mc2discord.minecraft.commands.DiscordCommandSource;
import fr.denisd3d.mc2discord.minecraft.commands.M2DCommandImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class Mc2DiscordMinecraft {
    public static CommandSourceStack commandSource;
    public static MinecraftServer server;

    public static void onRegisterCommands(CommandDispatcher<CommandSourceStack> commandSourceStackCommandDispatcher) {
        M2DCommandImpl.register(commandSourceStackCommandDispatcher);
        DiscordCommandImpl.register(commandSourceStackCommandDispatcher);
    }

    public static void onServerStarting(MinecraftServer minecraftServer) {
        server = minecraftServer;
        Vars.startTime = System.currentTimeMillis();
        Mc2Discord.INSTANCE = new Mc2Discord(new MinecraftImpl());
    }

    public static void onServerStarted(MinecraftServer minecraftServer) {
        LifecycleEvents.minecraftReady = true;
        commandSource = new CommandSourceStack(new DiscordCommandSource(), Vec3.ZERO, Vec2.ZERO, minecraftServer.overworld(), Integer.MAX_VALUE, "Discord", Component.literal("Discord"), minecraftServer, null);
        LifecycleEvents.mcOrDiscordReady();
    }


    public static void onServerStopped(MinecraftServer ignoredMinecraftServer) {
        LifecycleEvents.onShutdown();
        server = null;
    }

    public static void onCommand(ParseResults<CommandSourceStack> parseResults) {
        if (M2DUtils.isNotConfigured()) return;

        if (parseResults.getContext().getNodes().isEmpty()) return;

        if (!parseResults.getExceptions().isEmpty()) return;

        String command_name = parseResults.getContext().getNodes().get(0).getNode().getName();

        if (!Mc2Discord.INSTANCE.config.misc.broadcast_commands.contains(command_name)) return;

        CommandContext<CommandSourceStack> context = parseResults.getContext().build(parseResults.getReader().getString());

        try {
            String message = switch (command_name) {
                case "tellraw" -> {
                    StringRange selector_range = parseResults.getContext().getArguments().get("targets").getRange();
                    String target = context.getInput().substring(selector_range.getStart(), selector_range.getEnd());

                    if (target.equals("@s") && (context.getSource() == Mc2DiscordMinecraft.commandSource)) {
                        yield null; // Do not execute the vanilla command to prevent No player was found error but still return the message to discord
                    } else if (!target.equals("@a")) {  // Else if the target is not everyone it does not target discord
                        yield "";
                    }

                    yield ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getComponent(context, "message"), null, 0).getString();
                }
                case "say" ->
                        ChatType.bind(ChatType.SAY_COMMAND, context.getSource()).decorate(MessageArgument.getMessage(context, "message")).getString();
                case "me" ->
                        ChatType.bind(ChatType.EMOTE_COMMAND, context.getSource()).decorate(MessageArgument.getMessage(context, "action")).getString();
                default -> "";
            };

            if (message == null) return;
            if (message.isEmpty()) return;


            if (parseResults.getContext().getSource().getPlayer() != null) {
                PlayerEntity player = new PlayerEntity(parseResults.getContext().getSource().getPlayer().getGameProfile().getName(), parseResults.getContext().getSource().getPlayer().getDisplayName().getString(), parseResults.getContext().getSource().getPlayer().getGameProfile().getId());
                MessageManager.sendChatMessage(message, Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_display_name, List.of(player)), Entity.replace(Mc2Discord.INSTANCE.config.style.webhook_avatar_api, List.of(player))).subscribe();
            } else {
                MessageManager.sendInfoMessage("relayed_command", message).subscribe();
            }
        } catch (CommandSyntaxException ignored) {
        }
    }
}
