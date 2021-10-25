package ml.denisd3d.mc2discord.core.config.account;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Converter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import discord4j.common.util.Snowflake;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

import java.util.ArrayList;
import java.util.List;

public class Account {
    @Path("guild_id")
    @Comment("config.account.guild_id.comment")
    @PreserveNotNull
    public long guild_id = 0L;

    @Path("rename_discord_member")
    @Comment("config.account.rename_discord_member.comment")
    @PreserveNotNull
    public boolean rename_discord_member;

    @Path("discord_pseudo_format")
    @Comment("config.account.discord_pseudo_format.comment")
    @DefaultValue("config.account.discord_pseudo_format.value")
    @PreserveNotNull
    public String discord_pseudo_format;

    @Path("link_command")
    @Comment("config.account.link_command.comment")
    @PreserveNotNull
    public String link_command = "discord link";

    @Path("unlink_command")
    @Comment("config.account.unlink_command.comment")
    @PreserveNotNull
    public String unlink_command = "discord unlink";

    @Path("Messages")
    @Comment("config.account.messages.comment")
    @PreserveNotNull
    public AccountMessages messages = new AccountMessages();

    @Path("Policy")
    @Comment("config.account.policies.comment")
    @PreserveNotNull
    public List<AccountPolicy> policies = new ArrayList<>();

    @Path("force_link")
    @Comment("config.account.force_link.comment")
    @PreserveNotNull
    public boolean force_link = false;

    public static class AccountPolicy {
        @Path("required_roles_id")
        @Conversion(SnowflakeArrayConverter.class)
        @PreserveNotNull
        public List<Snowflake> required_roles_id = new ArrayList<>();

        @Path("roles_id_to_give")
        @Conversion(SnowflakeArrayConverter.class)
        @PreserveNotNull
        public List<Snowflake> roles_id_to_give = new ArrayList<>();

        public AccountPolicy() {
        }
    }
}

