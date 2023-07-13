package fr.denisd3d.mc2discord.core.entities;

import com.vdurmont.emoji.EmojiParser;

import java.util.Map;
import java.util.function.BiFunction;

public class MessageEntity extends Entity {
    public String message;

    public MessageEntity(String message) {
        this.message = EmojiParser.parseToAliases(message);
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put("message", this.message);
    }
}
