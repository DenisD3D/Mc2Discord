package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.entities.GlobalEntity;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public interface IMinecraft {
    /**
     * Translate a config message, may be overridden to add version specific translations
     * @param langManager The lang manager instance
     * @param translationKey The translation key
     * @return The translated message
     */
    default String translateKey(LangManager langManager, String translationKey) {
        return langManager.formatMessage(translationKey);
    }

    /**
     * Send a message to the minecraft server chat
     * @param content The message content
     * @param attachments Files that may be attached to the message, as an array of name and urls
     * @param referencedContent The message being replied to
     * @param senderId The Discord user id of the sender for quick @
     */
    void sendMessage(String content, HashMap<String, String> attachments, @Nullable String referencedContent, @Nullable String senderId);

    /**
     * Create help command text with all commands available for the permission level and the listed commands
     * @param permissionLevel The permission level granted
     * @param commands The list of additional commands to add to the help command
     * @return The help command text
     */
    String executeHelpCommand(Integer permissionLevel, List<String> commands);

    /**
     * Execute a command in the minecraft server
     * @param command The command to execute
     * @param permissionLevel The permission level granted, if < of the command permission level, the command will not be executed
     * @param channelId The channel id where the command was executed to send the response
     */
    void executeCommand(String command, int permissionLevel, Snowflake channelId);

    /**
     * Check if a new version of the mod is available
     * @return The new version number or null if no new version is available
     */
    String getNewVersion();

    /**
     * Get minecraft crash report datas (list of mods, java version, ...)
     * @return The crash report datas
     */
    String getEnvInfo();

    /**
     * Get the minecraft server data (version, players, ...)
     * @return The minecraft server data
     */
    GlobalEntity getServerData();

    /**
     * Get name of a player from his UUID, even if he is offline
     * @param uuid The player UUID
     * @return The player name
     */
    String getPlayerNameFromUUID(UUID uuid);

    /**
     * Register account related commands if account system is enabled
     */
    void registerAccountCommands();
}
