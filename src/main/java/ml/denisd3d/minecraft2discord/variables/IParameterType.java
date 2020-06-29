package ml.denisd3d.minecraft2discord.variables;

import java.util.function.Function;

public interface IParameterType<K>
{

    String get(String key, Object variable);

    void set(String key, Function<K, String> value);

    boolean containsKey(String key);
}
