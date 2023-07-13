package fr.denisd3d.mc2discord.core.storage;

import com.google.gson.JsonObject;
import discord4j.common.util.Snowflake;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LinkedPlayerList extends UserStorageList<LinkedPlayerEntry> {
    private static final File LINKED_PLAYERS_FILE = new File("m2d-linked-players.json");

    public LinkedPlayerList() {
        super(LINKED_PLAYERS_FILE);

        try {
            this.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected LinkedPlayerEntry createEntry(JsonObject entryData) {
        return new LinkedPlayerEntry(UUID.fromString(entryData.get("uuid").getAsString()), Snowflake.of(entryData.get("discord_id").getAsLong()));
    }
}
