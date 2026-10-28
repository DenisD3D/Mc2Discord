package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.serde.DeserializerContext;
import com.electronwill.nightconfig.core.serde.SerializerContext;
import com.electronwill.nightconfig.core.serde.TypeConstraint;
import com.electronwill.nightconfig.core.serde.ValueDeserializer;
import com.electronwill.nightconfig.core.serde.ValueSerializer;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;

import java.util.Optional;

public class SnowflakeConverter implements ValueSerializer<Snowflake, Object>, ValueDeserializer<Object, Snowflake> {
    @Override
    public Snowflake deserialize(Object value, Optional<TypeConstraint> resultType, DeserializerContext ctx) {
        if (value == null) {
            return M2DUtils.NIL_SNOWFLAKE;
        } else if (value instanceof String) {
            return Snowflake.of((String) value);
        } else if (value instanceof Number) {
            return Snowflake.of(((Number) value).longValue());
        } else {
            throw new IllegalArgumentException("Cannot convert " + value + " to a Snowflake");
        }
    }

    @Override
    public Object serialize(Snowflake value, SerializerContext ctx) {
        return value.asLong();
    }
}
