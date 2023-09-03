package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;

public class Features {
    @Path("status_channels")
    @Comment("config.features.status_channels.comment")
    @PreserveNotNull
    public boolean status_channels = false;

    @Path("account_linking")
    @Comment("config.features.account_linking.comment")
    @PreserveNotNull
    public boolean account_linking = false;
}