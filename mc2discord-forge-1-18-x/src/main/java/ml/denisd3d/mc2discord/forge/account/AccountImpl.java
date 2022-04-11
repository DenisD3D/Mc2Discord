package ml.denisd3d.mc2discord.forge.account;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import ml.denisd3d.mc2discord.core.Mc2Discord;
import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.forge.storage.DiscordIdEntry;
import ml.denisd3d.mc2discord.forge.storage.DiscordIdList;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class AccountImpl implements IAccount {
    public static final File FILE_DISCORD_IDS = new File("discord-ids.json");
    private static final DiscordIdList discordIds = new DiscordIdList(FILE_DISCORD_IDS);

    @Override
    public void loadDiscordIds() {
        try {
            discordIds.load();
        } catch (Exception exception) {
//            Mc2Discord.logger.warn("Failed to load discord ids list: ", exception);
        }
    }

    @Override
    public void saveDiscordIds() {
        try {
            discordIds.save();
        } catch (Exception exception) {
//            Mc2Discord.logger.warn("Failed to save discord ids list: ", exception);
        }
    }

    @Override
    public boolean contains(Object gameProfile) {
        return discordIds.contains((GameProfile) gameProfile);
    }

    @Override
    public void checkDiscordPseudoForAllDiscordAccount() {
        if (Mc2Discord.INSTANCE.m2dAccount == null) {
            return;
        }
        for (DiscordIdEntry discordIdEntry : discordIds.getEntries()) {
            if (discordIdEntry.getUser() != null) {
                Mc2Discord.INSTANCE.m2dAccount
                        .renameDiscordAccountL(
                                discordIdEntry.getUser().getId(),
                                discordIdEntry.getUser().getName(),
                                discordIdEntry.getDiscordId());
            }
        }
    }

    @Override
    public boolean add(UUID uuid, long id) {
        if (uuid != null) {
            Optional<GameProfile> gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
            if (gameProfile.isPresent()) {
                discordIds.add(new DiscordIdEntry(gameProfile.get(), id));
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean remove(UUID uuid) {
        if (uuid != null) {
            Optional<GameProfile> gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
            if (gameProfile.isPresent()) {
                discordIds.remove(gameProfile.get());
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateCommands() {
        CommandDispatcher<CommandSourceStack> dispatcher = ServerLifecycleHooks.getCurrentServer().getCommands().getDispatcher();
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
        ServerPlayer player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
        if (player != null) {
            player.sendMessage(new TextComponent(Mc2Discord.INSTANCE.config.account.messages.link_successful), Util.NIL_UUID);
        }
    }

    @Override
    public String getInGameName(UUID uuid) {
        Optional<GameProfile> gameProfile = ServerLifecycleHooks.getCurrentServer().getProfileCache().get(uuid);
        return gameProfile.map(GameProfile::getName).orElse(null);
    }
}
