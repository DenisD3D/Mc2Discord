package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.mc2discord.core.config.converters.RandomString;
import fr.denisd3d.mc2discord.core.config.converters.RandomStringConverter;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

public class Messages {
    @Path("start")
    @Comment("config.messages.start.comment")
    @DefaultValue("config.messages.start.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString start;

    @Path("stop")
    @Comment("config.messages.stop.comment")
    @DefaultValue("config.messages.stop.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString stop;

    @Path("join")
    @Comment("config.messages.join.comment")
    @DefaultValue("config.messages.join.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString join;

    @Path("leave")
    @Comment("config.messages.leave.comment")
    @DefaultValue("config.messages.leave.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString leave;

    @Path("death")
    @Comment("config.messages.death.comment")
    @DefaultValue("config.messages.death.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString death;

    @Path("advancement")
    @Comment("config.messages.advancement.comment")
    @DefaultValue("config.messages.advancement.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString advancement;
}
