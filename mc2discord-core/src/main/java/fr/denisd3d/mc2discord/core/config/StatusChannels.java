package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.config.converters.SnowflakeConverter;
import ml.denisd3d.config4j.Comment;

import java.util.ArrayList;
import java.util.List;

public class StatusChannels {

    @Path("Channel")
    @PreserveNotNull
    public List<StatusChannel> channels = new ArrayList<>();

    public static class StatusChannel {
        @Path("id")
        @Comment("config.status_channels.id.comment")
        @Conversion(SnowflakeConverter.class)
        @PreserveNotNull
        public Snowflake channel_id = M2DUtils.NIL_SNOWFLAKE;

        @Path("name_message")
        @Comment("config.status_channels.name_message.comment")
        @PreserveNotNull
        public String name_message = "${online_players} / ${max_players}";

        @Path("topic_message")
        @Comment("config.status_channels.topic_message.comment")
        @PreserveNotNull
        public String topic_message = "${online_players} / ${max_players}";

        @Path("update_period")
        @Comment("config.status_channels.update_period.comment")
        @PreserveNotNull
        public long update_period = 610;
    }
}