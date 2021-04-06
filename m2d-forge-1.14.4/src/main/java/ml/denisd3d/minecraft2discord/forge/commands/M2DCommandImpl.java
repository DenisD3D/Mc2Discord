package ml.denisd3d.minecraft2discord.forge.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ml.denisd3d.minecraft2discord.core.M2DCommands;
import ml.denisd3d.minecraft2discord.core.Minecraft2Discord;
import ml.denisd3d.minecraft2discord.forge.MinecraftImpl;
import ml.denisd3d.minecraft2discord.forge.storage.HiddenPlayerEntry;
import ml.denisd3d.minecraft2discord.forge.storage.HiddenPlayerList;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

import java.util.Collection;

public class M2DCommandImpl {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HIDDEN = new SimpleCommandExceptionType(new StringTextComponent("Player already in the list"));
    private static final SimpleCommandExceptionType PLAYER_NOT_HIDDEN = new SimpleCommandExceptionType(new StringTextComponent("Player wasn't in the list"));


    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("minecraft2discord")
                .requires(commandSource -> commandSource.hasPermissionLevel(3))
                .then(Commands.literal("status").executes(context -> {
                    M2DCommands.getStatus().forEach(s -> context.getSource().sendFeedback(new StringTextComponent(s), false));
                    return 1;
                })).then(Commands.literal("restart").executes(context -> {
                    M2DCommands.restart().forEach(s -> context.getSource().sendFeedback(new StringTextComponent(s), false));
                    return 1;
                })).then(Commands.literal("upload").executes(context -> {
                    String[] result = M2DCommands.upload();
                    context.getSource().sendFeedback(new StringTextComponent(result[0])
                            .appendSibling(new StringTextComponent(result[1]).setStyle(new Style()
                                    .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result[1]))
                                    .setColor(TextFormatting.BLUE)
                                    .setUnderlined(true))), false);
                    return 1;
                })).then(Commands.literal("invite").executes(context -> {
                    String result = M2DCommands.getInviteLink();
                    context.getSource().sendFeedback(new StringTextComponent(result).setStyle(new Style()
                            .setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result))
                            .setColor(TextFormatting.BLUE)
                            .setUnderlined(true)), false);
                    return 1;
                })).then(Commands.literal("hidden")
                        .then(Commands.literal("list").executes((p_198878_0_) -> listHiddenPlayers(p_198878_0_.getSource()))).then(Commands.literal("add").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_198879_0_, p_198879_1_) -> {
                                    PlayerList playerlist = p_198879_0_.getSource().getServer().getPlayerList();
                                    HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
                                    return ISuggestionProvider.suggest(playerlist.getPlayers().stream().filter((p_198871_1_) -> !hiddenPlayerList.isHidden(p_198871_1_.getGameProfile())).map((p_200567_0_) -> p_200567_0_.getGameProfile().getName()), p_198879_1_);
                                }).executes((p_198875_0_) -> addPlayers(p_198875_0_.getSource(), GameProfileArgument.getGameProfiles(p_198875_0_, "targets")))
                        )).then(Commands.literal("remove").then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((p_198881_0_, p_198881_1_) -> ISuggestionProvider.suggest(((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList.getKeys(), p_198881_1_))
                                .executes((p_198870_0_) -> removePlayers(p_198870_0_.getSource(), GameProfileArgument.getGameProfiles(p_198870_0_, "targets")))))
                        .then(Commands.literal("reload").executes((p_198882_0_) -> reload(p_198882_0_.getSource())))

                ));
    }

    private static int reload(CommandSource source) {
        ((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).readHiddenPlayerList();
        source.sendFeedback(new StringTextComponent("Reloaded hidden players"), true);
        return 1;
    }

    private static int addPlayers(CommandSource source, Collection<GameProfile> players) throws CommandSyntaxException {
        HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
        int i = 0;

        for (GameProfile gameprofile : players) {
            if (!hiddenPlayerList.isHidden(gameprofile)) {
                HiddenPlayerEntry hiddenPlayerEntry = new HiddenPlayerEntry(gameprofile);
                hiddenPlayerList.addEntry(hiddenPlayerEntry);
                source.sendFeedback(new StringTextComponent(TextComponentUtils.getDisplayName(gameprofile).getString() + " is now hidden."), true);
                ++i;
            }
        }

        if (i == 0) {
            throw PLAYER_ALREADY_HIDDEN.create();
        } else {
            return i;
        }
    }

    private static int removePlayers(CommandSource source, Collection<GameProfile> players) throws CommandSyntaxException {
        HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
        int i = 0;

        for (GameProfile gameprofile : players) {
            if (hiddenPlayerList.isHidden(gameprofile)) {
                HiddenPlayerEntry hiddenPlayerEntry = new HiddenPlayerEntry(gameprofile);
                hiddenPlayerList.removeEntry(hiddenPlayerEntry);
                source.sendFeedback(new StringTextComponent(TextComponentUtils.getDisplayName(gameprofile).getString() + " is now visible"), true);
                ++i;
            }
        }

        if (i == 0) {
            throw PLAYER_NOT_HIDDEN.create();
        } else {
            source.getServer().kickPlayersNotWhitelisted(source);
            return i;
        }
    }

    private static int listHiddenPlayers(CommandSource source) {
        String[] astring = ((MinecraftImpl) Minecraft2Discord.INSTANCE.iMinecraft).hiddenPlayerList.getKeys();
        if (astring.length == 0) {
            source.sendFeedback(new StringTextComponent("There are no hidden player"), false);
        } else {
            source.sendFeedback(new StringTextComponent("There are " + astring.length + " hidden players: " + String.join(", ", astring)), false);
        }

        return astring.length;
    }
}
