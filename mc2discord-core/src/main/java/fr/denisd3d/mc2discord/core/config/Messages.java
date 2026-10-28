package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;

public class Messages {
    @SerdeKey("start")
    @SerdeComment("config.messages.start.comment")
    @DefaultValue("config.messages.start.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate start;

    @SerdeKey("stop")
    @SerdeComment("config.messages.stop.comment")
    @DefaultValue("config.messages.stop.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate stop;

    @SerdeKey("join")
    @SerdeComment("config.messages.join.comment")
    @DefaultValue("config.messages.join.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate join;

    @SerdeKey("leave")
    @SerdeComment("config.messages.leave.comment")
    @DefaultValue("config.messages.leave.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate leave;

    @SerdeKey("death")
    @SerdeComment("config.messages.death.comment")
    @DefaultValue("config.messages.death.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate death;

    @SerdeKey("advancement")
    @SerdeComment("config.messages.advancement.comment")
    @DefaultValue("config.messages.advancement.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate advancement;

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;
}
