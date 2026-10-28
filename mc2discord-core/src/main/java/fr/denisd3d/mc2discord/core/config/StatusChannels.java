package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;

import java.util.ArrayList;
import java.util.List;

public class StatusChannels {

    @SerdeKey("Channel")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<StatusChannel> channels = new ArrayList<>();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;

    public static class StatusChannel {
        @SerdeKey("id")
        @SerdeComment("config.status_channels.id.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public Snowflake channel_id = M2DUtils.NIL_SNOWFLAKE;

        @SerdeKey("name_message")
        @SerdeComment("config.status_channels.name_message.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public RandomTemplate name_message = new RandomTemplate("${online_players} / ${max_players}");

        @SerdeKey("topic_message")
        @SerdeComment("config.status_channels.topic_message.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public RandomTemplate topic_message = new RandomTemplate("${online_players} / ${max_players}");

        @SerdeKey("update_period")
        @SerdeComment("config.status_channels.update_period.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public long update_period = 610;

        @SerdeKey("comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String comment;
    }
}
