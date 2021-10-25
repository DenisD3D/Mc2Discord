package ml.denisd3d.mc2discord.core;

import discord4j.common.util.TokenUtil;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class M2DCommands {

    public static List<String> getStatus() {
        List<String> response = new ArrayList<>();

        response.add(LangManager.translate("commands.status.title"));
        response.add(LangManager.translate("commands.status.bot_name", Mc2Discord.INSTANCE.botName, Mc2Discord.INSTANCE.botDiscriminator));
        response.add(LangManager.translate("commands.status.bot_id", Mc2Discord.INSTANCE.getBotId()));
        response.add(LangManager.translate("commands.status.state", Mc2Discord.INSTANCE.getState()));
        for (int shard_id = 0; shard_id <= Mc2Discord.INSTANCE.client.getGatewayClientGroup().getShardCount(); shard_id++) {
            if (Mc2Discord.INSTANCE.client.getGatewayClientGroup().find(shard_id).isPresent()) {
                response.add(LangManager.translate("commands.status.shard",
                        shard_id,
                        Mc2Discord.INSTANCE.client.getGatewayClientGroup().find(shard_id).get().getResponseTime().toString()
                                .substring(2).replaceAll("(\\d[HMS])(?!$)", "$1 ")
                                .toLowerCase()));
            }

        }

        String newVersion = Mc2Discord.INSTANCE.iMinecraft.getNewVersion();
        if (!newVersion.isEmpty()) {
            response.add(LangManager.translate("commands.status.version", newVersion));
        }

        if (Mc2Discord.INSTANCE.errors.size() != 0) {
            response.add(LangManager.translate("commands.status.errors"));
            response.addAll(Mc2Discord.INSTANCE.errors);
        } else {
            response.add(LangManager.translate("commands.status.no_error"));
        }
        return response;
    }

    public static List<String> restart() {
        List<String> response = new ArrayList<>();
        Mc2Discord.INSTANCE.restart();
        response.add(LangManager.translate("commands.restart.content"));

        return response;
    }

    public static String[] upload() {
        try {
            String config = String.join("\n", Files.readAllLines(Mc2Discord.CONFIG_FILE.toPath()));

            int token_start_index = config.indexOf("token = ") + 9;
            String token = config.substring(token_start_index, config.indexOf("\"", token_start_index + 1));
            String configWithoutToken = config.substring(0, token_start_index) +
                    "REMOVED|" + (M2DUtils.isTokenValid(token) ? ("VALID|" + TokenUtil.getSelfId(token)) : token.isEmpty() ? "EMPTY" : "INVALID") +
                    config.substring(config.indexOf("\n", token_start_index));

            String responseBody = HttpClient.create()
                    .post()
                    .uri("http://m2d.denisd3d.ml/api/v1/upload/")
                    .sendForm((httpClientRequest, httpClientForm) ->
                            httpClientForm.attr("config", configWithoutToken)
                                    .attr("errors", Mc2Discord.INSTANCE.errors.isEmpty() ? "None" : String.join("\n", Mc2Discord.INSTANCE.errors))
                                    .attr("env", Mc2Discord.INSTANCE.iMinecraft.getEnvInfo())
                    ).responseSingle((response, bytes) -> {
                        if (response.status().code() != 200)
                            return Mono.error(new Exception("Unexpected response status: " + response.status().code()));
                        else
                            return bytes.asString();
                    }).onErrorResume(throwable -> Mono.just("_" + throwable.getMessage()))
                    .block();

            if (responseBody != null && responseBody.startsWith("_"))
                throw new Exception(responseBody.substring(1));

            return new String[]{LangManager.translate("commands.upload.success"), responseBody};
        } catch (Exception e) {
            return new String[]{LangManager.translate("commands.upload.error", e.getLocalizedMessage()), ""};
        }
    }

    public static String getDiscordText() {
        return Mc2Discord.INSTANCE.config.misc.discord_text;
    }

    public static String getDiscordLink() {
        return Mc2Discord.INSTANCE.config.misc.discord_link;
    }

    public static String getInviteLink() {
        return M2DUtils.isTokenValid(Mc2Discord.INSTANCE.config.general.token) ? "https://discord.com/api/oauth2/authorize?client_id=" + TokenUtil.getSelfId(Mc2Discord.INSTANCE.config.general.token) + "&permissions=604359761&scope=bot" : LangManager.translate("commands.invite.error");
    }
}
