package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Channels {
    @Path("Channel")
    @PreserveNotNull
    public List<Channel> channels = new ArrayList<>();
    public transient HashMap<Long, Channel> channels_map = new HashMap<>();

    public static class Channel {
        @Path("id")
        @PreserveNotNull
        public long channel_id = 0L;

        @Path("subscriptions")
        @PreserveNotNull
        public List<String> subscriptions = new ArrayList<>();

        @Path("use_webhook")
        @PreserveNotNull
        public boolean use_webhook = true;

        public Channel(String... subscriptions) {
            this.subscriptions.addAll(Arrays.asList(subscriptions));
        }

        public Channel() { // Required for nightconfig to create instance
        }
    }
}
