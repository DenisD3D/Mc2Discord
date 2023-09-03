package fr.denisd3d.mc2discord.core.config;


import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import fr.denisd3d.config4j.Comment;
import fr.denisd3d.config4j.Config4J;
import fr.denisd3d.config4j.OnlyIf;

import java.io.File;
import java.util.function.Function;

public class M2DConfig extends Config4J {

    @Path("lang")
    @Comment("config.lang.comment")
    @PreserveNotNull
    @SuppressWarnings("unused")
    public String lang = "en_us";

    @Path("General")
    @Comment("config.general.comment")
    @PreserveNotNull
    public General general = new General();

    @Path("Channels")
    @Comment("config.channels.comment")
    @PreserveNotNull
    public Channels channels = new Channels();

    @Path("Messages")
    @Comment("config.messages.comment")
    @PreserveNotNull
    public Messages messages = new Messages();

    @Path("Commands")
    @Comment("config.commands.comment")
    @PreserveNotNull
    public Commands commands = new Commands();

    @Path("Features")
    @Comment("config.features.comment")
    @PreserveNotNull
    public Features features = new Features();

    @Path("StatusChannels")
    @Comment("config.status_channels.comment")
    @PreserveNotNull
    @OnlyIf("Features.status_channels")
    public StatusChannels statusChannels = new StatusChannels();

    @Path("Account")
    @Comment("config.account.comment")
    @PreserveNotNull
    @OnlyIf("Features.account_linking")
    public Account account = new Account();

    @Path("Style")
    @Comment("config.style.comment")
    @PreserveNotNull()
    public Style style = new Style();

    @Path("Misc")
    @Comment("config.misc.comment")
    @PreserveNotNull()
    public Misc misc = new Misc();

    public M2DConfig(File file, Function<String, String> translator) {
        super(file, translator);
    }

    @Override
    public void betweenLoadAndSave() {
        if (this.channels.channels.isEmpty()) {
            this.channels.channels.add(new Channels.Channel("info", "chat", "command"));
        }

        if (this.commands.permissions.isEmpty()) {
            this.commands.permissions.add(new Commands.CommandPermission(""));
        }

        if (this.statusChannels.channels.isEmpty()) {
            this.statusChannels.channels.add(new StatusChannels.StatusChannel());
        }

        if (this.misc.other_mods_messages.isEmpty()) {
            this.misc.other_mods_messages.add(new Misc.OtherModMessage());
        }

        if (this.account.policies.isEmpty()) {
            this.account.policies.add(new Account.AccountPolicy());
        }
    }
}


