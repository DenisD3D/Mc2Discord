package ml.denisd3d.minecraft2discord.core.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;
import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class M2DConfig {

    @Path("Misc.logs_format")
    @PreserveNotNull
    public static String logs_format = "[${log_time!HH:mm:ss}] [${log_thread_name}/${log_level}] [${log_logger_name}]: ${log_message}";
    //End General
    //General
    @Path("General.token")
    @Comment(" Token for the bot. This is a secret string that can be generated on discord website. More info here : https://github.com/DenisD3D/Minecraft2Discord/wiki/Discord-token")
    @PreserveNotNull
    public String token = "";
    //Channels
    @Path("Channels.Channel")
    @Comment(" Channels configuration. You may duplicate this block. Each one correspond to one channel\n" +
            " id is the discord id of the channel. See : https://github.com/DenisD3D/Minecraft2Discord/wiki/Discord-ids\n" +
            " subscriptions is an array of what type of message you want in this channel. Currently supported are : \n" +
            "   - chat (player chat message in both direction)\n" +
            "   - info (join, leave, start, stop, death, advancement)\n" +
            "   - command (listen to command, see Commands section)\n" +
            "   - log (all the log of the server)\n" +
            " use_webhook define whether or not to send the message with a custom head for the player")
    @PreserveNotNull
    public List<Channel> channels = new ArrayList<>();
    public transient HashMap<Long, Channel> channels_map = new HashMap<>();
    //End Channels

    //Messages
    @Path("Messages.start")
    @Comment(" Global variables only")
    @PreserveNotNull
    public String start_message = "The server has started";

    @Path("Messages.stop")
    @Comment(" Global variables only")
    @PreserveNotNull
    public String stop_message = "The server has stopped";

    @Path("Messages.join")
    @Comment(" Global variables and Player variables")
    @PreserveNotNull
    public String join_message = "${player_display_name} joined the game";

    @Path("Messages.leave")
    @Comment(" Global variables and Player variables")
    @PreserveNotNull
    public String leave_message = "${player_display_name} left the game";

    @Path("Messages.death")
    @Comment(" Global variables, Player variables and Death variables")
    @PreserveNotNull
    public String death_message = "${death_message}";

    @Path("Messages.advancement")
    @Comment(" Global variables, Player variables and Advancement variables")
    @PreserveNotNull
    public String advancement_message = "${player_display_name} has made the advancement ${advancement_title}. ${advancement_description}.";
    //End Messages

    //Commands
    @Path("Commands.prefix")
    @Comment(" Prefix used before each command. Minecraft default one is '/' (eg : /help)")
    @PreserveNotNull
    public String command_prefix = "/";

    @Path("Commands.use_codeblocks")
    @Comment(" If true, text returned by commands will be in a Discord code block")
    @PreserveNotNull
    public boolean use_codeblocks = true;

    @Path("Commands.error")
    @Comment(" Response when the user isn't allowed to use the command")
    @PreserveNotNull
    public String command_error_message = "Sorry, your not allowed to use that command!";

    @Path("Commands.Command")
    @Comment(" Commands permissions configuration. You may duplicate the block. Each block correspond to one rule\n" +
            " id is an user id or a role id. The rule (current block) will apply for this user or users with this role\n" +
            " commands is a list of the commands that are allowed in addition of the permission level\n" +
            " permission_level allow all the commands with this permission or under level. -1 mean only the commands in the list commands above and 0 mean all non op commands\n" +
            " see https://minecraft.gamepedia.com/Server.properties#op-permission-level")
    @PreserveNotNull
    public List<CommandRule> command_rules = new ArrayList<>();
    public transient HashMap<Long, CommandRule> command_rules_map = new HashMap<>();
    //End Commands

    //Status
    @Path("Status.Presence.message")
    @Comment(" Message to display under the bot in the member list")
    @PreserveNotNull
    public String presence_message = "${player_count} / ${max_player}";

    @Path("Status.Presence.update")
    @Comment(" Update frequency of the presence message (in seconds). A too low number might result in limitation from Discord")
    @PreserveNotNull
    public long presence_update = 60L;

    @Path("Status.Channels.Channel")
    @Comment(" Status channels configuration. You may duplicate the block. Each block correspond to one channel\n" +
            " id is the discord id of the channel. See : https://github.com/DenisD3D/Minecraft2Discord/wiki/Discord-ids\n" +
            " update_period is the update frequency of the channel (in seconds). A too low number might result in limitation form Discord (max 2 update per 10 minutes as of writing)\n" +
            " name_message is the message to set as a name of the channel. You can use Global variables. If the channel is a text channel, whitespace will be replaced by '-'\n" +
            " topic_message is the message to set as the description of the channel. You can use Global variables")
    @PreserveNotNull
    public List<StatusChannel> status_channels = new ArrayList<>();
    //End Status

    //Misc
    @Path("Misc.bot_name")
    @Comment(" Override the bot name in webhook mode. You may use global variable here")
    @PreserveNotNull
    public String bot_name = "";

    @Path("Misc.bot_avatar")
    @Comment(" Override the bot avatar in webhook mode. Must be a valid an url. You may use global variable here")
    @PreserveNotNull
    public String bot_avatar = "";

    @Path("Misc.avatar_api")
    @Comment(" Url to fetch player avatar from. Must be a valid url and not empty. You may use Global variables and Player variables here")
    @PreserveNotNull
    public String avatar_api = "https://mc-heads.net/head/${player_uuid}/right";

    @Path("Misc.relay_bot_messages")
    @Comment(" Define if other bots messages must be sent in the minecraft chat")
    @PreserveNotNull
    public boolean relay_bot_messages = false;

    @Path("Misc.minecraft_chat_format")
    @Comment(" Format for the messages sent in the minecraft chat. You may use Global variables, Member variables and Message variable here")
    @PreserveNotNull
    public String minecraft_chat_format = "<Discord - ${member_name}> ${message}";

    @Path("Misc.discord_chat_format")
    @Comment(" Format for chat messages sent in discord when webhook mode is turned to false. You may use Global variables, Player variables and Message variable here")
    @PreserveNotNull
    public String discord_chat_format = "**${player_display_name}**: ${message}";

    @Path("Misc.discord_text")
    @Comment(" Message for the /discord command")
    @PreserveNotNull
    public String discord_text = "Invitation link not set";

    @Path("Misc.discord_link")
    @Comment(" Link for the /discord command")
    @PreserveNotNull
    public String discord_link = "https://discord.gg/";
    //End Misc

    public static M2DConfig load(File file) {
        Config.setInsertionOrderPreserved(true);

        ObjectConverter converter = new ObjectConverter();
        CommentedFileConfig config = CommentedFileConfig.of(file);
        config.load();

        M2DConfig m2dConfig = converter.toObject(config, M2DConfig::new);

        config.clear();
        if (m2dConfig.channels.isEmpty()) {
            m2dConfig.channels.add(new Channel());
        }

        for (Channel channel : m2dConfig.channels) {
            if (channel.subscriptions.isEmpty()) {
                channel.subscriptions.add("info");
                channel.subscriptions.add("chat");
                channel.subscriptions.add("command");
            }

            m2dConfig.channels_map.put(channel.channel_id, channel);
        }

        if (m2dConfig.command_rules.isEmpty()) {
            m2dConfig.command_rules.add(new CommandRule());
        }

        for (CommandRule entry : m2dConfig.command_rules) {
            if (entry.commands.isEmpty()) {
                entry.commands.add("help");
            }

            m2dConfig.command_rules_map.put(entry.id, entry);
        }

        if (m2dConfig.status_channels.isEmpty()) {
            m2dConfig.status_channels.add(new StatusChannel());
        }

        converter.toConfig(m2dConfig, config);

        config.setComment("General", " Minecraft2Discord configuration\n" +
                "  - Curseforge : https://www.curseforge.com/minecraft/mc-mods/minecraft2discord\n" +
                "  - Modrinth : https://modrinth.com/mod/minecraft2discord\n" +
                "  - Discord : https://discord.gg/rzzd76c\n" +
                "  - Github : https://github.com/DenisD3D/Minecraft2Discord\n" +
                "  - Wiki : https://github.com/DenisD3D/Minecraft2Discord/wiki\n" +
                "\n" +
                " Read the wiki for a quick start guide or for more advanced customization\n" +
                " You can also join the discord to get some help\n" +
                "\n" +
                " You can use discord markdown\n" +
                " Many variables are available to customize the behavior. Check them on the wiki https://github.com/DenisD3D/Minecraft2Discord/wiki/Variables");

        config.setComment("Messages", "Customize here the message that are sent on discord. To disable one, set an empty value (\"\")\n" +
                "For the list of available variables see : https://github.com/DenisD3D/Minecraft2Discord/wiki/Variables");

        for (Field field : m2dConfig.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Comment.class) && field.isAnnotationPresent(Path.class)) {
                config.setComment(field.getAnnotation(Path.class).value(), field.getAnnotation(Comment.class).value());
            }
        }

        config.save();

        return m2dConfig;
    }

    public static class Channel {
        @Path("id")
        @PreserveNotNull
        public long channel_id = 0L;

        @Path("subscriptions")
        @PreserveNotNull
        public List<String> subscriptions = new ArrayList<>();

        @Path("use_webhook")
        @PreserveNotNull
        public boolean use_webhook = true;
    }

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
    }

    public static class StatusChannel {
        @Path("id")
        @PreserveNotNull
        public long channel_id = 0L;

        @Path("update_period")
        @PreserveNotNull
        public long update_period = 610;

        @Path("name_message")
        @PreserveNotNull
        public String name_message = "${online_players} / ${max_players}";

        @Path("topic_message")
        @PreserveNotNull
        public String topic_message = "${online_players} / ${max_players}";
    }
}
