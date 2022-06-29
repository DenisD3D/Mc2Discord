package ml.denisd3d.mc2discord.forge.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import ml.denisd3d.mc2discord.core.LangManager;
import ml.denisd3d.mc2discord.core.M2DCommands;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.forge.MinecraftImpl;
import ml.denisd3d.mc2discord.forge.account.AccountImpl;
import ml.denisd3d.mc2discord.forge.storage.DiscordIdEntry;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerEntry;
import ml.denisd3d.mc2discord.forge.storage.HiddenPlayerList;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.server.management.PlayerList;
import net.minecraft.server.management.UserListEntry;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class M2DCommandImpl {
    private static final SimpleCommandExceptionType PLAYER_ALREADY_HIDDEN = new SimpleCommandExceptionType(new StringTextComponent("Player already in the list"));
    private static final SimpleCommandExceptionType PLAYER_NOT_HIDDEN = new SimpleCommandExceptionType(new StringTextComponent("Player wasn't in the list"));
    private static final SimpleCommandExceptionType PLAYER_ALREADY_LINKED = new SimpleCommandExceptionType(new StringTextComponent("Player is already linked"));
    private static final SimpleCommandExceptionType PLAYER_NOT_LINKED = new SimpleCommandExceptionType(new StringTextComponent("Player isn't linked"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("mc2discord")
                .requires(commandSource -> commandSource.hasPermission(3))
                .then(Commands.literal("status").executes(context -> {
                    M2DCommands.getStatus().forEach(s -> context.getSource().sendSuccess(new StringTextComponent(s), false));
                    return 1;
                }))
                .then(Commands.literal("restart").executes(context -> {
                    M2DCommands.restart().forEach(s -> context.getSource().sendSuccess(new StringTextComponent(s), false));
                    return 1;
                }))
                .then(Commands.literal("upload").executes(context -> {
                    String[] result = M2DCommands.upload();
                    context.getSource()
                            .sendSuccess(new StringTextComponent(result[0]).append(new StringTextComponent(result[1]).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result[1]))
                                    .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
                                    .setUnderlined(true))), false);
                    return 1;
                }))
                .then(Commands.literal("invite").executes(context -> {
                    String result = M2DCommands.getInviteLink();
                    context.getSource()
                            .sendSuccess(new StringTextComponent(result).withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result))
                                    .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
                                    .setUnderlined(true)), false);
                    return 1;
                }))
                .then(Commands.literal("hidden")
                        .then(Commands.literal("list").executes((p_198878_0_) -> listHiddenPlayers(p_198878_0_.getSource())))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((p_198879_0_, p_198879_1_) -> {
                                            PlayerList playerlist = p_198879_0_.getSource().getServer().getPlayerList();
                                            HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
                                            return ISuggestionProvider.suggest(playerlist.getPlayers()
                                                    .stream()
                                                    .filter((p_198871_1_) -> !hiddenPlayerList.isHidden(p_198871_1_.getGameProfile()))
                                                    .map((p_200567_0_) -> p_200567_0_.getGameProfile().getName()), p_198879_1_);
                                        })
                                        .executes((p_198875_0_) -> addHiddenPlayers(p_198875_0_.getSource(), GameProfileArgument.getGameProfiles(p_198875_0_, "targets")))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((p_198881_0_, p_198881_1_) -> ISuggestionProvider.suggest(((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList.getUserList(), p_198881_1_))
                                        .executes((p_198870_0_) -> removeHiddenPlayers(p_198870_0_.getSource(), GameProfileArgument.getGameProfiles(p_198870_0_, "targets")))))
                        .then(Commands.literal("reload").executes((p_198882_0_) -> reloadHiddenPlayers(p_198882_0_.getSource()))))
                .then(Commands.literal("linked")
                        .then(Commands.literal("list").executes((p_198878_0_) -> listLinkedPlayers(p_198878_0_.getSource())))
                        .then(Commands.literal("add")
                                .then(Commands.argument("targets", UnknowableGameProfileArgument.unknowableGameProfileArgument())
                                        .suggests((p_198879_0_, p_198879_1_) -> {
                                            if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking) {
                                                return ISuggestionProvider.suggest(new String[0], p_198879_1_);
                                            }

                                            PlayerList playerlist = p_198879_0_.getSource().getServer().getPlayerList();
                                            return ISuggestionProvider.suggest(playerlist.getPlayers()
                                                    .stream()
                                                    .filter(serverPlayer -> !AccountImpl.discordIds.contains(serverPlayer.getGameProfile()))
                                                    .map((p_200567_0_) -> p_200567_0_.getGameProfile().getName()), p_198879_1_);
                                        })
                                        .then(Commands.argument("discord_id", IntegerArgumentType.integer(0))
                                                .executes((p_198875_0_) -> addLinkedPlayers(p_198875_0_.getSource(), UnknowableGameProfileArgument.getGameProfiles(p_198875_0_, "targets"), IntegerArgumentType.getInteger(p_198875_0_, "discord_id"))))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .suggests((p_198881_0_, p_198881_1_) -> {
                                            if (Mc2Discord.INSTANCE.m2dAccount == null) {
                                                return ISuggestionProvider.suggest(new String[0], p_198881_1_);
                                            }
                                            return ISuggestionProvider.suggest(AccountImpl.discordIds.getEntries()
                                                    .stream()
                                                    .map(UserListEntry::getUser)
                                                    .filter(Objects::nonNull)
                                                    .map(GameProfile::getName)
                                                    .filter(Objects::nonNull), p_198881_1_);
                                        })
                                        .executes((p_198870_0_) -> removeLinkedPlayers(p_198870_0_.getSource(), GameProfileArgument.getGameProfiles(p_198870_0_, "targets")))))
                        .then(Commands.literal("reload").executes((p_198882_0_) -> reloadLinkedPlayers(p_198882_0_.getSource())))

                ));
    }

    private static int reloadLinkedPlayers(CommandSource source) {
        if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking) {
            source.sendFailure(new StringTextComponent("Account linking features isn't enabled."));
            return 0;
        }
        Mc2Discord.INSTANCE.m2dAccount.iAccount.loadDiscordIds();
        source.sendSuccess(new StringTextComponent(LangManager.translate("commands.linked.reload")), true);
        return 1;
    }

    private static int removeLinkedPlayers(CommandSource source, Collection<GameProfile> targets) throws CommandSyntaxException {
        if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking) {
            source.sendFailure(new StringTextComponent("Account linking features isn't enabled."));
            return 0;
        }
        IAccount iAccount = Mc2Discord.INSTANCE.m2dAccount.iAccount;
        int i = 0;

        for (GameProfile gameprofile : targets) {
            if (iAccount.contains(gameprofile)) {
                iAccount.remove(gameprofile.getId());
                source.sendSuccess(new StringTextComponent(LangManager.translate("commands.linked.unlinked", TextComponentUtils.getDisplayName(gameprofile)
                        .getString())), true);
                ++i;
            }
        }

        if (i == 0) {
            throw PLAYER_NOT_LINKED.create();
        } else {
            return i;
        }
    }

    private static int addLinkedPlayers(CommandSource source, String target, int discord_id) throws CommandSyntaxException {
        if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking) {
            source.sendFailure(new StringTextComponent("Account linking features isn't enabled."));
            return 0;
        }

        IAccount iAccount = Mc2Discord.INSTANCE.m2dAccount.iAccount;

        UUID uuid;
        GameProfile gameProfile;
        try {
            uuid = UUID.fromString(target);
            gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
        } catch (IllegalArgumentException e) {
            uuid = null;
            gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(target);
        }
        if (gameProfile == null) {
            gameProfile = uuid != null ? new GameProfile(uuid, null) : null;
        }


        if (gameProfile == null || gameProfile.getId() == null) {
            source.sendFailure(new StringTextComponent("Can't link this player by name. Please use a valid player uuid"));
            return 0;
        }

        if (!iAccount.contains(gameProfile)) {
            iAccount.add(gameProfile.getId(), discord_id);
            source.sendSuccess(new StringTextComponent(LangManager.translate("commands.linked.linked", TextComponentUtils.getDisplayName(gameProfile)
                    .getString())), true);
            return 1;
        } else {
            throw PLAYER_ALREADY_LINKED.create();
        }
    }

    private static int listLinkedPlayers(CommandSource source) {
        if (Mc2Discord.INSTANCE.m2dAccount == null || !Mc2Discord.INSTANCE.config.features.account_linking) {
            source.sendFailure(new StringTextComponent("Account linking features isn't enabled."));
            return 0;
        }

        Collection<DiscordIdEntry> entries = AccountImpl.discordIds.getEntries();
        if (entries.size() == 0) {
            source.sendSuccess(new StringTextComponent(LangManager.translate("commands.linked.empty")), false);
        } else {
            source.sendSuccess(new StringTextComponent(LangManager.translate("commands.linked.list", entries.size(), entries.stream()
                    .map(UserListEntry::getUser)
                    .filter(Objects::nonNull)
                    .map(gameProfile -> {
                        if (gameProfile.getName() != null) {
                            return gameProfile.getName();
                        } else {
                            GameProfile gameProfile1 = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(gameProfile.getId());
                            if (gameProfile1 != null && gameProfile1.getName() != null) {
                                return gameProfile1.getName();
                            } else {
                                return gameProfile.getId().toString();
                            }
                        }
                    })
                    .collect(Collectors.joining(", ")))), false);
        }

        return entries.size();
    }

    private static int reloadHiddenPlayers(CommandSource source) {
        ((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).readHiddenPlayerList();
        source.sendSuccess(new StringTextComponent(LangManager.translate("commands.hidden.reload")), true);
        return 1;
    }

    private static int addHiddenPlayers(CommandSource source, Collection<GameProfile> players) throws CommandSyntaxException {
        HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
        int i = 0;

        for (GameProfile gameprofile : players) {
            if (!hiddenPlayerList.isHidden(gameprofile)) {
                HiddenPlayerEntry hiddenPlayerEntry = new HiddenPlayerEntry(gameprofile);
                hiddenPlayerList.add(hiddenPlayerEntry);
                source.sendSuccess(new StringTextComponent(LangManager.translate("commands.hidden.hidden", TextComponentUtils.getDisplayName(gameprofile)
                        .getString())), true);
                ++i;
            }
        }

        if (i == 0) {
            throw PLAYER_ALREADY_HIDDEN.create();
        } else {
            return i;
        }
    }

    private static int removeHiddenPlayers(CommandSource source, Collection<GameProfile> players) throws CommandSyntaxException {
        HiddenPlayerList hiddenPlayerList = ((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList;
        int i = 0;

        for (GameProfile gameprofile : players) {
            if (hiddenPlayerList.isHidden(gameprofile)) {
                HiddenPlayerEntry hiddenPlayerEntry = new HiddenPlayerEntry(gameprofile);
                hiddenPlayerList.remove(hiddenPlayerEntry);
                source.sendSuccess(new StringTextComponent(LangManager.translate("commands.hidden.visible", TextComponentUtils.getDisplayName(gameprofile)
                        .getString())), true);
                ++i;
            }
        }

        if (i == 0) {
            throw PLAYER_NOT_HIDDEN.create();
        } else {
            return i;
        }
    }

    private static int listHiddenPlayers(CommandSource source) {
        String[] hiddenPlayers = ((MinecraftImpl) Mc2Discord.INSTANCE.iMinecraft).hiddenPlayerList.getUserList();
        if (hiddenPlayers.length == 0) {
            source.sendSuccess(new StringTextComponent(LangManager.translate("commands.hidden.empty")), false);
        } else {
            source.sendSuccess(new StringTextComponent(LangManager.translate("commands.hidden.list", hiddenPlayers.length, String.join(", ", hiddenPlayers))), false);
        }

        return hiddenPlayers.length;
    }
}
