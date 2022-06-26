package ml.denisd3d.mc2discord.core;

import ml.denisd3d.mc2discord.core.account.IAccount;
import ml.denisd3d.mc2discord.core.config.core.Channels;
import ml.denisd3d.mc2discord.core.entities.Global;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface IMinecraft {
    void sendMessage(String content, HashMap<String, String> attachments);

    void executeCommand(String command, int permissionLevel, long messageChannelId, Channels.SendMode mode);

    Global getServerData();

    String executeHelpCommand(int permissionLevel, List<String> commands);

    boolean isPlayerHidden(UUID id, String name);

    String getNewVersion();

    String getEnvInfo();

    @Nullable
    IAccount getIAccount();

    default String translateKey(LangManager langManager, String translationKey) {
        return langManager.formatMessage(translationKey);
    }
}
