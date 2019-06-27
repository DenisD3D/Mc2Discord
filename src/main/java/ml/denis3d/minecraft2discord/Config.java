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
        public final ForgeConfigSpec.ConfigValue<String> botToken;
        public final ForgeConfigSpec.ConfigValue<Long> chatChannel;
        public final ForgeConfigSpec.ConfigValue<Long> infoChannel;

        public final ForgeConfigSpec.BooleanValue showJoinLeftMessages;
        public final ForgeConfigSpec.BooleanValue showAdvancementMessages;
        public final ForgeConfigSpec.BooleanValue showDeathMessages;
        public final ForgeConfigSpec.ConfigValue<String> discordPictureAPI;

        public Server(ForgeConfigSpec.Builder builder) {
            builder.comment(" Config for the discord side")
                    .push("Discord");

            botToken = builder
                    .comment(" Place here your Minecraft bot TOKEN")
                    .define("botToken", "");


            builder.comment(" Discord Channels Ids")
                    .push("Channels");

            chatChannel = builder
                    .comment(" Chat : All players messages")
                    .define("chat", 000000000000000000l);

            infoChannel = builder
                    .comment(" Info : Death, success...")
                    .define("info", 000000000000000000l);
            builder.pop();

            showJoinLeftMessages = builder
                    .comment(" Show the player join/left messages (send in the info channel)")
                    .define("showJoinLeftMessages", true);

            showAdvancementMessages = builder
                    .comment(" Show the advancement messages (send in the info channel)")
                    .define("showAdvancementMessages", true);

            showDeathMessages = builder
                    .comment(" Show the death messages (send in the info channel)")
                    .define("showDeathMessages", true);

            discordPictureAPI = builder
                    .comment(" Define the url where to request the discord profile picture of the player. $1 is player name and $2 is the player UUID Default : minotar.net API.")
                    .define("discordPictureAPI", "https://minotar.net/avatar/$1");
            builder.pop();
        }
    }
}
