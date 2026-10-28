package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Channels {
    @SerdeKey("Channel")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<Channel> channels = new ArrayList<>();

    public static class Channel {
        @SerdeKey("id")
        @SerdeComment("config.channels.id.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public Snowflake channel_id = M2DUtils.NIL_SNOWFLAKE;

        @SerdeKey("subscriptions")
        @SerdeComment("config.channels.subscriptions.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public List<String> subscriptions = new ArrayList<>();

        @SerdeKey("mode")
        @SerdeComment("config.channels.mode.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public SendMode mode = SendMode.WEBHOOK;

        @SerdeKey("comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String comment;

        public Channel(String... subscriptions) {
            this.subscriptions.addAll(Arrays.asList(subscriptions));
        }

        @SuppressWarnings("unused")
        public Channel() { // Allow NightConfig to create new instances
        }

        public enum SendMode {
            WEBHOOK, PLAIN_TEXT, EMBED
        }
    }

}
