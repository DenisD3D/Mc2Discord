package fr.denisd3d.mc2discord.fabric.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fr.denisd3d.mc2discord.core.M2DCommands;
import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.MessageManager;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerList;
import fr.denisd3d.mc2discord.core.storage.LinkedPlayerList;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.players.PlayerList;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class M2DCommandImpl {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HIDDEN = new SimpleCommandExceptionType(Component.literal("Player already in the list"));
    private static final SimpleCommandExceptionType PLAYER_NOT_HIDDEN = new SimpleCommandExceptionType(Component.literal("Player wasn't in the list"));
    private static final SimpleCommandExceptionType PLAYER_ALREADY_LINKED = new SimpleCommandExceptionType(Component.literal("Player is already linked"));
    private static final SimpleCommandExceptionType PLAYER_NOT_LINKED = new SimpleCommandExceptionType(Component.literal("Player isn't linked"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mc2discord").requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("status").executes(context -> {
                    List<String> result = M2DCommands.getStatus();
                    result.forEach(s -> context.getSource().sendSuccess(() -> Component.literal(s), false));
                    return 1;
                })).then(Commands.literal("restart").executes(context -> {
                    context.getSource().sendSuccess(() -> Component.literal(M2DCommands.restart()), false);
                    return 1;
                })).then(Commands.literal("upload").executes(context -> {
                    String[] result = M2DCommands.upload();
                    context.getSource().sendSuccess(() -> Component.literal(result[0]).append(Component.literal(result[1]).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result[1])).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)).withUnderlined(true))), false);
                    return 1;
                })).then(Commands.literal("invite").executes(context -> {
                    String result = M2DCommands.getInviteLink();
                    context.getSource().sendSuccess(() -> Component.literal(result).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result)).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)).withUnderlined(true)), false);
                    return 1;
                })).then(Commands.literal("hidden_players")
                        .then(Commands.literal("list").executes((context) -> {
                            String result = M2DCommands.listHiddenPlayers();
                            context.getSource().sendSuccess(() -> Component.literal(result), false);
                            return 1;
                        })).then(Commands.literal("add")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((context, suggestionsBuilder) -> {
                                            PlayerList playerlist = context.getSource().getServer().getPlayerList();
                                            HiddenPlayerList hiddenPlayerList = Mc2Discord.INSTANCE.hiddenPlayerList;
                                            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((player) -> !hiddenPlayerList.contains(player.getUUID())).map((player) -> player.getGameProfile().getName()), suggestionsBuilder);
                                        }).executes((context) -> {
                                            Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(context, "targets");
                                            List<String> result = M2DCommands.addHiddenPlayers(targets.stream().map(GameProfile::getId).toList());

                                            if (result.isEmpty())
                                                throw PLAYER_ALREADY_HIDDEN.create();

                                            result.forEach(s -> context.getSource().sendSuccess(() -> Component.literal(s), false));
                                            return 1;
                                        })))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((context, suggestionsBuilder) -> {
                                            PlayerList playerlist = context.getSource().getServer().getPlayerList();
                                            HiddenPlayerList hiddenPlayerList = Mc2Discord.INSTANCE.hiddenPlayerList;
                                            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((player) -> hiddenPlayerList.contains(player.getUUID())).map((player) -> player.getGameProfile().getName()), suggestionsBuilder);
                                        }).executes((context) -> {
                                            Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(context, "targets");
                                            List<String> result = M2DCommands.removeHiddenPlayers(targets.stream().map(GameProfile::getId).toList());

                                            if (result.isEmpty())
                                                throw PLAYER_NOT_HIDDEN.create();

                                            result.forEach(s -> context.getSource().sendSuccess(() -> Component.literal(s), false));
                                            return 1;
                                        })))
                        .then(Commands.literal("reload").executes((context) -> {
                            context.getSource().sendSuccess(() -> {
                                try {
                                    return Component.literal(M2DCommands.reloadHiddenPlayers());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }, false);
                            return 1;
                        }))

                ).then(Commands.literal("linked_players").then(Commands.literal("list").executes((context) -> {
                            Tuple2<String, Integer> result = M2DCommands.listLinkedPlayers();
                            context.getSource().sendSuccess(() -> Component.literal(result.getT1()), false);
                            return 1;
                        })).then(Commands.literal("add")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((context, suggestionsBuilder) -> {
                                            PlayerList playerlist = context.getSource().getServer().getPlayerList();
                                            LinkedPlayerList linkedPlayerList = Mc2Discord.INSTANCE.linkedPlayerList;
                                            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((player) -> !linkedPlayerList.contains(player.getUUID())).map((player) -> player.getGameProfile().getName()), suggestionsBuilder);
                                        }).then(Commands.argument("discord_id", LongArgumentType.longArg(0))
                                                .executes((context) -> {
                                                    Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(context, "targets");
                                                    if (targets.size() > 1)
                                                        throw new RuntimeException("Only one player can be added at a time");

                                                    String result = M2DCommands.addLinkedPlayers(targets.iterator().next().getId(), LongArgumentType.getLong(context, "discord_id"));

                                                    if (result == null)
                                                        throw PLAYER_ALREADY_LINKED.create();

                                                    context.getSource().sendSuccess(() -> Component.literal(result), false);
                                                    return 1;
                                                }))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((context, suggestionsBuilder) -> {
                                            PlayerList playerlist = context.getSource().getServer().getPlayerList();
                                            LinkedPlayerList linkedPlayerList = Mc2Discord.INSTANCE.linkedPlayerList;
                                            return SharedSuggestionProvider.suggest(playerlist.getPlayers().stream().filter((player) -> linkedPlayerList.contains(player.getUUID())).map((player) -> player.getGameProfile().getName()), suggestionsBuilder);
                                        }).executes((context) -> {
                                            Collection<GameProfile> targets = GameProfileArgument.getGameProfiles(context, "targets");
                                            List<String> result = M2DCommands.removeLinkedPlayers(targets.stream().map(GameProfile::getId).toList());

                                            if (result.isEmpty())
                                                throw PLAYER_NOT_LINKED.create();

                                            result.forEach(s -> context.getSource().sendSuccess(() -> Component.literal(s), false));
                                            return 1;
                                        })))
                        .then(Commands.literal("reload").executes((context) -> {
                            context.getSource().sendSuccess(() -> {
                                try {
                                    return Component.literal(M2DCommands.reloadLinkedPlayers());
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }, false);
                            return 1;
                        })))
                .then(Commands.literal("tellraw").then(Commands.argument("message", ComponentArgument.textComponent()).executes((context) -> {
                    MessageManager.sendChatMessage(ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getComponent(context, "message"), null, 0).getString(), Mc2Discord.INSTANCE.vars.mc2discord_display_name, Mc2Discord.INSTANCE.vars.mc2discord_avatar).subscribe();
                    context.getSource().sendSuccess(() -> Component.literal("Message sent to Discord"), false);
                    return 1;
                }))));
    }
}
