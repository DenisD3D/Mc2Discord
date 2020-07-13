package ml.denisd3d.minecraft2discord.variables;

import java.util.HashMap;
import java.util.function.Function;

public class MessageParameterType implements IParameterType<String>
{
    private final HashMap<String, Function<String, String>> parameters = new HashMap<>();

    public MessageParameterType()
    {
        parameters.put("message", s -> s);
    }

    @Override
    public String get(String key, Object variable)
    {
        return parameters.get(key).apply((String) variable);
    }

    @Override
    public void set(String key, Function<String, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }
}
