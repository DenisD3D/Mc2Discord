package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.conversion.Converter;
import discord4j.common.util.Snowflake;
import fr.denisd3d.mc2discord.core.M2DUtils;

public class SnowflakeConverter implements Converter<Snowflake, Object> {
    @Override
    public Snowflake convertToField(Object value) {
        if (value == null) {
            return M2DUtils.NIL_SNOWFLAKE;
        } else if (value instanceof String) {
            return Snowflake.of((String) value);
        } else if (value instanceof Number) {
            return Snowflake.of(((Number) value).longValue());
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert " + value + " to a Snowflake");
        }
    }

    @Override
    public Object convertFromField(Snowflake value) {
        return value.asLong();
    }
}
