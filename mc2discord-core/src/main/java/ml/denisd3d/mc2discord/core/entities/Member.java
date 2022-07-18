package ml.denisd3d.mc2discord.core.entities;

import java.util.HashMap;

public class Member extends Entity {
    public final String name;
    public final String discriminator;
    public final String nickname;
    public final String avatarUrl;
    public final String top_role_color;

    public final HashMap<String, String> replacements = new HashMap<>();

    public Member(String name, String discriminator, String nickname, String avatarUrl, int top_role_color) {
        this.name = name;
        this.discriminator = discriminator;
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
        StringBuilder hex = new StringBuilder(Integer.toHexString(top_role_color & 0xffffff));
        while (hex.length() < 6) {
            hex.insert(0, "0");
        }
        this.top_role_color = "#" + hex;
    }

    @Override
    public String replace(String content) {
        replacements.put("name", this.name);
        replacements.put("discriminator", this.discriminator);
        replacements.put("tag", this.name + "#" + this.discriminator);
        replacements.put("nickname", this.nickname);
        replacements.put("avatar_url", this.avatarUrl);
        replacements.put("top_role_color", this.top_role_color);

        content = content.replace("${color_start_top_role}", "${color_start_" + this.top_role_color + "}");
        return this.replace(content, "member", this.replacements);
    }
}
