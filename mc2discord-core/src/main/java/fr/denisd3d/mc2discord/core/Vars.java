package fr.denisd3d.mc2discord.core;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.AllowedMentions;

import java.util.HashMap;
import java.util.Map;

public class Vars {
    // Bot info
    public String bot_name = "";
    public String bot_discriminator = "";
    public Snowflake bot_id = M2DUtils.NIL_SNOWFLAKE;
    public String mc2discord_display_name = "";
    public String mc2discord_avatar = "";
    public String mc2discord_webhook_name = "";

    public Table<Snowflake, String, Snowflake> emojiCache = HashBasedTable.create(); // Store emojis to replace in messages :emoji: to <:name:id> for each guild (<GuildId, EmojiName, EmojiId>)
    public Table<Snowflake, String, Snowflake> channelCache = HashBasedTable.create(); // Store channels to replace in messages to #name for each guild (<GuildId, ChannelName, ChannelId>)
    public Table<Snowflake, String, Snowflake> memberCache = HashBasedTable.create(); // Store channels to replace in messages to #name for each guild (<GuildId, MemberUsername, MemberId>)
    public Map<Snowflake, Snowflake> channelCacheReverse = new HashMap<>(); // Store channels to get guild ids (<ChannelId, GuildId>)

    /**
     * The time when the server started, used to calculate the uptime
     */
    public static Long startTime = null;

    public Possible<AllowedMentions> allowedMentions = Possible.absent();
    public boolean missingMessageContentIntent = false;
    public boolean isStarted = false;
}
