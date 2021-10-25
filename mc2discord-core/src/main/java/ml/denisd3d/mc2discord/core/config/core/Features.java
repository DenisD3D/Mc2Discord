package ml.denisd3d.mc2discord.core.config.core;

import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;

public class Features {
    @Path("account_linking")
    @Comment("config.features.account_linking.comment")
    @PreserveNotNull
    public boolean account_linking = false;
}
