package ml.denisd3d.minecraft2discord.variables;

import ml.denisd3d.minecraft2discord.Config;
import ml.denisd3d.minecraft2discord.Minecraft2Discord;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Function;

public class GlobalParameterType implements IParameterType<Void>
{
    private final HashMap<String, Function<Void, String>> parameters = new HashMap<>();
    private static SaveFormat.LevelSave anvilConverterForAnvilFile;

    public GlobalParameterType()
    {
        parameters.put("global_online_players", aVoid -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getCurrentPlayerCount()));
        parameters.put("global_max_players", aVoid -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getMaxPlayers()));
        parameters.put("global_unique_player", aVoid -> String.valueOf(Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().playerDataManager.getPlayerDataFolder().list((dir, name) -> name.endsWith(".dat"))).map(list -> list.length).orElse(0)));
        parameters.put("global_motd", aVoid -> ServerLifecycleHooks.getCurrentServer().getMOTD());
        parameters.put("global_mc_version", aVoid -> ServerLifecycleHooks.getCurrentServer().getMinecraftVersion());
        parameters.put("global_server_hostname", aVoid -> ServerLifecycleHooks.getCurrentServer().getServerHostname());
        parameters.put("global_server_port", aVoid -> String.valueOf(ServerLifecycleHooks.getCurrentServer().getServerPort()));
        parameters.put("global_date", aVoid -> DateFormatUtils.format(System.currentTimeMillis(), Config.SERVER.dateFormat.get()));
        parameters.put("global_uptime", aVoid -> DurationFormatUtils.formatDuration(System.currentTimeMillis() - Minecraft2Discord.getStartedTime(), Config.SERVER.uptimeFormat.get()));
    }

    @Override
    public String get(String key, Object aVoid)
    {
        return parameters.get(key).apply(null);
    }

    @Override
    public void set(String key, Function<Void, String> value)
    {
        parameters.put(key, value);
    }

    @Override
    public boolean containsKey(String key)
    {
        return parameters.containsKey(key);
    }

}
