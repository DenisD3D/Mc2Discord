package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.conversion.Converter;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public class SnowflakeArrayConverter implements Converter<List<Snowflake>, List<Object>> {
    public List<Snowflake> convertToField(List<Object> value) {
        if (value != null) {
            List<Snowflake> list = new ArrayList<>();
            value.forEach(object -> {
                if (object instanceof String) list.add(Snowflake.of((String) object));
                else if (object instanceof Number) list.add(Snowflake.of(((Number) object).longValue()));
            });
            return list;
        } else {
            return null;
        }
    }

    public List<Object> convertFromField(List<Snowflake> value) {
        if (value != null) {
            List<Object> list = new ArrayList<>();
            value.forEach(snowflake -> list.add(snowflake.asLong()));
            return list;
        } else {
            return null;
        }
    }
}
