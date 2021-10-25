package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;

public class Status {
    @Path("Presence")
    @PreserveNotNull
    public Presence presence = new Presence();

    @Path("Channels")
    @Comment("config.status.channels.comment")
    @PreserveNotNull
    public StatusChannels statusChannels = new StatusChannels();

    public static class StatusChannel {
        @Path("id")
        @PreserveNotNull
        public long channel_id = 0L;

        @Path("update_period")
        @PreserveNotNull
        public long update_period = 610;

        @Path("name_message")
        @PreserveNotNull
        public String name_message = "${online_players} / ${max_players}";

        @Path("topic_message")
        @PreserveNotNull
        public String topic_message = "${online_players} / ${max_players}";
    }
}
