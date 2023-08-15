package fr.denisd3d.mc2discord.core.config.converters;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomString {
    private final List<String> values;
    private final Random rand = new Random();

    public RandomString(String value) {
        this.values = new ArrayList<>();
        this.values.add(value);
    }

    public RandomString(List<String> values) {
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }

    public String asString() {
        return this.values.get(this.rand.nextInt(this.values.size()));
    }
}
