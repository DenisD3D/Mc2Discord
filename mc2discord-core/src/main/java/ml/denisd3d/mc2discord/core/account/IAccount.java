package ml.denisd3d.mc2discord.core.account;

import discord4j.core.object.entity.User;

import java.util.UUID;

public interface IAccount {
    void loadDiscordIds();

    void saveDiscordIds();

    boolean contains(Object gameProfile);

    boolean add(UUID uuid, long id);

    boolean remove(UUID uuid);

    void updateCommands();

    void checkAllDiscordAccount();

    void sendLinkSuccess(UUID uuid);

    String getInGameName(UUID uuid);

    void removeIfPresent(User user);
}
