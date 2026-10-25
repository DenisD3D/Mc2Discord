package fr.denisd3d.mc2discord.core.config.converters;

import com.electronwill.nightconfig.core.conversion.Converter;
import fr.denisd3d.mc2discord.core.entities.Template;

public class TemplateConverter implements Converter<Template, String> {
    @Override
    public Template convertToField(String value) {
        return Template.parse(value == null ? "" : value);
    }

    @Override
    public String convertFromField(Template value) {
        return value == null ? null : value.source();
    }
}
