package ml.denisd3d.mc2discord.core.config.account;

import com.electronwill.nightconfig.core.conversion.Converter;
import discord4j.common.util.Snowflake;

import java.util.ArrayList;
import java.util.List;

public class SnowflakeArrayConverter implements Converter<List<Snowflake>, List<Number>> {
    public List<Snowflake> convertToField(List<Number> value) {
        if (value != null) {
            List<Snowflake> list = new ArrayList<>();
            value.forEach(integer -> list.add(Snowflake.of(integer.longValue())));
            return list;
        } else {
            return null;
        }
    }

    public List<Number> convertFromField(List<Snowflake> value) {
        if (value != null) {
            List<Number> list = new ArrayList<>();
            value.forEach(snowflake -> list.add(snowflake.asLong()));
            return list;
        } else {
            return null;
        }
    }
}
