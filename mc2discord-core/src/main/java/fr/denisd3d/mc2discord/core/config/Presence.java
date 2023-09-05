package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomString;
import fr.denisd3d.mc2discord.core.config.converters.RandomStringConverter;

public class Presence {
    @Path("message")
    @Comment("config.style.presence.message.comment")
    @DefaultValue("config.style.presence.message.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString message;

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

    @Path("comment")
    public String comment;
}