package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.common.util.TokenUtil;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.User;
import discord4j.core.util.OrderUtil;
import discord4j.rest.util.Color;
import org.apache.commons.lang3.math.NumberUtils;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M2DUtils {
    public static final File CONFIG_FILE = new File("config", "mc2discord.toml");
    public static final Snowflake NIL_SNOWFLAKE = Snowflake.of(0);
    private static final Pattern ANY_DISCORD_MENTION_PATTERN = Pattern.compile("<(?:a?:\\w+:|@&*|#)\\d+>");
    private static final Pattern ANY_DISCORD_NAMED_PATTERN = Pattern.compile("@\\w+|#\\w+|:\\w+:");


    /**
     * Check if the mod is configured and discord bot is ready to use
     * @return Whether the discord bot is ready or not
     */
    public static boolean isNotConfigured() {
        return Mc2Discord.INSTANCE == null || !Mc2Discord.INSTANCE.vars.isStarted;
    }

    /**
     * Checks if a discord bot token is valid
     *
     * @param token The token to check
     * @return Whether the token is valid or not
     */
    public static boolean isTokenValid(String token) {
        try {
            TokenUtil.getSelfId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Breaks a string into multiple messages
     *
     * @param input                 The string to break
     * @param maxLineLength         The maximum length of a section
     * @param surroundWithCodeBlock Whether to surround the string with a code block ``` or not
     * @return A list of strings that are no longer than maxLineLength
     */
    public static List<String> breakStringInMessages(String input, int maxLineLength, boolean surroundWithCodeBlock) {
        if (surroundWithCodeBlock) {
            maxLineLength -= 6;
        }

        StringTokenizer tok = new StringTokenizer(input, "\n");
        StringBuilder output = new StringBuilder(input.length());
        List<String> lines = new ArrayList<>();

        int lineLen = 0;
        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();

            if (lineLen + word.length() >= maxLineLength) {
                if (surroundWithCodeBlock) {
                    output.insert(0, "```");
                    output.append("```");
                }
                lines.add(output.toString());
                output.setLength(0);
                lineLen = 0;
            } else {
                output.append("\n");
                lineLen++;
            }
            output.append(word);
            lineLen += word.length();
        }
        if (!output.isEmpty()) {
            if (surroundWithCodeBlock) {
                output.insert(0, "```");
                output.append("```");
            }
            lines.add(output.toString());
        }
        return lines;
    }

    /**
     * Replaces all mentions in a discord message with their display name
     *
     * @param str     The string to replace mentions in
     * @param users   The list of users to replace mentions with
     * @param roles   The list of roles to replace mentions with
     * @param guildId The guild id to replace mentions with
     * @return The string with all mentions replaced
     */
    public static String replaceAllMentions(String str, List<User> users, List<Role> roles, Snowflake guildId) {
        Matcher matcher = ANY_DISCORD_MENTION_PATTERN.matcher(str);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String replacement = matcher.group(0);
            if (replacement.matches("<a?(:\\w+:)\\d+>")) { // Emojis
                matcher.appendReplacement(builder, Matcher.quoteReplacement(":" + replacement.split(":")[1].toLowerCase() + ":"));
            } else if (replacement.matches("<@(\\d+)>")) { // Users
                users.stream().filter(partialMember -> partialMember.getId().asString().equals(replacement.substring(2, replacement.length() - 1))).findFirst().ifPresent(user -> matcher.appendReplacement(builder, Matcher.quoteReplacement("@" + user.getUsername())));
            } else if (replacement.matches("<@&(\\d+)>")) { // Roles
                roles.stream().filter(role -> role.getId().asString().equals(replacement.substring(3, replacement.length() - 1))).findFirst().ifPresent(role -> matcher.appendReplacement(builder, Matcher.quoteReplacement("@" + role.getName())));
            } else if (replacement.matches("<#(\\d+)>")) { // Channels
                matcher.appendReplacement(builder, Matcher.quoteReplacement("#" + Mc2Discord.INSTANCE.vars.channelCache.rowMap().get(guildId).entrySet().stream().filter(entry -> entry.getValue().equals(Snowflake.of(replacement.substring(2, replacement.length() - 1)))).findFirst().map(Map.Entry::getKey).orElse("unknown")));
            }
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    /**
     * Transforms a message with @username to a message with <@mention>
     * @param message The message to transform
     * @param channelId The channel id to transform the message for
     * @return The transformed message
     */
    public static String transformToMention(String message, Snowflake channelId) {
        Snowflake guildId = Mc2Discord.INSTANCE.vars.channelCacheReverse.get(channelId);

        Matcher matcher = ANY_DISCORD_NAMED_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();

        while (matcher.find()) {
            String replacement = matcher.group(0);

            if (replacement.startsWith("@")) {
                String username = replacement.substring(1);
                Snowflake memberId = Mc2Discord.INSTANCE.vars.memberCache.get(guildId, username);
                if (memberId != null) {
                    matcher.appendReplacement(builder, Matcher.quoteReplacement("<@" + memberId.asString() + ">"));
                }
            } else if (replacement.startsWith(":")) {
                String emojiName = replacement.substring(1, replacement.length() - 1);
                Snowflake emojiId = Mc2Discord.INSTANCE.vars.emojiCache.get(guildId, emojiName);
                if (emojiId != null) {
                    matcher.appendReplacement(builder, Matcher.quoteReplacement("<:" + emojiName + ":" + emojiId.asString() + ">"));
                }
            } else if (replacement.startsWith("#")) {
                String channelName = replacement.substring(1);
                Snowflake mentionChannelId = Mc2Discord.INSTANCE.vars.channelCache.get(guildId, channelName);
                if (mentionChannelId != null) {
                    matcher.appendReplacement(builder, Matcher.quoteReplacement("<#" + mentionChannelId.asString() + ">"));
                }
            }
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    /**
     * Gets the color of a member
     *
     * @param member The member to get the color of
     * @return The color of the member
     */
    public static Mono<Integer> getMemberColor(Member member) {
        return MathFlux.max(member.getRoles().filter(role -> role.getColor().getRGB() != 0), OrderUtil.ROLE_ORDER).map(Role::getColor).map(Color::getRGB).defaultIfEmpty(16777215);
    }

    /**
     * Adds a member to the cache if it has a migrated account only
     * @param member The member to cache
     */
    public static void cacheMember(Member member) {
        if (!Objects.equals(member.getDiscriminator(), "0")) // If user doesn't have migrated account ignore it
            return;

        Mc2Discord.INSTANCE.vars.memberCache.put(member.getGuildId(), member.getUsername(), member.getId());
    }

    /**
     * Transform a discord color name to its integer value
     * @param color The color to transform, may be a name or an int value
     * @return The color as an object
     */
    public static Color getColorFromString(String color) {
        String s = color.toLowerCase();
        return switch (s) {
            case "white" -> Color.DISCORD_WHITE;
            case "light_gray" -> Color.LIGHT_GRAY;
            case "gray" -> Color.GRAY;
            case "dark_gray" -> Color.DARK_GRAY;
            case "black" -> Color.DISCORD_BLACK;
            case "red" -> Color.RED;
            case "pink" -> Color.PINK;
            case "orange" -> Color.ORANGE;
            case "yellow" -> Color.YELLOW;
            case "green" -> Color.GREEN;
            case "magenta" -> Color.MAGENTA;
            case "cyan" -> Color.CYAN;
            case "blue" -> Color.BLUE;
            case "light_sea_green" -> Color.LIGHT_SEA_GREEN;
            case "medium_sea_green" -> Color.MEDIUM_SEA_GREEN;
            case "summer_sky" -> Color.SUMMER_SKY;
            case "deep_lilac" -> Color.DEEP_LILAC;
            case "ruby" -> Color.RUBY;
            case "moon_yellow" -> Color.MOON_YELLOW;
            case "tahiti_gold" -> Color.TAHITI_GOLD;
            case "cinnabar" -> Color.CINNABAR;
            case "submarine" -> Color.SUBMARINE;
            case "hoki" -> Color.HOKI;
            case "deep_sea" -> Color.DEEP_SEA;
            case "sea_green" -> Color.SEA_GREEN;
            case "endeavour" -> Color.ENDEAVOUR;
            case "vivid_violet" -> Color.VIVID_VIOLET;
            case "jazzberry_jam" -> Color.JAZZBERRY_JAM;
            case "dark_goldenrod" -> Color.DARK_GOLDENROD;
            case "rust" -> Color.RUST;
            case "brown" -> Color.BROWN;
            case "gray_chateau" -> Color.GRAY_CHATEAU;
            case "bismark" -> Color.BISMARK;
            default -> NumberUtils.isParsable(color) ? Color.of(Integer.parseInt(color)) : Color.WHITE;
        };
    }
}
