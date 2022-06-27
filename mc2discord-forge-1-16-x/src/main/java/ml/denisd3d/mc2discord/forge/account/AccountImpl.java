package ml.denisd3d.mc2discord.forge.account;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import discord4j.core.object.entity.User;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.forge.storage.DiscordIdEntry;
import ml.denisd3d.mc2discord.forge.storage.DiscordIdList;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.io.File;
import java.util.UUID;

public class AccountImpl implements IAccount {
    public static final File FILE_DISCORD_IDS = new File("discord-ids.json");
    public static final DiscordIdList discordIds = new DiscordIdList(FILE_DISCORD_IDS);

    @Override
    public void loadDiscordIds() {
        try {
            discordIds.load();
        } catch (Exception exception) {
            Mc2Discord.logger.warn("Failed to load discord ids list: ", exception);
        }
    }

    @Override
    public void saveDiscordIds() {
        try {
            discordIds.save();
        } catch (Exception exception) {
            Mc2Discord.logger.warn("Failed to save discord ids list: ", exception);
        }
    }

    @Override
    public boolean contains(Object gameProfile) {
        return discordIds.contains((GameProfile) gameProfile);
    }

    @Override
    public void checkAllDiscordAccount() {
        if (Mc2Discord.INSTANCE.m2dAccount == null) {
            return;
        }
        for (DiscordIdEntry discordIdEntry : discordIds.getEntries()) {
            if (discordIdEntry.getUser() != null) {
                Mc2Discord.INSTANCE.m2dAccount.checkDiscordAccount(discordIdEntry.getUser().getId(), discordIdEntry.getUser()
                        .getName(), discordIdEntry.getDiscordId());
            }
        }
    }

    @Override
    public boolean add(UUID uuid, long id) {
        if (uuid != null) {
            GameProfile gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
            if (gameProfile == null) {
                gameProfile = new GameProfile(uuid, null);
            }
            discordIds.add(new DiscordIdEntry(gameProfile, id));
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(UUID uuid) {
        if (uuid != null) {
            GameProfile gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
            if (gameProfile != null) {
                discordIds.remove(gameProfile);
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateCommands() {
        CommandDispatcher<CommandSource> dispatcher = ServerLifecycleHooks.getCurrentServer().getCommands().getDispatcher();
        if (!Mc2Discord.INSTANCE.config.account.force_link) {
            if (dispatcher.getRoot().getChild(Mc2Discord.INSTANCE.config.account.link_command) == null) {
                LinkCommand.register(dispatcher);
            }
        }

        if (dispatcher.getRoot().getChild(Mc2Discord.INSTANCE.config.account.unlink_command) == null) {
            UnLinkCommand.register(dispatcher);
        }
    }

    @Override
    public void sendLinkSuccess(UUID uuid) {
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(new StringTextComponent(Mc2Discord.INSTANCE.config.account.messages.link_successful), Util.NIL_UUID);
        }
    }

    @Override
    public String getInGameName(UUID uuid) {
        GameProfile gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
        if (gameProfile != null) {
            return gameProfile.getName();
        }
        return null;
    }

    @Override
    public void removeIfPresent(User user) {
        discordIds.getEntries()
                .stream()
                .filter(discordIdEntry -> discordIdEntry.getDiscordId() == user.getId().asLong())
                .findFirst()
                .ifPresent(p_11387_ -> {
                    ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(p_11387_.getUser().getId());
                    if (player != null) {
                        player.connection.disconnect(new StringTextComponent(Mc2Discord.INSTANCE.config.account.messages.unlink_successful));
                    }
                    discordIds.remove(p_11387_);
                });
    }
}
