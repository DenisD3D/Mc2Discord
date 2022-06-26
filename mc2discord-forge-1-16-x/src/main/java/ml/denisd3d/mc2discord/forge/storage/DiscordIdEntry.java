package ml.denisd3d.mc2discord.forge.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.UserListEntry;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nonnull;
import java.util.Optional;
import java.util.UUID;

public class DiscordIdEntry extends UserListEntry<GameProfile> {
    private final long discord_id;

    public DiscordIdEntry(GameProfile player, long discord_id) {
        super(player);
        this.discord_id = discord_id;
    }

    public DiscordIdEntry(JsonObject jsonObject) {
        super(constructProfile(jsonObject));
        this.discord_id = jsonObject.has("discord_id") ? jsonObject.get("discord_id").getAsLong() : 0;
    }

    private static GameProfile constructProfile(JsonObject jsonObject) {
        if (jsonObject.has("uuid") || jsonObject.has("name")) {
            String s = jsonObject.get("uuid").getAsString();

            UUID uuid;
            try {
                uuid = UUID.fromString(s);
            } catch (Throwable throwable) {
                return null;
            }

            JsonElement nameJsonElement = jsonObject.get("name");
            String name = nameJsonElement != null ? nameJsonElement.getAsString() : Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()
                    .getProfileCache()
                    .get(uuid)).map(GameProfile::getName).orElse(null);

            return new GameProfile(uuid, name);
        } else {
            return null;
        }
    }

    public long getDiscordId() {
        return discord_id;
    }

    @Override
    protected void serialize(@Nonnull JsonObject data) {
        if (this.getUser() != null) {
            data.addProperty("uuid", this.getUser().getId() == null ? "" : this.getUser().getId().toString());
            data.addProperty("name", this.getUser().getName());
            data.addProperty("discord_id", this.discord_id);
        }
    }
}
