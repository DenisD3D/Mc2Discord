package fr.denisd3d.mc2discord.core.config;

import com.electronwill.nightconfig.core.serde.ObjectDeserializerBuilder;
import com.electronwill.nightconfig.core.serde.ObjectSerializerBuilder;
import com.electronwill.nightconfig.core.serde.ValueDeserializer;
import com.electronwill.nightconfig.core.serde.ValueDeserializerProvider;
import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;
import discord4j.common.util.Snowflake;
import fr.denisd3d.config4j.Config4J;
import fr.denisd3d.config4j.OnlyIf;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplate;
import fr.denisd3d.mc2discord.core.config.converters.RandomTemplateConverter;
import fr.denisd3d.mc2discord.core.config.converters.SnowflakeConverter;
import fr.denisd3d.mc2discord.core.config.converters.TemplateConverter;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.io.File;
import java.util.List;
import java.util.function.Function;

public class M2DConfig extends Config4J {

    @SerdeKey("lang")
    @SerdeComment("config.lang.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    @SuppressWarnings("unused")
    public String lang = "en_us";

    @SerdeKey("General")
    @SerdeComment("config.general.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public General general = new General();

    @SerdeKey("Channels")
    @SerdeComment("config.channels.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Channels channels = new Channels();

    @SerdeKey("Messages")
    @SerdeComment("config.messages.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Messages messages = new Messages();

    @SerdeKey("Commands")
    @SerdeComment("config.commands.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Commands commands = new Commands();

    @SerdeKey("Features")
    @SerdeComment("config.features.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Features features = new Features();

    @SerdeKey("StatusChannels")
    @SerdeComment("config.status_channels.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    @OnlyIf("Features.status_channels")
    public StatusChannels statusChannels = new StatusChannels();

    @SerdeKey("Account")
    @SerdeComment("config.account.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    @OnlyIf("Features.account_linking")
    public Account account = new Account();

    @SerdeKey("Style")
    @SerdeComment("config.style.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Style style = new Style();

    @SerdeKey("Misc")
    @SerdeComment("config.misc.comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public Misc misc = new Misc();

    @SerdeKey("comment")
    @SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
    public String comment;

    public M2DConfig(File file, Function<String, String> translator) {
        super(file, translator);
    }

    @Override
    protected void registerSerializers(ObjectSerializerBuilder builder) {
        builder.withSerializerForClass(Snowflake.class, new SnowflakeConverter());
        builder.withSerializerForClass(Template.class, new TemplateConverter());
        builder.withSerializerForClass(RandomTemplate.class, new RandomTemplateConverter());
    }

    @Override
    protected void registerDeserializers(ObjectDeserializerBuilder builder) {
        builder.withDeserializerProvider(deserializerFor(Snowflake.class, new SnowflakeConverter()));
        builder.withDeserializerProvider(deserializerFor(Template.class, new TemplateConverter()));
        builder.withDeserializerProvider(deserializerFor(RandomTemplate.class, new RandomTemplateConverter()));
    }

    /**
     * @return A provider giving the deserializer for fields (or list elements) of the given type, whatever the config value type
     */
    private static <R> ValueDeserializerProvider<Object, R> deserializerFor(Class<R> type, ValueDeserializer<Object, R> deserializer) {
        return (valueClass, resultType) -> resultType.getSatisfyingRawType().filter(type::equals).isPresent() ? deserializer : null;
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

        if (this.misc.allowed_mention == null) {
            this.misc.allowed_mention = List.of("ROLE", "USER");
        }

        if (this.misc.broadcast_commands == null) {
            this.misc.broadcast_commands = List.of("say", "me", "tellraw");
        }

        if (this.misc.other_mods_messages.isEmpty()) {
            this.misc.other_mods_messages.add(new Misc.OtherModMessage());
        }

        if (this.style.embed_colors.isEmpty()) {
            this.style.embed_colors.add("info", "SUMMER_SKY");
            this.style.embed_colors.add("chat", "MEDIUM_SEA_GREEN");
            this.style.embed_colors.add("command", "MEDIUM_SEA_GREEN");
            this.style.embed_colors.add("logs", "SUMMER_SKY");
        }

        if (this.account.policies.isEmpty()) {
            this.account.policies.add(new Account.AccountPolicy());
        }
    }
}
