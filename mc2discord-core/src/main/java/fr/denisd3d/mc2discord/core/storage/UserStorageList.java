package fr.denisd3d.mc2discord.core.storage;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.*;
import fr.denisd3d.mc2discord.core.Mc2Discord;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public abstract class UserStorageList<T extends UserStorageEntry> {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final File file;
    private final Map<UUID, T> map = Maps.newHashMap();

    public UserStorageList(File file) {
        this.file = file;
    }

    /**
     * Adds an entry to the list
     */
    public void add(T entry) {
        this.map.put(entry.getPlayerUuid(), entry);

        try {
            this.save();
        } catch (IOException ioexception) {
            Mc2Discord.LOGGER.warn("Could not save the list after adding a user.", ioexception);
        }

    }

    @Nullable
    public T get(UUID playerUuid) {
        return this.map.get(playerUuid);
    }

    public void remove(UUID playerUuid) {
        this.map.remove(playerUuid);

        try {
            this.save();
        } catch (IOException ioexception) {
            Mc2Discord.LOGGER.warn("Could not save the list after removing a user.", ioexception);
        }

    }

    public UUID[] getPlayerList() {
        return this.map.keySet().toArray(new UUID[0]);
    }

    public boolean isEmpty() {
        return this.map.size() < 1;
    }

    public boolean contains(UUID playerUuid) {
        return this.map.containsKey(playerUuid);
    }

    protected abstract T createEntry(JsonObject pEntryData);

    public Collection<T> getEntries() {
        return this.map.values();
    }

    public void save() throws IOException {
        JsonArray jsonarray = new JsonArray();
        this.map.values().stream().map(t -> {
            JsonObject jsonobject = new JsonObject();
            t.serialize(jsonobject);
            return jsonobject;
        }).forEach(jsonarray::add);

        try (BufferedWriter bufferedwriter = Files.newWriter(this.file, StandardCharsets.UTF_8)) {
            GSON.toJson(jsonarray, bufferedwriter);
        }

    }

    public void load() throws IOException {
        if (this.file.exists()) {
            try (BufferedReader bufferedreader = Files.newReader(this.file, StandardCharsets.UTF_8)) {
                JsonArray jsonarray = GSON.fromJson(bufferedreader, JsonArray.class);
                this.map.clear();

                for (JsonElement jsonelement : jsonarray) {
                    JsonObject jsonobject = convertToJsonObject(jsonelement);
                    T userStorageEntry = this.createEntry(jsonobject);
                    this.map.put(userStorageEntry.getPlayerUuid(), userStorageEntry);
                }
            }

        }
    }

    private static JsonObject convertToJsonObject(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            return jsonElement.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected entry to be a JsonObject");
        }
    }
}
