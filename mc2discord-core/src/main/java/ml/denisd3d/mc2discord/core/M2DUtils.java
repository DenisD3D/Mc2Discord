package ml.denisd3d.mc2discord.core;

import discord4j.common.util.TokenUtil;
import discord4j.core.object.entity.PartialMember;
import discord4j.core.object.entity.Role;
import discord4j.core.util.OrderUtil;
import discord4j.gateway.GatewayObserver;
import discord4j.rest.util.Color;
import org.apache.commons.lang3.math.NumberUtils;
import reactor.core.publisher.Mono;
import reactor.math.MathFlux;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class M2DUtils {
    public static final List<String> available_lang = Arrays.asList("en_us", "fr_fr", "ru_ru", "ko_kr", "zh_cn");
    public static final List<String> lang_contributors = Arrays.asList("Morty#0273 (ru_ru)", "PixelVoxel#4327 (ko_kr)", "thearchy.helios (zh_cn)");

    // Inspired from methods by Tomer Godinger.
    private static final int NOT_FOUND = -1;
    private static final String CODEBLOCKS_TOKEN = "```";

    public static boolean isTokenValid(String token) {
        try {
            TokenUtil.getSelfId(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean canHandleEvent() {
        return Mc2Discord.INSTANCE != null && Mc2Discord.INSTANCE.client != null && Mc2Discord.INSTANCE.getState() == GatewayObserver.CONNECTED && !Mc2Discord.INSTANCE.is_stopping;
    }

    private static int lastIndexOfRegex(String str, String toFind) {
        Pattern pattern = Pattern.compile(toFind);
        Matcher matcher = pattern.matcher(str);

        // Default to the NOT_FOUND constant
        int lastIndex = NOT_FOUND;

        // Search for the given pattern
        while (matcher.find()) {
            lastIndex = matcher.start();
        }

        return lastIndex;
    }

    public static int lastIndexOfRegex(String str, String toFind, int fromIndex) {
        // Limit the search by searching on a suitable substring
        return lastIndexOfRegex(str.substring(0, fromIndex), toFind);
    }

    public static List<String> breakStringToLines(String str, int maxLength, boolean withCodeBlocks) {
        List<String> result = new ArrayList<>();
        if (withCodeBlocks) maxLength -= 6;
        while (str.length() > maxLength) {
            int breakingIndex = getBreakingIndex(str, maxLength);

            // Append each prepared line to the builder
            result.add(withCodeBlocks ? CODEBLOCKS_TOKEN + str.substring(0, breakingIndex + 1) + CODEBLOCKS_TOKEN : str.substring(0, breakingIndex + 1));

            // And start the next line
            str = str.substring(breakingIndex + 1);
        }

        // Check if there are any residual characters left
        if (str.length() > 0) {
            result.add(withCodeBlocks ? CODEBLOCKS_TOKEN + str + CODEBLOCKS_TOKEN : str);
        }

        if (result.isEmpty()) {
            result.add("");
        }

        // Return the resulting string
        return result;
    }

    public static int getBreakingIndex(String str, int maxLength) {
        // Attempt to break on new line first,
        int breakingIndex = lastIndexOfRegex(str, "\n", maxLength);

        // Then on words,
        if (breakingIndex == NOT_FOUND) breakingIndex = lastIndexOfRegex(str, "\\s", maxLength);

        // Then on other non-alphanumeric characters,
        if (breakingIndex == NOT_FOUND) breakingIndex = lastIndexOfRegex(str, "[^a-zA-Z0-9]", maxLength);

        // And if all else fails, break in the middle of the word
        if (breakingIndex == NOT_FOUND) breakingIndex = maxLength;

        return breakingIndex;
    }

    public static int getSafeBreakingIndex(String str, int maxLength) {
        if (str.length() > maxLength) {
            return getBreakingIndex(str, maxLength);
        } else {
            return str.length();
        }
    }

    public static Color getColorFromString(String color) {
        String s = color.toLowerCase();
        if ("white".equals(s)) {
            return Color.DISCORD_WHITE;
        } else if ("light_gray".equals(s)) {
            return Color.LIGHT_GRAY;
        } else if ("gray".equals(s)) {
            return Color.GRAY;
        } else if ("dark_gray".equals(s)) {
            return Color.DARK_GRAY;
        } else if ("black".equals(s)) {
            return Color.DISCORD_BLACK;
        } else if ("red".equals(s)) {
            return Color.RED;
        } else if ("pink".equals(s)) {
            return Color.PINK;
        } else if ("orange".equals(s)) {
            return Color.ORANGE;
        } else if ("yellow".equals(s)) {
            return Color.YELLOW;
        } else if ("green".equals(s)) {
            return Color.GREEN;
        } else if ("magenta".equals(s)) {
            return Color.MAGENTA;
        } else if ("cyan".equals(s)) {
            return Color.CYAN;
        } else if ("blue".equals(s)) {
            return Color.BLUE;
        } else if ("light_sea_green".equals(s)) {
            return Color.LIGHT_SEA_GREEN;
        } else if ("medium_sea_green".equals(s)) {
            return Color.MEDIUM_SEA_GREEN;
        } else if ("summer_sky".equals(s)) {
            return Color.SUMMER_SKY;
        } else if ("deep_lilac".equals(s)) {
            return Color.DEEP_LILAC;
        } else if ("ruby".equals(s)) {
            return Color.RUBY;
        } else if ("moon_yellow".equals(s)) {
            return Color.MOON_YELLOW;
        } else if ("tahiti_gold".equals(s)) {
            return Color.TAHITI_GOLD;
        } else if ("cinnabar".equals(s)) {
            return Color.CINNABAR;
        } else if ("submarine".equals(s)) {
            return Color.SUBMARINE;
        } else if ("hoki".equals(s)) {
            return Color.HOKI;
        } else if ("deep_sea".equals(s)) {
            return Color.DEEP_SEA;
        } else if ("sea_green".equals(s)) {
            return Color.SEA_GREEN;
        } else if ("endeavour".equals(s)) {
            return Color.ENDEAVOUR;
        } else if ("vivid_violet".equals(s)) {
            return Color.VIVID_VIOLET;
        } else if ("jazzberry_jam".equals(s)) {
            return Color.JAZZBERRY_JAM;
        } else if ("dark_goldenrod".equals(s)) {
            return Color.DARK_GOLDENROD;
        } else if ("rust".equals(s)) {
            return Color.RUST;
        } else if ("brown".equals(s)) {
            return Color.BROWN;
        } else if ("gray_chateau".equals(s)) {
            return Color.GRAY_CHATEAU;
        } else if ("bismark".equals(s)) {
            return Color.BISMARK;
        }
        return NumberUtils.isParsable(color) ? Color.of(Integer.parseInt(color)) : Color.WHITE;
    }


    private static final Pattern any_pattern = Pattern.compile("<(?::\\w+:|@&*|#)\\d+>");

    public static String replaceAllMentions(String str, List<PartialMember> partialMembers) {
        Matcher matcher = any_pattern.matcher(str);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group(0);
            if (replacement.matches("<(:\\w+:)\\d+>")) {
                matcher.appendReplacement(buffer, "");
                buffer.append(":").append(replacement.split(":")[1]).append(":");
            } else if (replacement.matches("<@(\\d+)>")) {
                partialMembers.stream()
                        .filter(partialMember -> partialMember.getId().asString().equals(replacement.substring(2, replacement.length() - 1)))
                        .findFirst()
                        .ifPresent(partialMember -> {
                            matcher.appendReplacement(buffer, "");
                            buffer.append("@").append(partialMember.getDisplayName());
                        });
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    public static Mono<Integer> getMemberColor(PartialMember member) {
        return MathFlux.max(member.getRoles().filter(role -> role.getColor().getRGB() != 0), OrderUtil.ROLE_ORDER).map(Role::getColor).map(Color::getRGB).defaultIfEmpty(16777215);
    }
}
