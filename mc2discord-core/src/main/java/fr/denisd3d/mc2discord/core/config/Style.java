package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import com.electronwill.nightconfig.toml.TomlFormat;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.entities.Template;

public class Style {
    @SerdeKey("bot_name")
    @SerdeComment("config.style.bot_name.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template bot_name = Template.parse("");

    @SerdeKey("bot_avatar")
    @SerdeComment("config.style.bot_avatar.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template bot_avatar = Template.parse("");

    @SerdeKey("webhook_display_name")
    @SerdeComment("config.style.webhook_display_name.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template webhook_display_name = Template.parse("${player_display_name}");

    @SerdeKey("webhook_avatar_api")
    @SerdeComment("config.style.webhook_avatar_api.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template webhook_avatar_api = Template.parse("https://mc-heads.net/head/${player_uuid}/right");

    @SerdeKey("minecraft_chat_format")
    @SerdeComment("config.style.minecraft_chat_format.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template minecraft_chat_format = Template.parse("<Discord - ${member_display_name}> ${reply}${message} ${attachments}");

    @SerdeKey("reply_format")
    @SerdeComment("config.style.reply_format.comment")
    @DefaultValue("config.style.reply_format.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template reply_format;

    @SerdeKey("discord_chat_format")
    @SerdeComment("config.style.discord_chat_format.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template discord_chat_format = Template.parse("**${player_display_name}**: ${message}");

    @SerdeKey("EmbedColors")
    @SerdeComment("config.style.embed_colors.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Config embed_colors = TomlFormat.newConfig();

    @SerdeKey("embed_show_bot_avatar")
    @SerdeComment("config.style.embed_show_bot_avatar.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean embed_show_bot_avatar = false;

    @SerdeKey("Presence")
    @SerdeComment("config.style.presence.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Presence presence = new Presence();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;
}
