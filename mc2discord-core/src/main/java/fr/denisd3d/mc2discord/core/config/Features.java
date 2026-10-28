package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;

public class Features {
    @SerdeKey("status_channels")
    @SerdeComment("config.features.status_channels.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean status_channels = false;

    @SerdeKey("account_linking")
    @SerdeComment("config.features.account_linking.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public boolean account_linking = false;

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;
}
