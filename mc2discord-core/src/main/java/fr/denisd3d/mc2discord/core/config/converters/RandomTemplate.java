package fr.denisd3d.mc2discord.core.config.converters;

import fr.denisd3d.mc2discord.core.entities.Entity;
import fr.denisd3d.mc2discord.core.entities.Template;

import java.util.List;
import java.util.Random;

/**
 * One or several templates, one of them is picked randomly each time the text is used
 */
public class RandomTemplate {
    private final List<Template> values;
    private final Random rand = new Random();

    public RandomTemplate(String value) {
        this(List.of(Template.parse(value)));
    }

    public RandomTemplate(List<Template> values) {
        this.values = values;
    }

    public List<Template> getValues() {
        return values;
    }

    public boolean isEmpty() {
        return values.get(0).isEmpty();
    }

    public Template pick() {
        return this.values.get(this.rand.nextInt(this.values.size()));
    }

    public String render(Entity... entities) {
        return pick().render(entities);
    }
}
