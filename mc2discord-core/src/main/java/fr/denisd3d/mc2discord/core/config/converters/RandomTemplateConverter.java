package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.serde.DeserializerContext;
import com.electronwill.nightconfig.core.serde.SerializerContext;
import com.electronwill.nightconfig.core.serde.TypeConstraint;
import com.electronwill.nightconfig.core.serde.ValueDeserializer;
import com.electronwill.nightconfig.core.serde.ValueSerializer;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomTemplateConverter implements ValueSerializer<RandomTemplate, Object>, ValueDeserializer<Object, RandomTemplate> {
    @Override
    public RandomTemplate deserialize(Object value, Optional<TypeConstraint> resultType, DeserializerContext ctx) {
        if (value == null) {
            return new RandomTemplate("");
        } else if (value instanceof String) {
            return new RandomTemplate((String) value);
        } else if (value instanceof List<?> list) {
            if (list.isEmpty())
                return new RandomTemplate("");
            List<Template> outputList = new ArrayList<>(list.size());
            for (Object obj : list) {
                outputList.add(Template.parse(obj.toString()));
            }
            return new RandomTemplate(outputList);
        } else {
            throw new IllegalArgumentException("Cannot convert " + value + " to a RandomTemplate");
        }
    }

    @Override
    public Object serialize(RandomTemplate value, SerializerContext ctx) {
        if (value == null)
            return null;
        else if (value.getValues().size() == 1)
            return value.getValues().get(0).source();
        else
            return value.getValues().stream().map(Template::source).toList();
    }
}
