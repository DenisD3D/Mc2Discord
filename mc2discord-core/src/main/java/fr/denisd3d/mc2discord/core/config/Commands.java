package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Conversion;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;
import fr.denisd3d.mc2discord.core.config.converters.SnowflakeConverter;
import ml.denisd3d.config4j.Comment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
    @Path("prefix")
    @Comment("config.commands.prefix.comment")
    @PreserveNotNull
    public String prefix = "!";

    @Path("use_codeblocks")
    @Comment("config.commands.use_codeblocks.comment")
    @PreserveNotNull
    public boolean use_codeblocks = true;

    @Path("Permission")
    @Comment("config.commands.permission.comment")
    @PreserveNotNull
    public List<CommandPermission> permissions = new ArrayList<>();

    public static class CommandPermission {
        @Path("id")
        @Comment("config.commands.permission.id.comment")
        @Conversion(SnowflakeConverter.class)
        @PreserveNotNull
        public Snowflake id = M2DUtils.NIL_SNOWFLAKE;

        @Path("permission_level")
        @Comment("config.commands.permission.permission_level.comment")
        @PreserveNotNull
        public Integer permission_level = -1;

        @Path("commands")
        @Comment("config.commands.permission.commands.comment")
        @PreserveNotNull
        public List<String> commands = new ArrayList<>();


        public CommandPermission(String... commands) {
            this.commands.addAll(Arrays.asList(commands));
        }

        @SuppressWarnings("unused")
        public CommandPermission() { // Required for night-config to create instance
        }
    }
}