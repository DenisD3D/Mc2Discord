package fr.denisd3d.mc2discord.core.storage;

import com.google.gson.JsonObject;

import java.util.UUID;

public abstract class UserStorageEntry {
    private final UUID playerUuid;
    public UserStorageEntry(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public UUID getPlayerUuid() {
        return this.playerUuid;
    }

    protected abstract void serialize(JsonObject pData);
}
