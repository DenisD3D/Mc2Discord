package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.DefaultValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Commands {
    @Path("prefix")
    @Comment("config.commands.prefix.comment")
    @PreserveNotNull
    public String prefix = "/";

    @Path("use_codeblocks")
    @Comment("config.commands.use_codeblocks.comment")
    @PreserveNotNull
    public boolean use_codeblocks = true;

    @Path("error")
    @Comment("config.commands.error.comment")
    @DefaultValue("config.commands.error.value")
    @PreserveNotNull
    public String error_message;

    @Path("Command")
    @Comment("config.commands.command.comment")
    @PreserveNotNull
    public List<CommandRule> rules = new ArrayList<>();
    public transient HashMap<Long, CommandRule> rules_map = new HashMap<>();

    public static class CommandRule {
        @Path("id")
        @PreserveNotNull
        public long id = 0L;

        @Path("commands")
        @PreserveNotNull
        public List<String> commands = new ArrayList<>();

        @Path("permission_level")
        @PreserveNotNull
        public Integer permission_level = -1;

        @Path("comment")
        @PreserveNotNull
        public String comment = "";

        public CommandRule(String... commands) {
            this.commands.addAll(Arrays.asList(commands));
        }

        @SuppressWarnings("unused")
        public CommandRule() { // Required for nightconfig to create instance
        }
    }
}
