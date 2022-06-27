package ml.denisd3d.mc2discord.core.entities;

import com.vdurmont.emoji.EmojiParser;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message extends Entity {
    public final HashMap<String, String> replacements = new HashMap<>();
    public String message;

    public Message(String message) {
        this.message = EmojiParser.parseToAliases(message);
    }

    @Override
    public String replace(String content) {
        replacements.put("message", this.message);
        return replace(content, null, replacements);
    }
}
