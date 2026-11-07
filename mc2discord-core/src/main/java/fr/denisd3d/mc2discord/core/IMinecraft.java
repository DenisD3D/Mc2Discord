package fr.denisd3d.mc2discord.core;

import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.Template;
import reactor.core.publisher.Mono;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;
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
     * Send a Discord message to the minecraft server chat
     * @param format The chat format, it may also contain the Minecraft formatting variables and ${reply} / ${attachments}
     * @param entities The entities of the message (message, member)
     * @param attachments Files that may be attached to the message, as a map of name and urls
     * @param reply The rendered reply format if the message is replying to another one
     * @param senderUsername The Discord username of the sender for quick @
     */
    void sendMessage(Template format, List<Entity> entities, Map<String, String> attachments, @Nullable String reply, @Nullable String senderUsername);

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
     * @return The output of the command, empty if the command did not output anything
     */
    Mono<String> executeCommand(String command, int permissionLevel);

    /**
     * Get the completions of a partially typed command, like the tab completion of the console
     * @param input The partially typed command
     * @param permissionLevel The permission level granted, commands above it are not suggested
     * @return The suggestions and usage hints for the end of the input
     */
    Mono<CommandCompletion> getCommandCompletion(String input, int permissionLevel);

    /**
     * @param suggestions The input completed with each suggestion
     * @param usages The input with the usage of each argument that can be typed at its end (eg: "give Steve diamond <count>"), shown in game when
     *        there is no suggestion
     */
    record CommandCompletion(List<String> suggestions, List<String> usages) {
        public static final CommandCompletion EMPTY = new CommandCompletion(List.of(), List.of());
    }

    /**
     * Get minecraft crash report datas (list of mods, java version, ...)
     * @return The crash report datas
     */
    String getEnvInfo();

    /**
     * @return The number of online players, hidden players excluded
     */
    int getOnlinePlayerCount();

    int getMaxPlayers();

    /**
     * @return The number of players that joined the server at least once
     */
    int getUniquePlayerCount();

    String getMotd();

    String getMinecraftVersion();

    String getServerHostname();

    int getServerPort();

    /**
     * @return The number of ticks per second, capped to the server tick rate
     */
    double getTps();

    /**
     * @return The mean time in milliseconds to process a tick
     */
    double getMspt();

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
