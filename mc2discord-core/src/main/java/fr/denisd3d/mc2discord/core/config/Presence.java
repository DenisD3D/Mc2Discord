package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;

public class Presence {
    @SerdeKey("message")
    @SerdeComment("config.style.presence.message.comment")
    @DefaultValue("config.style.presence.message.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate message;

    @SerdeKey("type")
    @SerdeComment("config.status.presence.type.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String type = "PLAYING";

    @SerdeKey("update")
    @SerdeComment("config.status.presence.update.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public long update = 60L;

    @SerdeKey("link")
    @SerdeComment("config.status.presence.link.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String link = "";

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;
}
