package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.mc2discord.core.config.converters.RandomString;
import fr.denisd3d.mc2discord.core.config.converters.RandomStringConverter;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;

public class AccountMessages {
    @Path("link_get_code")
    @Comment("config.account.messages.link_get_code.comment")
    @DefaultValue("config.account.messages.link_get_code.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString link_get_code;

    @Path("link_successful")
    @Comment("config.account.messages.link_successful.comment")
    @DefaultValue("config.account.messages.link_successful.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString link_successful;

    @Path("link_invalid_code")
    @Comment("config.account.messages.link_invalid_code.comment")
    @DefaultValue("config.account.messages.link_invalid_code.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString link_invalid_code;

    @Path("link_error_already")
    @Comment("config.account.messages.link_error_already.comment")
    @DefaultValue("config.account.messages.link_error_already.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString link_error_already;

    @Path("unlink_successful")
    @Comment("config.account.messages.unlink_successful.comment")
    @DefaultValue("config.account.messages.unlink_successful.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString unlink_successful;

    @Path("unlink_error")
    @Comment("config.account.messages.unlink_error.comment")
    @DefaultValue("config.account.messages.unlink_error.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString unlink_error;

    @Path("missing_roles")
    @Comment("config.account.messages.missing_roles.comment")
    @DefaultValue("config.account.messages.missing_roles.value")
    @Conversion(RandomStringConverter.class)
    @PreserveNotNull
    public RandomString missing_roles;

    @Path("comment")
    public String comment;
}
