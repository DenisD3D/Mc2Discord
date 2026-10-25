package fr.denisd3d.mc2discord.core.entities;

import discord4j.core.object.entity.Member;

public record MemberEntity(String globalName, String username, String nickname, String avatarUrl, int topRoleColor) implements Entity {
    /**
     * @param topRoleColor See {@link fr.denisd3d.mc2discord.core.M2DUtils#getMemberColor(Member)}
     */
    public static MemberEntity of(Member member, int topRoleColor) {
        return new MemberEntity(member.getGlobalName().orElse(member.getUsername()), member.getUsername(), member.getNickname().orElse(""),
                member.getAvatarUrl(), topRoleColor);
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
