package ml.denisd3d.minecraft2discord;


import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;


public class Config {
    public static final ForgeConfigSpec SERVER_SPECS;
    public static final Server SERVER;

    static {
        Pair<Server, ForgeConfigSpec> serverPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER_SPECS = serverPair.getRight();
        SERVER = serverPair.getLeft();
    }

    public static class Server {
        public final ForgeConfigSpec.ConfigValue<String> token;
        public final ForgeConfigSpec.LongValue chatChannel;
        public final ForgeConfigSpec.LongValue infoChannel;

        public final ForgeConfigSpec.BooleanValue joinLeaveEnabled;
        public final ForgeConfigSpec.ConfigValue<String> joinMessage;
        public final ForgeConfigSpec.ConfigValue<String> leaveMessage;

        public final ForgeConfigSpec.BooleanValue startStopEnabled;
        public final ForgeConfigSpec.ConfigValue<String> startMessage;
        public final ForgeConfigSpec.ConfigValue<String> stopMessage;
        public final ForgeConfigSpec.ConfigValue<String> crashMessage;

        public final ForgeConfigSpec.BooleanValue advancementEnabled;
        public final ForgeConfigSpec.ConfigValue<String> advancementMessage;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> hiddenAdvancementsList;

        public final ForgeConfigSpec.BooleanValue deathEnabled;
        public final ForgeConfigSpec.ConfigValue<String> deathMessage;

        public final ForgeConfigSpec.BooleanValue presenceEnabled;
        public final ForgeConfigSpec.ConfigValue<String> presenceMessage;
        public final ForgeConfigSpec.LongValue presenceUpdatePeriod;

        public final ForgeConfigSpec.BooleanValue topicEnabled;
        public final ForgeConfigSpec.LongValue topicChannel;
        public final ForgeConfigSpec.ConfigValue<String> topicMessage;
        public final ForgeConfigSpec.LongValue topicUpdatePeriod;

        public final ForgeConfigSpec.BooleanValue nameEnabled;
        public final ForgeConfigSpec.LongValue nameChannel;
        public final ForgeConfigSpec.ConfigValue<String> nameMessage;
        public final ForgeConfigSpec.LongValue nameUpdatePeriod;

        public final ForgeConfigSpec.BooleanValue discordCommandEnabled;
        public final ForgeConfigSpec.ConfigValue<String> inviteLink;

        public final ForgeConfigSpec.ConfigValue<List<? extends Long>> commandAllowedUsersIds;
        public final ForgeConfigSpec.ConfigValue<List<? extends Long>> commandAllowedRolesIds;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allowedCommandForEveryone;
        public final ForgeConfigSpec.ConfigValue<String> missingPermissionsMessage;
        public final ForgeConfigSpec.ConfigValue<String> commandPrefix;

        public final ForgeConfigSpec.BooleanValue webhooksEnabled;
        public final ForgeConfigSpec.ConfigValue<String> nameFormat;
        public final ForgeConfigSpec.ConfigValue<String> messageFormat;
        public final ForgeConfigSpec.ConfigValue<String> avatarAPI;
        public final ForgeConfigSpec.ConfigValue<String> serverName;
        public final ForgeConfigSpec.ConfigValue<String> serverAvatarURL;

        public final ForgeConfigSpec.BooleanValue nicknameEnabled;
        public final ForgeConfigSpec.BooleanValue enableBotMessagesRelay;

        public final ForgeConfigSpec.ConfigValue<String> dateFormat;
        public final ForgeConfigSpec.ConfigValue<String> uptimeFormat;
        public final ForgeConfigSpec.ConfigValue<String> chatFormat;

        public final ForgeConfigSpec.BooleanValue mentionsEnabled;
        public final ForgeConfigSpec.BooleanValue codeblocksEnabled;

        public final ForgeConfigSpec.LongValue chatWebhookId;
        public final ForgeConfigSpec.LongValue infoWebhookId;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.comment(" Configuration file for Minecraft2Discord\n" +
                    "   - Curseforge : https://www.curseforge.com/minecraft/mc-mods/minecraft2discord\n" +
                    "   - Github : https://github.com/Denis3D/Minecraft2Discord\n" +
                    "   - Wiki : https://github.com/Denis3D/Minecraft2Discord/wiki\n" +
                    "   - Discord : https://discord.gg/rzzd76c\n" +
                    " Any message can be disabled by putting an empty value (\"\")\n" +
                    " Discord markdown is supported\n" +
                    " Variable can be used to make the message dynamic\n" +
                    " There are in the form ${name_of_the_variable}\n" +
                    " Global variables are usable everywhere :\n" +
                    "   - global_online_players, global_max_players, global_unique_player, global_motd, global_mc_version, global_server_hostname, global_server_port, global_date, global_uptime\n" +
                    " The following groups of variables are usable only when mentioned above the field\n" +
                    "   - Player : player_name, player_uuid, player_health\n" +
                    "   - Death : death_message, death_unformatted_message, death_attacker_name, death_attacker_health, death_key\n" +
                    "   - Advancement : advancement_title, advancement_description\n" +
                    "   - Message : message\n" +
                    "   - Discord User : discord_user_name, discord_user_tag, discord_user_discriminator\n\n" +
                    " You don't know how do edit this file ? You can found tutorials on the github wiki. You can also join the discord to get some help").push("Discord");
            {
                token = builder
                        .comment(" Token of the discord bot")
                        .define("token", "");

                //Channels
                builder.comment(" Discord Channels Ids").push("Channels");
                {
                    chatChannel = builder
                            .comment(" All players messages")
                            .defineInRange("chat", 0, 0, Long.MAX_VALUE);

                    infoChannel = builder
                            .comment(" All system messages (e.g. Death, Advancement, Join/Leave, Server Start/Stop)")
                            .defineInRange("info", 0, 0, Long.MAX_VALUE);
                }
                builder.pop();
            }
            builder.pop();

            builder.comment(" Messages settings").push("Messages");
            {
                // Start & Stop
                builder.comment(" Start & stop settings").push("StartStop");
                {
                    startStopEnabled = builder
                            .comment(" Enable start & stop messages")
                            .define("enabled", true);

                    startMessage = builder
                            .comment(" Start Message")
                            .define("start", "The server has started.");

                    stopMessage = builder
                            .comment(" Stop Message")
                            .define("stop", "The server has stopped.");

                    crashMessage = builder
                            .comment(" Crash Message\n May not work for every crash")
                            .define("crash", "The server has crashed.");
                }
                builder.pop();

                // Join & Left
                builder.comment(" Join & leave settings").push("JoinLeave");
                {
                    joinLeaveEnabled = builder
                            .comment(" Enable join & leave messages")
                            .define("enabled", true);

                    joinMessage = builder
                            .comment(" Join Message\n Variables : Player")
                            .define("join", "${player_name} joined the game.");

                    leaveMessage = builder
                            .comment(" Leave Message\n Variables : Player")
                            .define("leave", "${player_name} left the game.");
                }
                builder.pop();

                // Advancement
                builder.comment(" Advancement settings").push("Advancement");
                {
                    advancementEnabled = builder
                            .comment(" Enable advancements messages")
                            .define("enabled", true);

                    advancementMessage = builder
                            .comment(" Advancement Message\n Variables : Player, Advancement")
                            .define("message", "${player_name} has made the advancement ${advancement_title}. ${advancement_description}.");

                    hiddenAdvancementsList = builder
                            .comment(" List of advancements that will not be sent. \"modid\" will remove every advancement from a mod (e.g. \"minecraft\") and \"modid:path/to/advancement\" will remove every advancement under this path (e.g. \"minecraft:nether\")")
                            .defineList("hiddenAdvancementsList", ArrayList::new, e -> e instanceof String);
                }
                builder.pop();

                // Death
                builder.comment(" Death settings").push("Death");
                {
                    deathEnabled = builder
                            .comment(" Enable deaths messages")
                            .define("enabled", true);

                    deathMessage = builder
                            .comment(" Death Message\n Variables : Player, Death")
                            .define("message", "${death_message}");
                }
                builder.pop();
            }
            builder.pop();

            builder.comment(" Status Settings").push("Status");
            {
                // Presence
                builder.comment(" Presence settings").push("Presence");
                {
                    presenceEnabled = builder
                            .comment(" Enable presence for the bot (e.g. : Playing Minecraft)")
                            .define("enabled", true);

                    presenceMessage = builder
                            .comment(" Text set as presence for the bot")
                            .define("message", "${global_online_players} / ${global_max_players} players");

                    presenceUpdatePeriod = builder
                            .comment(" Period between two presence updates in seconds")
                            .defineInRange("updatePeriod", 60, 10, Long.MAX_VALUE);
                }
                builder.pop();

                // Topic
                builder.comment(" Topic settings : Edit the topic of a channel regularly").push("Topic");
                {
                    topicEnabled = builder
                            .comment(" Enable topic update")
                            .define("enabled", false);

                    topicChannel = builder
                            .comment(" Text channel where the topic is updated regularly")
                            .defineInRange("channel", 0, 0, Long.MAX_VALUE);

                    topicMessage = builder
                            .comment(" Text set as topic of the channel")
                            .define("message", "${global_online_players} / ${global_max_players} players");

                    topicUpdatePeriod = builder
                            .comment(" Period between two topic updates in seconds\n Due to discord limitation, minimum is 5 minutes (310s)")
                            .defineInRange("updatePeriod", 610, 310, Long.MAX_VALUE);
                }
                builder.pop();

                // Name
                builder.comment(" Channel Name settings : Edit the name of a channel regularly").push("ChannelName");
                {
                    nameEnabled = builder
                            .comment(" Enable channel name update")
                            .define("enabled", false);

                    nameChannel = builder
                            .comment(" Text or Voice channel where the name is updated regularly")
                            .defineInRange("channel", 0, 0, Long.MAX_VALUE);

                    nameMessage = builder
                            .comment(" Text set as name of the channel\n If it is a text channel, space and special character will be replaced by '-'")
                            .define("message", "Players : ${global_online_players} / ${global_max_players}");

                    nameUpdatePeriod = builder
                            .comment(" Period between two topic updates in seconds\n Due to discord limitation, minimum is 5 minutes (300s)")
                            .defineInRange("updatePeriod", 610, 310, Long.MAX_VALUE);
                }
                builder.pop();
            }
            builder.pop();

            builder.comment(" Commands settings").push("Commands");
            {
                // Discord command
                builder.comment(" Discord command settings").push("DiscordCommand");
                {
                    discordCommandEnabled = builder
                            .comment(" Enable the discord command that displays an invitation link")
                            .define("enabled", false);

                    inviteLink = builder
                            .comment("Invite link for your discord server")
                            .define("link", "Invite link not set.");
                }
                builder.pop();

                // Commands integration
                builder.comment(" Commands integration settings").push("CommandsIntegration");
                {
                    commandAllowedRolesIds = builder
                            .comment(" List of the roles that can use every command on discord (without quotes, separated by commas)")
                            .defineList("commandAllowedRolesIds", ArrayList::new, e -> e instanceof Long);

                    commandAllowedUsersIds = builder
                            .comment(" List of the members who can use every command on discord (without quotes, separated by commas)")
                            .defineList("commandAllowedUsersIds", ArrayList::new, e -> e instanceof Long);

                    allowedCommandForEveryone = builder
                            .comment("List of commands that everyone can use on discord")
                            .defineList("allowedCommandForEveryone", () -> Lists.newArrayList("help"), e -> e instanceof String);

                    missingPermissionsMessage = builder
                            .comment(" Message sent when someone executes a command on discord without enough permissions")
                            .define("errorMessage", "You don't have enough permission or the command doesn't exist");

                    commandPrefix = builder
                            .comment(" Prefix to execute Minecraft command on discord")
                            .define("prefix", "/");

                    codeblocksEnabled = builder
                            .comment(" Enable the use of codeblocks in commands reply")
                            .define("codeblocksEnabled", true);
                }
                builder.pop();
            }
            builder.pop();

            builder.comment(" Webhooks settings").push("Webhooks");
            {
                webhooksEnabled = builder
                        .comment(" Enable the use of webhooks (customized profile picture and name)")
                        .define("enabled", true);

                nameFormat = builder
                        .comment(" Format for the name of the webhook\n Variables : Player")
                        .define("nameFormat", "${player_name}");

                messageFormat = builder
                        .comment(" Format used when webhooks are disabled\n Variables : Player, Message")
                        .define("disabledFormat", "**${player_name}** : ${message}");

                avatarAPI = builder
                        .comment(" API url for webhook profile picture\n Variables : Player")
                        .define("avatarAPI", "https://mc-heads.net/head/${player_name}");

                builder.comment(" Server Account").push("Server");
                {
                    serverName = builder
                            .comment(" Name used to send message as the server\n Empty means bot name")
                            .define("name", "");

                    serverAvatarURL = builder
                            .comment(" Url to the avatar used to send message as the server\n Empty means bot avatar")
                            .define("avatarURL", "");
                }
                builder.pop();
            }
            builder.pop();

            builder.comment(" Some miscellaneous settings").push("Miscellaneous");
            {
                nicknameEnabled = builder
                        .comment(" Enable the use of nicknames. If false, account name will be used")
                        .define("nicknameEnabled", true);

                enableBotMessagesRelay = builder
                        .comment(" Should bots messages be sent to Minecraft chat")
                        .define("botMessageRelayEnabled", false);

                mentionsEnabled = builder
                        .comment(" Enable @everyone, @here and role mention")
                        .define("mentionsEnabled", false);
            }
            builder.pop();

            builder.comment(" Variables settings").push("Variables");
            {
                dateFormat = builder
                        .comment(" Change the format for the ${global_date} variable\n See available options on https://github.com/Denis3d/Minecraft2Discord/wiki/Date-&-Time-Formatting")
                        .define("dateFormat", "yyyy-MM-dd HH:mm:ss");

                uptimeFormat = builder
                        .comment(" Change the format for ${global_uptime} variable\n See available options \"Date and Time Patterns\" on https://github.com/Denis3d/Minecraft2Discord/wiki/Date-&-Time-Formatting")
                        .define("uptimeFormat", "HH:mm");

                chatFormat = builder
                        .comment(" Change the format for the prefix of message relayed from discord to minecraft\n  Variables : DiscordUser")
                        .define("chatFormat", "<Discord - ${discord_user_name}> ");
            }
            builder.pop();

            builder.comment(" Please do ne edit this part of the config even if you know what you are doing there.\n This part is used by the mod to save some data returned by discord needed for a future launch.\n").push("Minecraft2Discord data storage");
            {
                chatWebhookId = builder
                        .defineInRange("chatWebhookId", 0, 0, Long.MAX_VALUE);

                infoWebhookId = builder
                        .defineInRange("infoWebhookId", 0, 0, Long.MAX_VALUE);

            }
            builder.pop();
        }
    }
}
