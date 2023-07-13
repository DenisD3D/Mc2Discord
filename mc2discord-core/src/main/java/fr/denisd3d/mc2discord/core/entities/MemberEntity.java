package fr.denisd3d.mc2discord.core.entities;

import java.util.Map;
import java.util.function.BiFunction;

public class MemberEntity extends Entity {
    private static final String prefix = "member_";
    public final String global_name;
    public final String username;
    public final String nickname;
    public final String avatarUrl;
    public final String top_role_color;

    public MemberEntity(String global_name, String username, String nickname, String avatarUrl, int top_role_color) {
        this.global_name = global_name;
        this.username = username;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        StringBuilder hex = new StringBuilder(Integer.toHexString(top_role_color & 0xffffff));
        while (hex.length() < 6) {
            hex.insert(0, "0");
        }
        this.top_role_color = "#" + hex;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put(prefix + "display_name", !this.nickname.isEmpty() ? this.nickname : this.global_name);
        replacements.put(prefix + "global_name", this.global_name);
        replacements.put(prefix + "username", this.username);
        replacements.put(prefix + "nickname", this.nickname);
        replacements.put(prefix + "avatar_url", this.avatarUrl);
        replacements.put(prefix + "top_role_color", this.top_role_color);
        replacements.put("color_start|top_role", "${color_start|" + this.top_role_color + "}");
    }
}
