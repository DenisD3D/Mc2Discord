package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

import java.util.ArrayList;
import java.util.List;

public class Misc {
    @Path("bot_name")
    @Comment("config.misc.bot_name.comment")
    @PreserveNotNull
    public String bot_name = "";

    @Path("bot_avatar")
    @Comment("config.misc.bot_avatar.comment")
    @PreserveNotNull
    public String bot_avatar = "";

    @Path("avatar_api")
    @Comment("config.misc.avatar_api.comment")
    @PreserveNotNull
    public String avatar_api = "https://mc-heads.net/head/${player_uuid}/right";

    @Path("relay_bot_messages")
    @Comment("config.misc.relay_bot_messages.comment")
    @PreserveNotNull
    public boolean relay_bot_messages = false;

    @Path("relay_say_me_command")
    @Comment("config.misc.relay_say_me_command.comment")
    @PreserveNotNull
    public boolean relay_say_me_command = true;

    @Path("allowed_mention")
    @Comment("config.misc.allowed_mention.comment")
    @PreserveNotNull
    public List<String> allowed_mention = new ArrayList<>();

    @Path("minecraft_chat_format")
    @Comment("config.misc.minecraft_chat_format.comment")
    @PreserveNotNull
    public String minecraft_chat_format = "<Discord - ${member_nickname}> ${message}";

    @Path("discord_chat_format")
    @Comment("config.misc.discord_chat_format.comment")
    @PreserveNotNull
    public String discord_chat_format = "**${player_display_name}**: ${message}";

    @Path("discord_text")
    @Comment("config.misc.discord_text.comment")
    @DefaultValue("config.misc.discord_text.value")
    @PreserveNotNull
    public String discord_text;

    @Path("discord_link")
    @Comment("config.misc.discord_link.comment")
    @PreserveNotNull
    public String discord_link = "https://discord.gg/";

    @Path("logs_format")
    @Comment("config.misc.logs_format.comment")
    @PreserveNotNull
    public String logs_format = "[${log_time!HH:mm:ss}] [${log_thread_name}/${log_level}] [${log_logger_name}]: ${log_message}";

    @Path("logs_level")
    @Comment("config.misc.logs_level.comment")
    @PreserveNotNull
    public String logs_level = "INFO";
}
