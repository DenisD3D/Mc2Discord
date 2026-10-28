package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.serde.DeserializerContext;
import com.electronwill.nightconfig.core.serde.SerializerContext;
import com.electronwill.nightconfig.core.serde.TypeConstraint;
import com.electronwill.nightconfig.core.serde.ValueDeserializer;
import com.electronwill.nightconfig.core.serde.ValueSerializer;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.util.Optional;

public class TemplateConverter implements ValueSerializer<Template, String>, ValueDeserializer<Object, Template> {
    @Override
    public Template deserialize(Object value, Optional<TypeConstraint> resultType, DeserializerContext ctx) {
        return Template.parse(value == null ? "" : (String) value);
    }

    @Override
    public String serialize(Template value, SerializerContext ctx) {
        return value == null ? null : value.source();
    }
}
