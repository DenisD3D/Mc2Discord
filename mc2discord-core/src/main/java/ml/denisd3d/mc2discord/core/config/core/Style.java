package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

public class Style {
    @Path("bot_name")
    @Comment("config.style.bot_name.comment")
    @PreserveNotNull
    public String bot_name = "";

    @Path("bot_avatar")
    @Comment("config.style.bot_avatar.comment")
    @PreserveNotNull
    public String bot_avatar = "";

    @Path("avatar_api")
    @Comment("config.style.avatar_api.comment")
    @PreserveNotNull
    public String avatar_api = "https://mc-heads.net/head/${player_uuid}/right";

    @Path("minecraft_chat_format")
    @Comment("config.style.minecraft_chat_format.comment")
    @PreserveNotNull
    public String minecraft_chat_format = "<Discord - ${member_nickname}> ${reply}${message}";

    @Path("discord_chat_format")
    @Comment("config.style.discord_chat_format.comment")
    @PreserveNotNull
    public String discord_chat_format = "**${player_display_name}**: ${message}";

    @Path("reply_format")
    @Comment("config.style.reply_format.comment")
    @DefaultValue("config.style.reply_format.value")
    @PreserveNotNull
    public String reply_format;

    @Path("embed_color.info")
    @Comment("config.style.embed_color.comment")
    @PreserveNotNull
    public String embed_color_info = "SUMMER_SKY";

    @Path("embed_color.chat")
    @PreserveNotNull
    public String embed_color_chat = "MEDIUM_SEA_GREEN";

    @Path("embed_color.command")
    @PreserveNotNull
    public String embed_color_command = "MEDIUM_SEA_GREEN";

    @Path("embed_color.log")
    @PreserveNotNull
    public String embed_color_log = "SUMMER_SKY";

}
