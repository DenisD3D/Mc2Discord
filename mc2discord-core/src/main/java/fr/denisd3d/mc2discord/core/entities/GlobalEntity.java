package fr.denisd3d.mc2discord.core.entities;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.Vars;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Map;
import java.util.function.BiFunction;

public class GlobalEntity extends Entity {
    public final int onlinePlayers;
    public final int maxPlayers;
    public final int uniquePlayers;
    public final String motd;
    public final String mcVersion;
    public final String serverHostname;
    public final String serverPort;

    public GlobalEntity(int onlinePlayers, int maxPlayers, int uniquePlayers, String motd, String mcVersion, String serverHostname, String serverPort) {
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.uniquePlayers = uniquePlayers;
        this.motd = motd;
        this.mcVersion = mcVersion;
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
    }

    @Override
    public void getReplacements(Map<String, String> replacements, Map<String, BiFunction<String, String, String>> formatters) {
        replacements.put("online_players", String.valueOf(this.onlinePlayers));
        replacements.put("max_players", String.valueOf(this.maxPlayers));
        replacements.put("unique_players", String.valueOf(this.uniquePlayers));
        replacements.put("motd", this.motd);
        replacements.put("mc_version", this.mcVersion);
        replacements.put("server_hostname", this.serverHostname);
        replacements.put("server_port", this.serverPort);
        replacements.put("now", String.valueOf(System.currentTimeMillis()));
        replacements.put("uptime", String.valueOf(System.currentTimeMillis() - Vars.startTime));
        replacements.put("bot_name", Mc2Discord.INSTANCE.vars.bot_name);
        replacements.put("bot_discriminator", Mc2Discord.INSTANCE.vars.bot_discriminator);
        replacements.put("bot_id", Mc2Discord.INSTANCE.vars.bot_id.asString());
        replacements.put("bot_display_name", Mc2Discord.INSTANCE.vars.mc2discord_display_name);
        replacements.put("bot_avatar_url", Mc2Discord.INSTANCE.vars.mc2discord_avatar);

        formatters.put("now", (format, value) -> DateFormatUtils.format(Long.parseLong(value), format));
        formatters.put("uptime", (format, value) -> DurationFormatUtils.formatDuration(Long.parseLong(value), format));
    }
}
