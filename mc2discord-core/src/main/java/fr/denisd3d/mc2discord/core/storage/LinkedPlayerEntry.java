package fr.denisd3d.mc2discord.core.storage;

import com.google.gson.JsonObject;
import discord4j.common.util.Snowflake;

import java.util.UUID;

public class LinkedPlayerEntry extends UserStorageEntry {
    private final Snowflake discordId;

    public LinkedPlayerEntry(UUID playerUuid, Snowflake discordId) {
        super(playerUuid);
        this.discordId = discordId;
    }

    public Snowflake getDiscordId() {
        return discordId;
    }

    @Override
    protected void serialize(JsonObject data) {
        data.addProperty("uuid", this.getPlayerUuid().toString());
        data.addProperty("discord_id", this.discordId.asLong());
    }
}
