package ml.denis3d.minecraft2discord;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

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
        ////Channels ids
        public final ForgeConfigSpec.ConfigValue<Long> chatChannel;
        public final ForgeConfigSpec.ConfigValue<Long> infoChannel;

        //Features on/off
        public final ForgeConfigSpec.BooleanValue sendJoinLeftMessages;
        public final ForgeConfigSpec.BooleanValue sendAdvancementMessages;
        public final ForgeConfigSpec.BooleanValue sendDeathsMessages;
        public final ForgeConfigSpec.BooleanValue sendServerStartStopMessages;
        public final ForgeConfigSpec.BooleanValue discordCommandEnabled;

        //Messages
        public final ForgeConfigSpec.ConfigValue<String> joinMessage;
        public final ForgeConfigSpec.ConfigValue<String> leftMessage;
        public final ForgeConfigSpec.ConfigValue<String> advancementMessage;
        public final ForgeConfigSpec.ConfigValue<String> deathMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStartMessage;
        public final ForgeConfigSpec.ConfigValue<String> serverStopMessage;

        //Misc
        public final ForgeConfigSpec.ConfigValue<String> discordInviteLink;
        public final ForgeConfigSpec.ConfigValue<String> discordPictureAPI;
        public final ForgeConfigSpec.BooleanValue allowInterModComms;


        public Server(ForgeConfigSpec.Builder builder) {
            //Discord config
            builder.comment(" Config for data coming from your discord server")
                    .push("Discord");

            botToken = builder
                    .comment(" Token for your Discord bot. Look at curseforge project one if you don't know how to get one")
                    .define("botToken", "");

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
                    .comment(" Enable or disable the discord command that show an invite link (cf : Misc.discordInviteLink")
                    .define("discordCommandEnabled", true);

            //END Features on/off
            builder.pop();

            //Message configuration
            builder.comment(" Customise the messages here")
                    .push("Messages");

            joinMessage = builder
                    .comment(" $1 = player name")
                    .define("joinMessage", "$1 joined the game.");

            leftMessage = builder
                    .comment(" $1 = player name")
                    .define("leftMessage", "$1 left the game.");

            advancementMessage = builder
                    .comment(" $1 = player name, $2 = advancement")
                    .define("advancementMessage", "$1 has made the advancement $2.");

            deathMessage = builder
                    .comment(" $1 = player name, $2 = death message")
                    .define("deathMessage", "$1 $2.");

            serverStartMessage = builder
                    .comment("No variable")
                    .define("serverStartMessage", "Server has started.");

            serverStopMessage = builder
                    .comment("No variable")
                    .define("serverStopMessage", "Server has stopped.");

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

            //END Misc configuration
            builder.pop();
        }
    }
}
