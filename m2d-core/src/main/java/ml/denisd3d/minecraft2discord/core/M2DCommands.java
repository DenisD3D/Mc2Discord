package ml.denisd3d.minecraft2discord.core;

import discord4j.common.util.TokenUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class M2DCommands {
    public static List<String> getStatus() {
        List<String> response = new ArrayList<>();

        response.add("=====Minecraft2Discord=====");
        response.add("Bot name: " + Minecraft2Discord.INSTANCE.botName + "#" + Minecraft2Discord.INSTANCE.botDiscriminator);
        response.add("Bot id: " + Minecraft2Discord.INSTANCE.getBotId());
        response.add("Status: " + Minecraft2Discord.INSTANCE.getState());

        String newVersion = Minecraft2Discord.INSTANCE.iMinecraft.getNewVersion();
        if (!newVersion.isEmpty()) {
            response.add("New version: " + newVersion);
        }

        if (Minecraft2Discord.INSTANCE.errors.size() != 0) {
            response.add("Errors: ");
            response.addAll(Minecraft2Discord.INSTANCE.errors);
        } else {
            response.add("No error");
        }
        return response;
    }

    public static List<String> restart() {
        List<String> response = new ArrayList<>();
        Minecraft2Discord.INSTANCE.restart();
        response.add("Restarting the discord bot...");

        return response;
    }

    public static String[] upload() {
        try {
            HttpPost post = new HttpPost("https://m2d.denisd3d.ml/api/v1/uploads");

            String config = String.join("\n", Files.readAllLines(Minecraft2Discord.CONFIG_FILE.toPath()));

            String configWithoutToken = config.substring(0, config.indexOf("token = ")) +
                    "token = REMOVED|" + (M2DUtils.isTokenValid(Minecraft2Discord.INSTANCE.config.token) ? ("VALID|" + TokenUtil.getSelfId(Minecraft2Discord.INSTANCE.config.token)) : Minecraft2Discord.INSTANCE.config.token.isEmpty() ? "EMPTY" : "INVALID") +
                    config.substring(config.indexOf("\n", config.indexOf("oken = ")));

            // add request parameter, form parameters
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("config", StringEscapeUtils.escapeJavaScript(configWithoutToken)));
            urlParameters.add(new BasicNameValuePair("errors", Minecraft2Discord.INSTANCE.errors.isEmpty() ? "None" : String.join("\n", Minecraft2Discord.INSTANCE.errors)));
            urlParameters.add(new BasicNameValuePair("env", Minecraft2Discord.INSTANCE.iMinecraft.getEnvInfo()));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                ResponseHandler<String> responseHandler = response -> {
                    int status = response.getStatusLine().getStatusCode();
                    if (status >= 200 && status < 300) {
                        HttpEntity responseEntity = response.getEntity();
                        return responseEntity != null ? EntityUtils.toString(responseEntity) : null;
                    } else {
                        throw new ClientProtocolException("Unexpected response status: " + status);
                    }
                };
                String responseBody = httpClient.execute(post, responseHandler);
                return new String[]{"Your link : ", responseBody};
            }
        } catch (Exception e) {
            Minecraft2Discord.logger.error(e);
            return new String[]{"Upload failed", ""};
        }
    }

    public static String getDiscordText() {
        return Minecraft2Discord.INSTANCE.config.discord_text;
    }

    public static String getDiscordLink() {
        return Minecraft2Discord.INSTANCE.config.discord_link;
    }

    public static String getInviteLink() {
        return M2DUtils.isTokenValid(Minecraft2Discord.INSTANCE.config.token) ? "https://discord.com/api/oauth2/authorize?client_id=" + TokenUtil.getSelfId(Minecraft2Discord.INSTANCE.config.token) + "&permissions=872926289&scope=bot" : "The discord bot isn't started";
    }
}
