package fr.denisd3d.mc2discord.minecraft.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import discord4j.discordjson.possible.Possible;
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
import net.minecraft.network.chat.*;
import net.minecraft.server.players.PlayerList;
import reactor.util.function.Tuple2;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class M2DCommandImpl {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HIDDEN = new SimpleCommandExceptionType(new TextComponent("Player already in the list"));
    private static final SimpleCommandExceptionType PLAYER_NOT_HIDDEN = new SimpleCommandExceptionType(new TextComponent("Player wasn't in the list"));
    private static final SimpleCommandExceptionType PLAYER_ALREADY_LINKED = new SimpleCommandExceptionType(new TextComponent("Player is already linked"));
    private static final SimpleCommandExceptionType PLAYER_NOT_LINKED = new SimpleCommandExceptionType(new TextComponent("Player isn't linked"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("mc2discord").requires(commandSource -> commandSource.hasPermission(2))
                .then(Commands.literal("status").executes(context -> {
                    List<String> result = M2DCommands.getStatus();
                    result.forEach(s -> context.getSource().sendSuccess(new TextComponent(s), false));
                    return 1;
                })).then(Commands.literal("restart").executes(context -> {
                    context.getSource().sendSuccess(new TextComponent(M2DCommands.restart()), false);
                    return 1;
                })).then(Commands.literal("upload").executes(context -> {
                    String[] result = M2DCommands.upload();
                    context.getSource().sendSuccess(new TextComponent(result[0]).append(new TextComponent(result[1]).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result[1])).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)).withUnderlined(true))), false);
                    return 1;
                })).then(Commands.literal("invite").executes(context -> {
                    String result = M2DCommands.getInviteLink();
                    context.getSource().sendSuccess(new TextComponent(result).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result)).withColor(TextColor.fromLegacyFormat(ChatFormatting.BLUE)).withUnderlined(true)), false);
                    return 1;
                })).then(Commands.literal("hidden_players")
                        .then(Commands.literal("list").executes((context) -> {
                            String result = M2DCommands.listHiddenPlayers();
                            context.getSource().sendSuccess(new TextComponent(result), false);
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

                                            result.forEach(s -> context.getSource().sendSuccess(new TextComponent(s), false));
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

                                            result.forEach(s -> context.getSource().sendSuccess(new TextComponent(s), false));
                                            return 1;
                                        })))
                        .then(Commands.literal("reload").executes((context) -> {
                            try {
                                context.getSource().sendSuccess(new TextComponent(M2DCommands.reloadHiddenPlayers()), false);
                                return 1;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }))

                ).then(Commands.literal("linked_players").then(Commands.literal("list").executes((context) -> {
                            Tuple2<String, Integer> result = M2DCommands.listLinkedPlayers();
                            context.getSource().sendSuccess(new TextComponent(result.getT1()), false);
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

                                                    context.getSource().sendSuccess(new TextComponent(result), false);
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

                                            result.forEach(s -> context.getSource().sendSuccess(new TextComponent(s), false));
                                            return 1;
                                        })))
                        .then(Commands.literal("reload").executes((context) -> {
                            try {
                                context.getSource().sendSuccess(new TextComponent(M2DCommands.reloadLinkedPlayers()), false);
                                return 1;
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })))
                .then(Commands.literal("tellraw").then(Commands.argument("type", StringArgumentType.word()).suggests((context, suggestionsBuilder) -> SharedSuggestionProvider.suggest(List.of("chat", "info", "custom"), suggestionsBuilder))
                        .then(Commands.argument("message", ComponentArgument.textComponent()).executes((context) -> {
                            MessageManager.sendMessage(List.of(StringArgumentType.getString(context, "type")), ComponentUtils.updateForEntity(context.getSource(), ComponentArgument.getComponent(context, "message"), null, 0).getString(), Possible.of(Mc2Discord.INSTANCE.vars.mc2discord_display_name), Possible.of(Mc2Discord.INSTANCE.vars.mc2discord_avatar)).subscribe();
                            context.getSource().sendSuccess(new TextComponent("Message sent to Discord"), false);
                            return 1;
                        })))));
    }
}
