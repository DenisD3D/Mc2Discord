package ml.denisd3d.mc2discord.core.config.account;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

public class AccountMessages {
    @Path("link_get_code")
    @Comment("config.account.messages.link_get_code.comment")
    @DefaultValue("config.account.messages.link_get_code.value")
    @PreserveNotNull
    public String link_get_code;

    @Path("link_successful")
    @Comment("config.account.messages.link_successful.comment")
    @DefaultValue("config.account.messages.link_successful.value")
    @PreserveNotNull
    public String link_successful;

    @Path("link_invalid_code")
    @Comment("config.account.messages.link_invalid_code.comment")
    @DefaultValue("config.account.messages.link_invalid_code.value")
    @PreserveNotNull
    public String link_invalid_code;

    @Path("link_error_already")
    @Comment("config.account.messages.link_error_already.comment")
    @DefaultValue("config.account.messages.link_error_already.value")
    @PreserveNotNull
    public String link_error_already;

    @Path("unlink_successful")
    @Comment("config.account.messages.unlink_successful.comment")
    @DefaultValue("config.account.messages.unlink_successful.value")
    @PreserveNotNull
    public String unlink_successful;

    @Path("unlink_error")
    @Comment("config.account.messages.unlink_error.comment")
    @DefaultValue("config.account.messages.unlink_error.value")
    @PreserveNotNull
    public String unlink_error;

    @Path("missing_roles")
    @Comment("config.account.messages.missing_roles.comment")
    @DefaultValue("config.account.messages.missing_roles.value")
    @PreserveNotNull
    public String missing_roles;
}
