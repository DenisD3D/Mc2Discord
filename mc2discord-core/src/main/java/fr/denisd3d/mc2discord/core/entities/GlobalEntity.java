package fr.denisd3d.mc2discord.core.entities;

import fr.denisd3d.mc2discord.core.Mc2Discord;
import fr.denisd3d.mc2discord.core.Vars;

import java.time.Duration;
import java.time.Instant;

/**
 * Variables available in every template, read only when a template uses them
 */
public final class GlobalEntity implements Entity {
    public static final GlobalEntity INSTANCE = new GlobalEntity();

    private GlobalEntity() {
    }

    @Override
    public Object get(String variable) {
        return switch (variable) {
            case "online_players" -> Mc2Discord.INSTANCE.minecraft.getOnlinePlayerCount();
            case "max_players" -> Mc2Discord.INSTANCE.minecraft.getMaxPlayers();
            case "unique_players" -> Mc2Discord.INSTANCE.minecraft.getUniquePlayerCount();
            case "motd" -> Mc2Discord.INSTANCE.minecraft.getMotd();
            case "mc_version" -> Mc2Discord.INSTANCE.minecraft.getMinecraftVersion();
            case "server_hostname" -> Mc2Discord.INSTANCE.minecraft.getServerHostname();
            case "server_port" -> Mc2Discord.INSTANCE.minecraft.getServerPort();
            case "mod_loader" -> Mc2Discord.INSTANCE.vars.modLoader;
            case "mod_loader_version" -> Mc2Discord.INSTANCE.vars.modLoaderVersion;
            case "mod_count" -> Mc2Discord.INSTANCE.vars.modCount;
            case "tps" -> Mc2Discord.INSTANCE.minecraft.getTps();
            case "mspt" -> Mc2Discord.INSTANCE.minecraft.getMspt();
            case "now" -> Instant.now();
            case "uptime" -> Vars.startTime == null ? Duration.ZERO : Duration.ofMillis(System.currentTimeMillis() - Vars.startTime);
            case "bot_name" -> Mc2Discord.INSTANCE.vars.bot_name;
            case "bot_discriminator" -> Mc2Discord.INSTANCE.vars.bot_discriminator;
            case "bot_id" -> Mc2Discord.INSTANCE.vars.bot_id.asString();
            case "bot_display_name" -> Mc2Discord.INSTANCE.vars.mc2discord_display_name;
            case "bot_avatar_url" -> Mc2Discord.INSTANCE.vars.mc2discord_avatar;
            default -> null;
        };
    }
}
