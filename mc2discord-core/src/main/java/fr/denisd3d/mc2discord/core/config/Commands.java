package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
    @SerdeKey("prefix")
    @SerdeComment("config.commands.prefix.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String prefix = "!";

    @SerdeKey("use_codeblocks")
    @SerdeComment("config.commands.use_codeblocks.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean use_codeblocks = true;

    @SerdeKey("slash_command")
    @SerdeComment("config.commands.slash_command.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean slash_command = true;

    @SerdeKey("Permission")
    @SerdeComment("config.commands.permission.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public List<CommandPermission> permissions = new ArrayList<>();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;

    public static class CommandPermission {
        @SerdeKey("id")
        @SerdeComment("config.commands.permission.id.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public Snowflake id = M2DUtils.NIL_SNOWFLAKE;

        @SerdeKey("permission_level")
        @SerdeComment("config.commands.permission.permission_level.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public Integer permission_level = -1;

        @SerdeKey("commands")
        @SerdeComment("config.commands.permission.commands.comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public List<String> commands = new ArrayList<>();

        @SerdeKey("comment")
        @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
        public String comment;

        public CommandPermission(String... commands) {
            this.commands.addAll(Arrays.asList(commands));
        }

        @SuppressWarnings("unused")
        public CommandPermission() { // Required for night-config to create instance
        }
    }
}
