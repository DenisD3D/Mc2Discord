package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import discord4j.common.util.Snowflake;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.util.ArrayList;
import java.util.List;

public class Account {
    @SerdeKey("guild_id")
    @SerdeComment("config.account.guild_id.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Snowflake guild_id = M2DUtils.NIL_SNOWFLAKE;

    @SerdeKey("rename_discord_member")
    @SerdeComment("config.account.rename_discord_member.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean rename_discord_member = true;

    @SerdeKey("discord_pseudo_format")
    @SerdeComment("config.account.discord_pseudo_format.comment")
    @DefaultValue("config.account.discord_pseudo_format.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template discord_pseudo_format;

    @SerdeKey("Messages")
    @SerdeComment("config.account.messages.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public AccountMessages messages = new AccountMessages();

    @SerdeKey("force_link")
    @SerdeComment("config.account.force_link.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean force_link = false;

    @SerdeKey("Policy")
    @SerdeComment("config.account.policies.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<AccountPolicy> policies = new ArrayList<>();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;

    public static class AccountPolicy {
        @SerdeKey("required_roles_id")
        @SerdeComment("config.account.policies.required_roles_id.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public List<Snowflake> required_roles_id = new ArrayList<>();

        @SerdeKey("roles_id_to_give")
        @SerdeComment("config.account.policies.roles_id_to_give.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public List<Snowflake> roles_id_to_give = new ArrayList<>();

        @SerdeKey("comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String comment;

        public AccountPolicy() {
        }
    }
}
