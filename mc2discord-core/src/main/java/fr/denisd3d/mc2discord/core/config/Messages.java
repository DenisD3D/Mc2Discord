package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplateConverter;

public class Messages {
    @Path("start")
    @Comment("config.messages.start.comment")
    @DefaultValue("config.messages.start.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate start;

    @Path("stop")
    @Comment("config.messages.stop.comment")
    @DefaultValue("config.messages.stop.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate stop;

    @Path("join")
    @Comment("config.messages.join.comment")
    @DefaultValue("config.messages.join.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate join;

    @Path("leave")
    @Comment("config.messages.leave.comment")
    @DefaultValue("config.messages.leave.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate leave;

    @Path("death")
    @Comment("config.messages.death.comment")
    @DefaultValue("config.messages.death.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate death;

    @Path("advancement")
    @Comment("config.messages.advancement.comment")
    @DefaultValue("config.messages.advancement.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate advancement;

    @Path("comment")
    public String comment;
}
