package ml.denisd3d.mc2discord.forge.storage;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.management.UserList;
import net.minecraft.server.management.UserListEntry;

import javax.annotation.Nonnull;
import java.io.File;

public class HiddenPlayerList extends UserList<GameProfile, HiddenPlayerEntry> {

    public HiddenPlayerList(File saveFile) {
        super(saveFile);
    }

    @Nonnull
    protected UserListEntry<GameProfile> createEntry(@Nonnull JsonObject entryData) {
        return new HiddenPlayerEntry(entryData);
    }

    public boolean isHidden(GameProfile profile) {
        return this.contains(profile);
    }

    @Override
    @Nonnull
    public String[] getUserList() {
        String[] hiddenPlayers = new String[this.getEntries().size()];

        int i = 0;
        for (UserListEntry<GameProfile> userListEntry : this.getEntries()) {
            if (userListEntry.getUser() != null)
                hiddenPlayers[i++] = userListEntry.getUser().getName();
        }

        return hiddenPlayers;
    }

    @Override
    @Nonnull
    protected String getKeyForUser(GameProfile obj) {
        return obj.getId().toString();
    }
}
