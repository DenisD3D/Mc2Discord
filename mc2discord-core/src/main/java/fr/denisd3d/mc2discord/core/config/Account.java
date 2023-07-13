package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.config.converters.SnowflakeArrayConverter;
import fr.denisd3d.mc2discord.core.config.converters.SnowflakeConverter;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

import java.util.ArrayList;
import java.util.List;

public class Account {
    @Path("guild_id")
    @Comment("config.account.guild_id.comment")
    @Conversion(SnowflakeConverter.class)
    @PreserveNotNull
    public Snowflake guild_id = M2DUtils.NIL_SNOWFLAKE;

    @Path("rename_discord_member")
    @Comment("config.account.rename_discord_member.comment")
    @PreserveNotNull
    public boolean rename_discord_member = true;

    @Path("discord_pseudo_format")
    @Comment("config.account.discord_pseudo_format.comment")
    @DefaultValue("config.account.discord_pseudo_format.value")
    @PreserveNotNull
    public String discord_pseudo_format;

    @Path("Messages")
    @Comment("config.account.messages.comment")
    @PreserveNotNull
    public AccountMessages messages = new AccountMessages();

    @Path("force_link")
    @Comment("config.account.force_link.comment")
    @PreserveNotNull
    public boolean force_link = false;

    @Path("Policy")
    @Comment("config.account.policies.comment")
    @PreserveNotNull
    public List<AccountPolicy> policies = new ArrayList<>();


    public static class AccountPolicy {
        @Path("required_roles_id")
        @Comment("config.account.policies.required_roles_id.comment")
        @Conversion(SnowflakeArrayConverter.class)
        @PreserveNotNull
        public List<Snowflake> required_roles_id = new ArrayList<>();

        @Path("roles_id_to_give")
        @Comment("config.account.policies.roles_id_to_give.comment")
        @Conversion(SnowflakeArrayConverter.class)
        @PreserveNotNull
        public List<Snowflake> roles_id_to_give = new ArrayList<>();

        public AccountPolicy() {
        }
    }
}

