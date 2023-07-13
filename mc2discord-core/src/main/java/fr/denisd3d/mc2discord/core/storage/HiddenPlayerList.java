package fr.denisd3d.mc2discord.core.storage;

import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class HiddenPlayerList extends UserStorageList<HiddenPlayerEntry> {
    private static final File HIDDEN_PLAYERS_FILE = new File("m2d-hidden-players.json");

    public HiddenPlayerList() {
        super(HIDDEN_PLAYERS_FILE);

        try {
            this.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected HiddenPlayerEntry createEntry(JsonObject entryData) {
        return new HiddenPlayerEntry(UUID.fromString(entryData.get("uuid").getAsString()));
    }
}
