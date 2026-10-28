package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import fr.denisd3d.config4j.DefaultValue;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.util.ArrayList;
import java.util.List;

public class Misc {
    @SerdeKey("relay_bot_messages")
    @SerdeComment("config.misc.relay_bot_messages.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean relay_bot_messages = false;

    @SerdeKey("allowed_mention")
    @SerdeComment("config.misc.allowed_mention.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<String> allowed_mention = null;

    @SerdeKey("discord_text")
    @SerdeComment("config.misc.discord_text.comment")
    @DefaultValue("config.misc.discord_text.value")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String discord_text;

    @SerdeKey("discord_link")
    @SerdeComment("config.misc.discord_link.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String discord_link = "https://discord.gg/";

    @SerdeKey("logs_format")
    @SerdeComment("config.misc.logs_format.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Template logs_format = Template.parse("[${log_time|HH:mm:ss}] [${log_thread_name}/${log_level}] [${log_logger_name}]: ${log_message}");

    @SerdeKey("logs_level")
    @SerdeComment("config.misc.logs_level.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String logs_level = "INFO";

    @SerdeKey("broadcast_commands")
    @SerdeComment("config.misc.broadcast_commands.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<String> broadcast_commands;

    @SerdeKey("verbose_other_mods_messages")
    @SerdeComment("config.misc.verbose_other_mods_messages.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean verbose_other_mods_messages = false;

    @SerdeKey("other_mods_messages")
    @SerdeComment("config.misc.other_mods_messages.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<OtherModMessage> other_mods_messages = new ArrayList<>();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;

    public static class OtherModMessage {
        @SerdeKey("class_name")
        @SerdeComment("config.misc.other_mods_messages.class_name.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String class_name = "";

        @SerdeKey("class_index")
        @SerdeComment("config.misc.other_mods_messages.class_index.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public int class_index = 0;

        @SerdeKey("type")
        @SerdeComment("config.misc.other_mods_messages.type.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String type = "info";

        @SerdeKey("comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String comment;
    }
}
