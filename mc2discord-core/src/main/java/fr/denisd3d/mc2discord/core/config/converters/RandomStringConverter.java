package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.conversion.Converter;

import java.util.ArrayList;
import java.util.List;

public class RandomStringConverter implements Converter<RandomString, Object> {
    @Override
    public RandomString convertToField(Object value) {
        if (value == null) {
            return new RandomString("");
        } else if (value instanceof String) {
            return new RandomString((String) value);
        } else if (value instanceof List<?>) {
            List<String> outputList = new ArrayList<>(((List<?>) value).size());
            for (Object obj : (List<?>) value) {
                outputList.add(obj.toString());
            }
            return new RandomString(outputList);
        } else {
            throw new IllegalArgumentException(
                    "Cannot convert " + value + " to a RandomString");
        }
    }

    @Override
    public Object convertFromField(RandomString value) {
        if (value.getValues().size() == 1)
            return value.getValues().get(0);
        else
            return value.getValues();
    }
}
