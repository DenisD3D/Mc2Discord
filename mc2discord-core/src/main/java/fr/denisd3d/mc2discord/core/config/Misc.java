package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.DefaultValue;

import java.util.ArrayList;
import java.util.List;

public class Misc {
    @Path("relay_bot_messages")
    @Comment("config.misc.relay_bot_messages.comment")
    @PreserveNotNull
    public boolean relay_bot_messages = false;

    @Path("allowed_mention")
    @Comment("config.misc.allowed_mention.comment")
    @PreserveNotNull
    public List<String> allowed_mention = null;

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
    public String logs_format = "[${log_time|HH:mm:ss}] [${log_thread_name}/${log_level}] [${log_logger_name}]: ${log_message}";

    @Path("logs_level")
    @Comment("config.misc.logs_level.comment")
    @PreserveNotNull
    public String logs_level = "INFO";

    @Path("broadcast_commands")
    @Comment("config.misc.broadcast_commands.comment")
    @PreserveNotNull
    public List<String> broadcast_commands;

    @Path("verbose_other_mods_messages")
    @Comment("config.misc.verbose_other_mods_messages.comment")
    @PreserveNotNull
    public boolean verbose_other_mods_messages = false;

    @Path("other_mods_messages")
    @Comment("config.misc.other_mods_messages.comment")
    @PreserveNotNull
    public List<OtherModMessage> other_mods_messages = new ArrayList<>();

    @Path("comment")
    public String comment;


    public static class OtherModMessage {
        @Path("class_name")
        @Comment("config.misc.other_mods_messages.class_name.comment")
        @PreserveNotNull
        public String class_name = "";

        @Path("class_index")
        @Comment("config.misc.other_mods_messages.class_index.comment")
        @PreserveNotNull
        public int class_index = 0;

        @Path("type")
        @Comment("config.misc.other_mods_messages.type.comment")
        @PreserveNotNull
        public String type = "info";

        @Path("comment")
        public String comment;
    }
}
