package fr.denisd3d.mc2discord.core.entities;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;

public record MemberEntity(String globalName, String username, String nickname, String avatarUrl, int topRoleColor) implements Entity {
    /**
     * @param topRoleColor See {@link fr.denisd3d.mc2discord.core.M2DUtils#getMemberColor(Member)}
     */
    public static MemberEntity of(Member member, int topRoleColor) {
        return new MemberEntity(member.getGlobalName().orElse(member.getUsername()), member.getUsername(), member.getNickname().orElse(""),
                member.getAvatarUrl(), topRoleColor);
    }

    /**
     * For authors that aren't guild members, like webhooks: no nickname and the default color
     */
    public static MemberEntity of(User user) {
        return new MemberEntity(user.getGlobalName().orElse(user.getUsername()), user.getUsername(), "", user.getAvatarUrl(), 16777215);
    }

    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "member_display_name" -> nickname.isEmpty() ? globalName : nickname;
            case "member_global_name" -> globalName;
            case "member_username" -> username;
            case "member_nickname" -> nickname;
            case "member_avatar_url" -> avatarUrl;
            case "member_top_role_color" -> String.format("#%06x", topRoleColor & 0xffffff);
            default -> null;
        };
    }
}
