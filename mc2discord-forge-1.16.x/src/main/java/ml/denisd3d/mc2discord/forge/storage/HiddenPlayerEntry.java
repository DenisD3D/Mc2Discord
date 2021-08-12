package ml.denisd3d.mc2discord.forge.storage;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.UserListEntry;

import javax.annotation.Nonnull;
import java.util.UUID;

public class HiddenPlayerEntry extends UserListEntry<GameProfile> {
    public HiddenPlayerEntry(GameProfile profile) {
        super(profile);
    }

    public HiddenPlayerEntry(JsonObject json) {
        super(gameProfileFromJsonObject(json));
    }

    private static GameProfile gameProfileFromJsonObject(JsonObject json) {
        if (json.has("uuid") && json.has("name")) {
            String s = json.get("uuid").getAsString();

            UUID uuid;
            try {
                uuid = UUID.fromString(s);
            } catch (Throwable throwable) {
                return null;
            }

            return new GameProfile(uuid, json.get("name").getAsString());
        } else {
            return null;
        }
    }

    @Override
    protected void serialize(@Nonnull JsonObject data) {
        if (this.getUser() != null) {
            data.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
            data.addProperty("name", this.getUser().getName());
        }
    }
}
