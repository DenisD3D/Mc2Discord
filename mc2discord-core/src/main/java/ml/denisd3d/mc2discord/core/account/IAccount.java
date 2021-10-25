package ml.denisd3d.mc2discord.core.account;

import ml.denisd3d.mc2discord.core.Mc2Discord;

import java.util.UUID;

public interface IAccount {
    void loadDiscordIds();

    void saveDiscordIds();

    boolean contains(Object gameProfile);

    boolean add(UUID uuid, long id);

    boolean remove(UUID uuid);

    void updateCommands();

    void checkDiscordPseudoForAllDiscordAccount();

    void sendLinkSuccess(UUID uuid);

    String getInGameName(UUID uuid);
}
