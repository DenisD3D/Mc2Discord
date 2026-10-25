package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import com.electronwill.nightconfig.toml.TomlFormat;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.config.converters.TemplateConverter;
import fr.denisd3d.mc2discord.core.entities.Template;

public class Style {
    @Path("bot_name")
    @Comment("config.style.bot_name.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template bot_name = Template.parse("");

    @Path("bot_avatar")
    @Comment("config.style.bot_avatar.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template bot_avatar = Template.parse("");

    @Path("webhook_display_name")
    @Comment("config.style.webhook_display_name.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template webhook_display_name = Template.parse("${player_display_name}");

    @Path("webhook_avatar_api")
    @Comment("config.style.webhook_avatar_api.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template webhook_avatar_api = Template.parse("https://mc-heads.net/head/${player_uuid}/right");

    @Path("minecraft_chat_format")
    @Comment("config.style.minecraft_chat_format.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template minecraft_chat_format = Template.parse("<Discord - ${member_display_name}> ${reply}${message} ${attachments}");

    @Path("reply_format")
    @Comment("config.style.reply_format.comment")
    @DefaultValue("config.style.reply_format.value")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template reply_format;

    @Path("discord_chat_format")
    @Comment("config.style.discord_chat_format.comment")
    @Conversion(TemplateConverter.class)
    @PreserveNotNull
    public Template discord_chat_format = Template.parse("**${player_display_name}**: ${message}");

    @Path("EmbedColors")
    @Comment("config.style.embed_colors.comment")
    @PreserveNotNull
    public Config embed_colors = TomlFormat.newConfig();

    @Path("embed_show_bot_avatar")
    @Comment("config.style.embed_show_bot_avatar.comment")
    @PreserveNotNull
    public boolean embed_show_bot_avatar = false;

    @Path("Presence")
    @Comment("config.style.presence.comment")
    @PreserveNotNull
    public Presence presence = new Presence();

    @Path("comment")
    public String comment;
}
