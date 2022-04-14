package ml.denisd3d.mc2discord.core.config;


import com.electronwill.nightconfig.core.conversion.Path;
import com.electronwill.nightconfig.core.conversion.PreserveNotNull;
import ml.denisd3d.config4j.Comment;
import ml.denisd3d.config4j.Config4J;
import ml.denisd3d.config4j.OnlyIf;
import ml.denisd3d.mc2discord.core.config.account.Account;
import ml.denisd3d.mc2discord.core.config.core.*;

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
    @PreserveNotNull
    public Commands commands = new Commands();

    @Path("Status")
    @PreserveNotNull
    public Status status = new Status();

    @Path("Features")
    @PreserveNotNull
    public Features features = new Features();

    @Path("Account")
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

        if (this.commands.rules.isEmpty()) {
            this.commands.rules.add(new Commands.CommandRule("help"));
        }

        if (this.status.statusChannels.channels.isEmpty()) {
            this.status.statusChannels.channels.add(new Status.StatusChannel());
        }

        for (Channels.Channel channel : this.channels.channels) {
            this.channels.channels_map.put(channel.channel_id, channel);
        }

        for (Commands.CommandRule commandRule : this.commands.rules) {
            this.commands.rules_map.put(commandRule.id, commandRule);
        }

        if (this.account.policies.isEmpty()) {
            this.account.policies.add(new Account.AccountPolicy());
        }
    }
}


