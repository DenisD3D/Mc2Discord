package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;

public class AccountMessages {
    @SerdeKey("link_get_code")
    @SerdeComment("config.account.messages.link_get_code.comment")
    @DefaultValue("config.account.messages.link_get_code.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate link_get_code;

    @SerdeKey("link_successful")
    @SerdeComment("config.account.messages.link_successful.comment")
    @DefaultValue("config.account.messages.link_successful.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate link_successful;

    @SerdeKey("link_invalid_code")
    @SerdeComment("config.account.messages.link_invalid_code.comment")
    @DefaultValue("config.account.messages.link_invalid_code.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate link_invalid_code;

    @SerdeKey("link_error_already")
    @SerdeComment("config.account.messages.link_error_already.comment")
    @DefaultValue("config.account.messages.link_error_already.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate link_error_already;

    @SerdeKey("unlink_successful")
    @SerdeComment("config.account.messages.unlink_successful.comment")
    @DefaultValue("config.account.messages.unlink_successful.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate unlink_successful;

    @SerdeKey("unlink_error")
    @SerdeComment("config.account.messages.unlink_error.comment")
    @DefaultValue("config.account.messages.unlink_error.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate unlink_error;

    @SerdeKey("missing_roles")
    @SerdeComment("config.account.messages.missing_roles.comment")
    @DefaultValue("config.account.messages.missing_roles.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public RandomTemplate missing_roles;

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;
}
