package fr.denisd3d.mc2discord.core.entities;

import com.vdurmont.emoji.EmojiParser;

public record MessageEntity(String message) implements Entity {
    public MessageEntity {
        message = EmojiParser.parseToAliases(message);
    }

    @Override
    public Object get(String variable) {
        return "message".equals(variable) ? message : null;
    }
}
