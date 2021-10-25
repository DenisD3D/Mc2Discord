package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;

import java.util.ArrayList;
import java.util.List;

public class StatusChannels {
    @Path("Channel")
    @PreserveNotNull
    public List<Status.StatusChannel> channels = new ArrayList<>();
}
