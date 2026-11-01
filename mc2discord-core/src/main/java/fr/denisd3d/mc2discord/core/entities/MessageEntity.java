package fr.denisd3d.mc2discord.core.entities;

import net.fellbaum.jemoji.EmojiManager;

public record MessageEntity(String message) implements Entity {
    public MessageEntity {
        message = EmojiManager.replaceAllEmojis(message, emoji -> emoji.getDiscordAliases().stream().findFirst().orElse(emoji.getEmoji()));
    }

    @Override
    public Object get(String variable) {
        return "message".equals(variable) ? message : null;
    }
}
