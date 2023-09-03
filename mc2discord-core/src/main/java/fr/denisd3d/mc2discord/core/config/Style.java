package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;

public class Style {
    @Path("bot_name")
    @Comment("config.style.bot_name.comment")
    @PreserveNotNull
    public String bot_name = "";

    @Path("bot_avatar")
    @Comment("config.style.bot_avatar.comment")
    @PreserveNotNull
    public String bot_avatar = "";

    @Path("webhook_display_name")
    @Comment("config.style.webhook_display_name.comment")
    @PreserveNotNull
    public String webhook_display_name = "${player_display_name}";

    @Path("webhook_avatar_api")
    @Comment("config.style.webhook_avatar_api.comment")
    @PreserveNotNull
    public String webhook_avatar_api = "https://mc-heads.net/head/${player_uuid}/right";

    @Path("minecraft_chat_format")
    @Comment("config.style.minecraft_chat_format.comment")
    @PreserveNotNull
    public String minecraft_chat_format = "<Discord - ${member_display_name}> ${reply}${message}";

    @Path("reply_format")
    @Comment("config.style.reply_format.comment")
    @DefaultValue("config.style.reply_format.value")
    @PreserveNotNull
    public String reply_format;

    @Path("discord_chat_format")
    @Comment("config.style.discord_chat_format.comment")
    @PreserveNotNull
    public String discord_chat_format = "**${player_display_name}**: ${message}";

    @Path("EmbedColors")
    @Comment("config.style.embed_colors.comment")
    @PreserveNotNull
    public EmbedColors embed_colors = new EmbedColors();

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

    public static class EmbedColors {
        @Path("info")
        @PreserveNotNull
        public String info = "SUMMER_SKY";

        @Path("chat")
        @PreserveNotNull
        public String chat = "MEDIUM_SEA_GREEN";

        @Path("command")
        @PreserveNotNull
        public String command = "MEDIUM_SEA_GREEN";

        @Path("log")
        @PreserveNotNull
        public String log = "SUMMER_SKY";

        @Path("comment")
        public String comment;
    }
}