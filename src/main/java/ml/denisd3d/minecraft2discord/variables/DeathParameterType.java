package ml.denisd3d.minecraft2discord.variables;

import net.minecraftforge.event.entity.living.LivingDeathEvent;

import java.util.HashMap;
import java.util.function.Function;

public class DeathParameterType implements IParameterType<LivingDeathEvent>
{
    private final HashMap<String, Function<LivingDeathEvent, String>> parameters = new HashMap<>();

    public DeathParameterType()
    {
        parameters.put("death_message", livingDeathEvent -> livingDeathEvent.getEntityLiving().getCombatTracker().getDeathMessage().getFormattedText());
        parameters.put("death_unformatted_message", livingDeathEvent -> livingDeathEvent.getEntityLiving().getCombatTracker().getDeathMessage().getUnformattedComponentText());
        parameters.put("death_key", livingDeathEvent -> "death.attack." + livingDeathEvent.getSource().getDamageType());
        parameters.put("death_attacker_name", livingDeathEvent -> livingDeathEvent.getEntityLiving().getAttackingEntity() != null ? livingDeathEvent.getEntityLiving().getAttackingEntity().getDisplayName().getFormattedText() : "");
        parameters.put("death_attacker_health", livingDeathEvent -> livingDeathEvent.getEntityLiving().getAttackingEntity() != null ? String.format("%d", (long) livingDeathEvent.getEntityLiving().getAttackingEntity().getHealth()) : "");

    }

    @Override
    public String get(String key, Object variable)
    {
        return parameters.get(key).apply((LivingDeathEvent) variable);
    }

    @Override
    public void set(String key, Function<LivingDeathEvent, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }
}
