package ml.denisd3d.minecraft2discord.variables;

import net.minecraftforge.event.entity.player.AdvancementEvent;

import java.util.HashMap;
import java.util.function.Function;

public class AdvancementParameterType implements IParameterType<AdvancementEvent>
{
    private final HashMap<String, Function<AdvancementEvent, String>> parameters = new HashMap<>();

    public AdvancementParameterType()
    {
        parameters.put("advancement_title", advancementEvent -> advancementEvent.getAdvancement().getDisplay() != null ? advancementEvent.getAdvancement().getDisplay().getTitle().getFormattedText() : "");
        parameters.put("advancement_description", advancementEvent -> advancementEvent.getAdvancement().getDisplay() != null ? advancementEvent.getAdvancement().getDisplay().getDescription().getFormattedText() : "");
    }

    @Override
    public String get(String key, Object variable)
    {
        return parameters.get(key).apply((AdvancementEvent) variable);
    }

    @Override
    public void set(String key, Function<AdvancementEvent, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }
}
