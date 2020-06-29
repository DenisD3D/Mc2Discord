package ml.denis3d.minecraft2discord;

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
        //Discord config
        public final ForgeConfigSpec.ConfigValue<String> botToken;
        public final ForgeConfigSpec.ConfigValue<List<? extends Long>> commandAllowedUsersIds;
        public final ForgeConfigSpec.ConfigValue<List<? extends Long>> commandAllowedRolesIds;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allowedCommandForEveryone;
        ////Channels ids
        public final ForgeConfigSpec.LongValue chatChannel;
        public final ForgeConfigSpec.LongValue infoChannel;
        public final ForgeConfigSpec.LongValue editableTopicChannel;
        public final ForgeConfigSpec.LongValue editableVoiceChannel;

        //Features on/off
        public final ForgeConfigSpec.BooleanValue sendJoinLeftMessages;
        public final ForgeConfigSpec.BooleanValue sendAdvancementMessages;
        public final ForgeConfigSpec.BooleanValue sendDeathsMessages;
        public final ForgeConfigSpec.BooleanValue sendServerStartStopMessages;
        public final ForgeConfigSpec.BooleanValue enabledDiscordCommand;
        public final ForgeConfigSpec.BooleanValue enableDiscordPresence;
        public final ForgeConfigSpec.BooleanValue enableEditableChannelTopicUpdate;
        public final ForgeConfigSpec.BooleanValue enableEditableVoiceChannelUpdate;
        public final ForgeConfigSpec.BooleanValue useNickname;
        public final ForgeConfigSpec.BooleanValue useDiscordWebhooks;
        public final ForgeConfigSpec.BooleanValue allowBotSendMessage;

        //Messages
        public final ForgeConfigSpec.ConfigValue<String> joinMessage;
        public final ForgeConfigSpec.ConfigValue<String> leftMessage;
        public final ForgeConfigSpec.ConfigValue<String> advancementMessage;
        public final ForgeConfigSpec.ConfigValue<String> deathMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStartMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStopMessage;
        public final ForgeConfigSpec.ConfigValue<String> discordPresence;
        public final ForgeConfigSpec.ConfigValue<String> editableChannelTopicUpdateMessage;
        public final ForgeConfigSpec.ConfigValue<String> editableChannelTopicOfflineMessage;
        public final ForgeConfigSpec.ConfigValue<String> editableVoiceChannelUpdateMessage;
        public final ForgeConfigSpec.ConfigValue<String> editableVoiceChannelOfflineMessage;
        public final ForgeConfigSpec.ConfigValue<String> commandMissingPermissionsMessage;
        public final ForgeConfigSpec.ConfigValue<String> noneWebhookChatMessageFormat;

        //Misc
        public final ForgeConfigSpec.ConfigValue<String> discordInviteLink;
        public final ForgeConfigSpec.ConfigValue<String> discordPictureAPI;
        public final ForgeConfigSpec.LongValue discordBotPresenceUpdatePeriod;
        public final ForgeConfigSpec.LongValue editableChannelTopicUpdatePeriod;
        public final ForgeConfigSpec.LongValue editableVoiceChannelUpdatePeriod;
        public final ForgeConfigSpec.BooleanValue allowInterModComms;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> hideAdvancementList;


        public Server(ForgeConfigSpec.Builder builder)
        {
            //Discord config
            builder.comment(" Config for data coming from your discord server")
                .push("Discord");

            botToken = builder
                .comment(" Token for your Discord bot. Look at curseforge project page if you don't know how to get one")
                    .define("botToken", "");

            commandAllowedUsersIds = builder
                    .comment(" List of the members who is allowed to use command from discord (include op and no-op command)")
                    .defineList("commandAllowedUsersIds", ArrayList::new, e -> e instanceof Long);

            commandAllowedRolesIds = builder
                    .comment(" List of the roles who is allowed to use command from discord (include op and no-op command)")
                    .defineList("commandAllowedRolesIds", ArrayList::new, e -> e instanceof Long);

            allowedCommandForEveryone = builder
                    .comment(" List of the commands that everyone is allowed to use in discord")
                    .defineList("allowedCommandForEveryone", () -> Lists.newArrayList("help"), e -> e instanceof String);

            ////Channels ids config
            builder.comment(" Discord Channels Ids")
                .push("Channels");

            chatChannel = builder
                .comment(" Chat : All players messages")
                .defineInRange("chat", 0, 0, Long.MAX_VALUE);

            infoChannel = builder
                .comment(" Info : Death, Advancement, Join / Left, Server Start/Stop...")
                .defineInRange("info", 0, 0, Long.MAX_VALUE);

            editableTopicChannel = builder
                .comment(" EditableTopic : id of the channel of which the topic can be updated periodically (see enableEditableChannelTopicUpdate, editableChannelTopicUpdateMessage, editableChannelTopicUpdatePeriod")
                .defineInRange("editableTopic", 0, 0, Long.MAX_VALUE);

            editableVoiceChannel = builder
                    .comment(" EditableVoiceChannel : id of the voice channel of which the name can be updated periodically (see enableEditableVoiceChannelUpdate, editableVoiceChannelUpdateMessage, editableVoiceChannelUpdatePeriod")
                    .defineInRange("editableVoiceChannel", 0, 0, Long.MAX_VALUE);

            ////END Channels ids config
            builder.pop();

            //END Discord config
            builder.pop();

            //Features on/off
            builder.comment(" Toggle features on and off (Send in info channel)")
                .push("Features");

            sendJoinLeftMessages = builder
                    .comment(" Send players join/left messages.")
                    .define("sendJoinLeftMessages", true);

            sendAdvancementMessages = builder
                    .comment(" Send players advancements messages.")
                    .define("sendAdvancementsMessages", true);

            sendDeathsMessages = builder
                    .comment(" Send players deaths messages.")
                    .define("sendDeathsMessages", true);

            sendServerStartStopMessages = builder
                    .comment(" Send server start/stop messages.")
                .define("sendServerStartStopMessages", true);

            enabledDiscordCommand = builder
                .comment(" Enable or disable the discord command that show an invite link (cf : Misc.discordInviteLink)")
                .define("enabledDiscordCommand", false);

            enableDiscordPresence = builder
                .comment(" Enable or disable discord presence of the bot (ex : Playing .......)")
                .define("enableDiscordPresence", false);

            enableEditableChannelTopicUpdate = builder
                .comment(" Enable or disable discord channel topic update (description of the channel that can be but next to his name)")
                .define("enableEditableChannelTopicUpdate", false);

            enableEditableVoiceChannelUpdate = builder
                    .comment(" Enable or disable discord voice channel update (title of the channel)")
                    .define("enableEditableVoiceChannelUpdate", false);

            useNickname = builder
                .comment(" Enable or disable the use of nickname (name specific to a guild). If false it will use the username for everyone")
                .define("useNickname", true);

            useDiscordWebhooks = builder
                .comment(" Enable or disable the use of webhooks (custom profile picture and name in discord). If false message will be send with the bot account in the form : player_name : message")
                .define("useDiscordWebhooks", true);

            allowBotSendMessage = builder
                .comment("Allow bot message to be relayed on server chat")
                .define("allowBotSendMessage", false);

            //END Features on/off
            builder.pop();

            //Message configuration
            builder.comment(" Customise the messages here. Global variable available for all the following fields : $online_players$, $max_players$, $motd$, $mc_version$, $server_hostname$, $server_port$, $unique_player$, $date$, $time$, $uptime$")
                    .push("Messages");

            joinMessage = builder
                .comment(" $1 = player_name")
                    .define("joinMessage", "$1 joined the game.");

            leftMessage = builder
                .comment(" $1 = player_name")
                    .define("leftMessage", "$1 left the game.");

            advancementMessage = builder
                .comment(" $1 = player_name, $2 = advancement, $3 = advancement description")
                    .define("advancementMessage", "$1 has made the advancement $2. $3");

            deathMessage = builder
                .comment(" $1 = formatted death message, $2 = player_name, $3 unformatted death message, $4 death   ")
                    .define("deathMessage", "$1 $2.");

            serverStartMessage = builder
                    .comment("Global variable only")
                .define("serverStartMessage", "Server has started.");

            serverStopMessage = builder
                .comment("Global variable only")
                .define("serverStopMessage", "Server has stopped.");

            discordPresence = builder
                .comment("Global variable only")
                .define("discordPresence", "$online_players$ / $max_players$ players");

            editableChannelTopicUpdateMessage = builder
                .comment("Global variable only")
                .define("editableChannelTopicUpdateMessage", "$online_players$ / $max_players$ players");

            editableChannelTopicOfflineMessage = builder
                    .comment("Global variable only")
                    .define("editableChannelTopicOfflineMessage", "OFFLINE");

            editableVoiceChannelUpdateMessage = builder
                    .comment("Global variable only")
                    .define("editableVoiceChannelUpdateMessage", "$online_players$ / $max_players$ players");

            editableVoiceChannelOfflineMessage = builder
                    .comment("Global variable only")
                    .define("editableVoiceChannelOfflineMessage", "OFFLINE");

            commandMissingPermissionsMessage = builder
                .comment("Message send when someone execute a command in discord without having the permission. Empty to disable. Global variable only.")
                .define("commandMissingPermissionsMessage", "You do not have enough permission or the command doesn't exist");

            noneWebhookChatMessageFormat = builder
                .comment("Format for the message sent by the bot while not using webhooks. Support discord markdown. Default to '**PLAYER** : MESSAGE. $1 = player_name, $2 = message")
                .define("noneWebhookChatMessageFormat", "**$1** : $2");

            //END Message configuration
            builder.pop();

            //Misc configuration
            builder.comment(" Some miscellaneous configuration")
                .push("Misc");

            discordInviteLink = builder
                .comment(" Invite link for your discord server")
                .define("discordInviteLink", "Invite link not set");

            discordPictureAPI = builder
                .comment(" API url for discord profile picture. $1 is player name and $2 is the player UUID.")
                .define("discordPictureAPI", "https://mc-heads.net/head/$1");

            discordBotPresenceUpdatePeriod = builder
                .comment(" Period between to presence update in seconds. Default 180s (3 minutes)")
                .defineInRange("discordBotPresenceUpdatePeriod", 180, 1, Long.MAX_VALUE);

            editableChannelTopicUpdatePeriod = builder
                .comment(" Period between to presence update in seconds. Default 600s (10 minutes)")
                .defineInRange("editableChannelTopicUpdatePeriod", 600, 1, Long.MAX_VALUE);

            editableVoiceChannelUpdatePeriod = builder
                    .comment(" Period between to presence update in seconds. Default 600s (10 minutes)")
                    .defineInRange("editableVoiceChannelUpdatePeriod", 600, 1, Long.MAX_VALUE);

            allowInterModComms = builder
                .comment(" Allow other mod to send message to discord using Minecraft2Discord")
                .define("allowInterModComms", true);

            hideAdvancementList = builder
                .comment(" List of advancement that will not be sent. 'modid' will remove every advancement from a mod (ex:minecraft) and modid:path/to/advancement will remove every advancement under this path (ex: minecraft:nether")
                .defineList("hideAdvancementList", ArrayList::new, e -> e instanceof String);

            //END Misc configuration
            builder.pop();
        }
    }
}
