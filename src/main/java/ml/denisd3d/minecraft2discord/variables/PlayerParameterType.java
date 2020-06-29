package ml.denisd3d.minecraft2discord.variables;

import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.function.Function;

public class PlayerParameterType implements IParameterType<PlayerEntity>
{
    private final HashMap<String, Function<PlayerEntity, String>> parameters = new HashMap<>();

    public PlayerParameterType()
    {
        parameters.put("player_name", playerEntity -> playerEntity.getDisplayName().getFormattedText());
        parameters.put("player_uuid", playerEntity -> playerEntity.getGameProfile().getId().toString());
        parameters.put("player_health", playerEntity -> String.format("%d", (long) playerEntity.getHealth()));
    }

    @Override
    public String get(String key, Object variable)
    {
        return parameters.get(key).apply((PlayerEntity) variable);
    }

    @Override
    public void set(String key, Function<PlayerEntity, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }
}
