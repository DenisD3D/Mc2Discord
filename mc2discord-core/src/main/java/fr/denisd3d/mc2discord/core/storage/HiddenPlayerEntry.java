package fr.denisd3d.mc2discord.core.storage;

import com.google.gson.JsonObject;

import java.util.UUID;

public class HiddenPlayerEntry extends UserStorageEntry {
    public HiddenPlayerEntry(UUID playerUuid) {
        super(playerUuid);
    }

    @Override
    protected void serialize(JsonObject data) {
        data.addProperty("uuid", this.getPlayerUuid().toString());
    }
}
