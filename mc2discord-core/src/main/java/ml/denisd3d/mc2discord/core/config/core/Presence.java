package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

public class Presence {
    @Path("message")
    @Comment("config.status.presence.message.comment")
    @DefaultValue("config.status.presence.message.value")
    @PreserveNotNull
    public String message;

    @Path("type")
    @Comment("config.status.presence.type.comment")
    @PreserveNotNull
    public String type = "PLAYING";

    @Path("update")
    @Comment("config.status.presence.update.comment")
    @PreserveNotNull
    public long update = 60L;

    @Path("link")
    @Comment("config.status.presence.link.comment")
    @PreserveNotNull
    public String link = "";
}
