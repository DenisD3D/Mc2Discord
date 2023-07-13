package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

public class Messages {
    @Path("start")
    @Comment("config.messages.start.comment")
    @DefaultValue("config.messages.start.value")
    @PreserveNotNull
    public String start;

    @Path("stop")
    @Comment("config.messages.stop.comment")
    @DefaultValue("config.messages.stop.value")
    @PreserveNotNull
    public String stop;

    @Path("join")
    @Comment("config.messages.join.comment")
    @DefaultValue("config.messages.join.value")
    @PreserveNotNull
    public String join;

    @Path("leave")
    @Comment("config.messages.leave.comment")
    @DefaultValue("config.messages.leave.value")
    @PreserveNotNull
    public String leave;

    @Path("death")
    @Comment("config.messages.death.comment")
    @DefaultValue("config.messages.death.value")
    @PreserveNotNull
    public String death;

    @Path("advancement")
    @Comment("config.messages.advancement.comment")
    @DefaultValue("config.messages.advancement.value")
    @PreserveNotNull
    public String advancement;
}
