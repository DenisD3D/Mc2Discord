package fr.denisd3d.mc2discord.core;

import discord4j.common.util.Snowflake;
import discord4j.common.util.TokenUtil;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerEntry;
import fr.denisd3d.mc2discord.core.storage.HiddenPlayerList;
import fr.denisd3d.mc2discord.core.storage.LinkedPlayerEntry;
import fr.denisd3d.mc2discord.core.storage.LinkedPlayerList;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class M2DCommands {
    public static List<String> getStatus() {
        List<String> result = new ArrayList<>();

        result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.title"));
        if (M2DUtils.isNotConfigured()) {
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.invalid_config"));
        } else {
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.bot_name", Mc2Discord.INSTANCE.vars.bot_name, Mc2Discord.INSTANCE.vars.bot_discriminator));
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.bot_id", Mc2Discord.INSTANCE.vars.bot_id.asString()));
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.state", Mc2Discord.INSTANCE.client.getGatewayClient(0).flatMap(gatewayClient -> gatewayClient.isConnected().blockOptional()).map(connected -> connected ? "Connected" : "Disconnected").orElse("Disconnected")));
            for (int shard_id = 0; shard_id < Mc2Discord.INSTANCE.client.getGatewayClientGroup().getShardCount(); shard_id++) {
                if (Mc2Discord.INSTANCE.client.getGatewayClientGroup().find(shard_id).isPresent()) {
                    result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.shard", shard_id, Mc2Discord.INSTANCE.client.getGatewayClientGroup().find(shard_id).get().getResponseTime().toString().substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ").toLowerCase()));
                }
            }
        }

        String newVersion = Mc2Discord.INSTANCE.minecraft.getNewVersion();
        if (newVersion != null) {
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.version", newVersion));
        }

        if (Mc2Discord.INSTANCE.errors.size() != 0) {
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.errors"));
            result.addAll(Mc2Discord.INSTANCE.errors);
        } else {
            result.add(Mc2Discord.INSTANCE.langManager.translate("commands.status.no_error"));
        }
        return result;
    }

    public static String restart() {
        try {
            Mc2Discord.INSTANCE.restart();
        } catch (Exception ignored) {
            Mc2Discord.INSTANCE = new Mc2Discord(Mc2Discord.INSTANCE.minecraft);
        }
        return Mc2Discord.INSTANCE.langManager.translate("commands.restart.content");
    }

    public static String[] upload() {
        try {
            String config = String.join("\n", Files.readAllLines(M2DUtils.CONFIG_FILE.toPath()));

            int token_start_index = config.indexOf("token = ") + 9;
            String token = config.substring(token_start_index, config.indexOf("\"", token_start_index + 1));
            String configWithoutToken = config.substring(0, token_start_index) + "REMOVED|" + (M2DUtils.isTokenValid(token) ? ("VALID|" + TokenUtil.getSelfId(token)) : token.isEmpty() ? "EMPTY" : "INVALID") + "\"" + config.substring(config.indexOf("\n", token_start_index));

            String responseBody = HttpClient.create().post().uri("http://m2d.denisd3d.fr/api/v1/upload/").sendForm((httpClientRequest, httpClientForm) -> httpClientForm.attr("config", configWithoutToken).attr("errors", Mc2Discord.INSTANCE.errors.isEmpty() ? "None" : String.join("\n", Mc2Discord.INSTANCE.errors)).attr("env", Mc2Discord.INSTANCE.minecraft.getEnvInfo())).responseSingle((response, bytes) -> {
                if (response.status().code() != 200)
                    return Mono.error(new Exception("Unexpected response status: " + response.status().code()));
                else return bytes.asString();
            }).onErrorResume(throwable -> Mono.just("_" + throwable.getMessage())).block();

            if (responseBody != null && responseBody.startsWith("_")) throw new Exception(responseBody.substring(1));

            return new String[]{Mc2Discord.INSTANCE.langManager.translate("commands.upload.success"), responseBody};
        } catch (Exception e) {
            return new String[]{Mc2Discord.INSTANCE.langManager.translate("commands.upload.error", e.getLocalizedMessage()), ""};
        }
    }

    public static String getDiscordText() {
        return Mc2Discord.INSTANCE.config.misc.discord_text;
    }

    public static String getDiscordLink() {
        return Mc2Discord.INSTANCE.config.misc.discord_link;
    }

    public static String getInviteLink() {
        return M2DUtils.isTokenValid(Mc2Discord.INSTANCE.config.general.token) ? "https://discord.com/api/oauth2/authorize?client_id=" + TokenUtil.getSelfId(Mc2Discord.INSTANCE.config.general.token) + "&permissions=604359761&scope=bot" : Mc2Discord.INSTANCE.langManager.translate("commands.invite.error");
    }

    public static String listHiddenPlayers() {
        UUID[] hiddenPlayersUUID = Mc2Discord.INSTANCE.hiddenPlayerList.getPlayerList();
        if (hiddenPlayersUUID.length == 0) {
            return Mc2Discord.INSTANCE.langManager.translate("commands.hidden.empty");
        } else {
            return Mc2Discord.INSTANCE.langManager.translate("commands.hidden.list", hiddenPlayersUUID.length, String.join(", ", Arrays.stream(hiddenPlayersUUID).map(uuid -> Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(uuid)).toList()));
        }
    }

    public static List<String> addHiddenPlayers(List<UUID> targets) {
        List<String> result = new ArrayList<>();
        HiddenPlayerList hiddenPlayerList = Mc2Discord.INSTANCE.hiddenPlayerList;

        for (UUID target : targets) {
            if (!hiddenPlayerList.contains(target)) {
                HiddenPlayerEntry hiddenPlayerEntry = new HiddenPlayerEntry(target);
                hiddenPlayerList.add(hiddenPlayerEntry);
                result.add(Mc2Discord.INSTANCE.langManager.translate("commands.hidden.hidden", Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(target)));
            }
        }
        return result;
    }

    public static List<String> removeHiddenPlayers(List<UUID> targets) {
        List<String> result = new ArrayList<>();
        HiddenPlayerList hiddenPlayerList = Mc2Discord.INSTANCE.hiddenPlayerList;

        for (UUID target : targets) {
            if (hiddenPlayerList.contains(target)) {
                hiddenPlayerList.remove(target);
                result.add(Mc2Discord.INSTANCE.langManager.translate("commands.hidden.visible", Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(target)));
            }
        }
        return result;
    }

    public static String reloadHiddenPlayers() throws IOException {
        Mc2Discord.INSTANCE.hiddenPlayerList.load();
        return Mc2Discord.INSTANCE.langManager.translate("commands.hidden.reload");
    }

    public static Tuple2<String, Integer> listLinkedPlayers() {
        UUID[] linkedPlayersUUID = Mc2Discord.INSTANCE.linkedPlayerList.getPlayerList();
        if (linkedPlayersUUID.length == 0) {
            return Tuples.of(Mc2Discord.INSTANCE.langManager.translate("commands.linked.empty"), 0);
        } else {
            return Tuples.of(Mc2Discord.INSTANCE.langManager.translate("commands.linked.list", linkedPlayersUUID.length, String.join(", ", Arrays.stream(linkedPlayersUUID).map(uuid -> Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(uuid)).toList())), linkedPlayersUUID.length);
        }
    }


    public static String addLinkedPlayers(UUID target_uuid, long discord_id) {
        if (Mc2Discord.INSTANCE.linkedPlayerList.contains(target_uuid))
            return null;

        LinkedPlayerEntry linkedPlayerEntry = new LinkedPlayerEntry(target_uuid, Snowflake.of(discord_id));
        Mc2Discord.INSTANCE.linkedPlayerList.add(linkedPlayerEntry);
        return Mc2Discord.INSTANCE.langManager.translate("commands.linked.linked", Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(target_uuid));
    }

    public static List<String> removeLinkedPlayers(List<UUID> targets) {
        List<String> result = new ArrayList<>();
        LinkedPlayerList linkedPlayerList = Mc2Discord.INSTANCE.linkedPlayerList;

        for (UUID target : targets) {
            if (linkedPlayerList.contains(target)) {
                linkedPlayerList.remove(target);
                result.add(Mc2Discord.INSTANCE.langManager.translate("commands.linked.unlinked", Mc2Discord.INSTANCE.minecraft.getPlayerNameFromUUID(target)));
            }
        }
        return result;
    }

    public static String reloadLinkedPlayers() throws IOException {
        Mc2Discord.INSTANCE.linkedPlayerList.load();
        return Mc2Discord.INSTANCE.langManager.translate("commands.linked.reload");
    }
}
