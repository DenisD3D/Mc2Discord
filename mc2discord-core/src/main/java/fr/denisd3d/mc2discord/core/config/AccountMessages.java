package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplateConverter;

public class AccountMessages {
    @Path("link_get_code")
    @Comment("config.account.messages.link_get_code.comment")
    @DefaultValue("config.account.messages.link_get_code.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate link_get_code;

    @Path("link_successful")
    @Comment("config.account.messages.link_successful.comment")
    @DefaultValue("config.account.messages.link_successful.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate link_successful;

    @Path("link_invalid_code")
    @Comment("config.account.messages.link_invalid_code.comment")
    @DefaultValue("config.account.messages.link_invalid_code.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate link_invalid_code;

    @Path("link_error_already")
    @Comment("config.account.messages.link_error_already.comment")
    @DefaultValue("config.account.messages.link_error_already.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate link_error_already;

    @Path("unlink_successful")
    @Comment("config.account.messages.unlink_successful.comment")
    @DefaultValue("config.account.messages.unlink_successful.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate unlink_successful;

    @Path("unlink_error")
    @Comment("config.account.messages.unlink_error.comment")
    @DefaultValue("config.account.messages.unlink_error.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate unlink_error;

    @Path("missing_roles")
    @Comment("config.account.messages.missing_roles.comment")
    @DefaultValue("config.account.messages.missing_roles.value")
    @Conversion(RandomTemplateConverter.class)
    @PreserveNotNull
    public RandomTemplate missing_roles;

    @Path("comment")
    public String comment;
}
