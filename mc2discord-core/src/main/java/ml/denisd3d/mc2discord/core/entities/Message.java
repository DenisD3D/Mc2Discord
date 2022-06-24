package ml.denisd3d.mc2discord.core.entities;

import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message extends Entity {
    private static final Pattern emote_pattern = Pattern.compile("<:(.+?):\\d+>");
    public final HashMap<String, String> replacements = new HashMap<>();
    public String message;

    public Message(String message) {
        this.message = message;
    }

    @Override
    public String replace(String content) {
        Matcher matcher = emote_pattern.matcher(message);
        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String replacement = matcher.group(1);
            if (replacement != null) {
                matcher.appendReplacement(buffer, "");
                buffer.append(":").append(replacement).append(":");
            }
        }
        matcher.appendTail(buffer);

        replacements.put("message", this.message = EmojiParser.parseToAliases(buffer.toString()));
        return replace(content, null, replacements);
    }
}
