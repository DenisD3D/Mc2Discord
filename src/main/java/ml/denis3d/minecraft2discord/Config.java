package ml.denis3d.minecraft2discord;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

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
        ////Channels ids
        public final ForgeConfigSpec.ConfigValue<Long> chatChannel;
        public final ForgeConfigSpec.ConfigValue<Long> infoChannel;

        //Features on/off
        public final ForgeConfigSpec.BooleanValue sendJoinLeftMessages;
        public final ForgeConfigSpec.BooleanValue sendAdvancementMessages;
        public final ForgeConfigSpec.BooleanValue sendDeathsMessages;
        public final ForgeConfigSpec.BooleanValue sendServerStartStopMessages;
        public final ForgeConfigSpec.BooleanValue discordCommandEnabled;
        public final ForgeConfigSpec.BooleanValue enableDiscordPresence;
        public final ForgeConfigSpec.BooleanValue useDiscordWebhooks;

        //Messages
        public final ForgeConfigSpec.ConfigValue<String> joinMessage;
        public final ForgeConfigSpec.ConfigValue<String> leftMessage;
        public final ForgeConfigSpec.ConfigValue<String> advancementMessage;
        public final ForgeConfigSpec.ConfigValue<String> deathMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStartMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStopMessage;
        public final ForgeConfigSpec.ConfigValue<String> discordPresence;

        //Misc
        public final ForgeConfigSpec.ConfigValue<String> discordInviteLink;
        public final ForgeConfigSpec.ConfigValue<String> discordPictureAPI;
        public final ForgeConfigSpec.BooleanValue allowInterModComms;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> hideAdvancementList;

        public Server(ForgeConfigSpec.Builder builder) {
            //Discord config
            builder.comment(" Config for data coming from your discord server")
                    .push("Discord");

            botToken = builder
                    .comment(" Token for your Discord bot. Look at curseforge project one if you don't know how to get one")
                    .define("botToken", "");

            commandAllowedUsersIds = builder
                    .comment(" List of the members who is allowed to use command from discord (include op and no-op command)")
                    .defineList("commandAllowedUsersIds", Lists.newArrayList(), o -> o instanceof Long);

            commandAllowedRolesIds = builder
                    .comment(" List of the roles who is allowed to use command from discord (include op and no-op command)")
                    .defineList("commandAllowedRolesIds", Lists.newArrayList(), o -> o instanceof Long);

            ////Channels ids config
            builder.comment(" Discord Channels Ids")
                    .push("Channels");

            chatChannel = builder
                    .comment(" Chat : All players messages")
                    .define("chat", 000000000000000000l);

            infoChannel = builder
                    .comment(" Info : Death, Advancement, Join / Left, Server Start/Stop...")
                    .define("info", 000000000000000000l);

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

            discordCommandEnabled = builder
                    .comment(" Enable or disable the discord command that show an invite link (cf : Misc.discordInviteLink)")
                    .define("discordCommandEnabled", true);

            enableDiscordPresence = builder
                    .comment(" Enable or disable discord presence of the bot (ex : Playing .......)")
                    .define("enableDiscordPresence", false);

            useDiscordWebhooks = builder
                    .comment(" Enable or disable the use of webhooks (custom profile picture and name in discord). If false message will be send with the bot account in the form : player_name : message")
                    .define("useDiscordWebhooks", true);

            //END Features on/off
            builder.pop();

            //Message configuration
            builder.comment(" Customise the messages here. Global variable : $online_players$, $max_players$, $motd$, $mc_version$, $server_hostname$, $server_port$")
                    .push("Messages");

            joinMessage = builder
                    .comment(" $1 = player name")
                    .define("joinMessage", "$1 joined the game.");

            leftMessage = builder
                    .comment(" $1 = player name")
                    .define("leftMessage", "$1 left the game.");

            advancementMessage = builder
                    .comment(" $1 = player name, $2 = advancement, $3 = advancement description")
                    .define("advancementMessage", "$1 has made the advancement $2. $3");

            deathMessage = builder
                    .comment(" $1 = player name, $2 = death message")
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
                    .define("discordPictureAPI", "https://minotar.net/avatar/$1");

            allowInterModComms = builder
                    .comment(" Allow other mod to send message to discord using Minecraft2Discord")
                    .define("discordCommandEnabled", true);

            hideAdvancementList = builder
                    .comment(" List of advancement that will not be sent. 'modid' will remove every advancement from a mod (ex:minecraft) and modid:path/to/advancement will remove every advancement under this path (ex: minecraft:nether")
                    .defineList("hideAdvancementList", Lists.newArrayList(), o -> o instanceof String);

            //END Misc configuration
            builder.pop();
        }
    }
}
