package fr.denisd3d.mc2discord.core;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class LangManager {
    public static final List<String> AVAILABLE_LANG = Arrays.asList("en_us", "fr_fr", "ru_ru", "ko_kr", "zh_cn" );
    public static final List<String> LANG_CONTRIBUTORS = Arrays.asList("Morty#0273 (ru_ru)", "PixelVoxel#4327 (ko_kr)");
    private static final Gson GSON = new Gson();
    private static final Pattern FLOAT_REPLACE_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

    protected final Map<String, String> properties = new HashMap<>();

    public LangManager() {
        String lang = null;
        try {
            if (M2DUtils.CONFIG_FILE.exists()) {
                Scanner scanner = new Scanner(M2DUtils.CONFIG_FILE);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    int index = line.indexOf("lang = ");
                    if (index != -1) {
                        lang = line.substring(index + 8, index + 13); // get 4 letter
                        break;
                    }
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        loadLang("en_us");
        if (lang != null && AVAILABLE_LANG.contains(lang) && !lang.equals("en_us")) {
            loadLang(lang);
        }
    }

    public static JsonObject getJsonObject(JsonElement json, String memberName) {
        if (json.isJsonObject()) {
            return json.getAsJsonObject();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a JsonObject, was " + toString(json));
        }
    }

    public static String getString(JsonElement json, String memberName) {
        if (json.isJsonPrimitive()) {
            return json.getAsString();
        } else {
            throw new JsonSyntaxException("Expected " + memberName + " to be a string, was " + toString(json));
        }
    }

    public static String toString(JsonElement json) {
        String s = StringUtils.abbreviateMiddle(String.valueOf(json), "...", 10);
        if (json == null) {
            return "null (missing)";
        } else if (json.isJsonNull()) {
            return "null (json)";
        } else if (json.isJsonArray()) {
            return "an array (" + s + ")";
        } else if (json.isJsonObject()) {
            return "an object (" + s + ")";
        } else {
            if (json.isJsonPrimitive()) {
                JsonPrimitive jsonprimitive = json.getAsJsonPrimitive();
                if (jsonprimitive.isNumber()) {
                    return "a number (" + s + ")";
                }

                if (jsonprimitive.isBoolean()) {
                    return "a boolean (" + s + ")";
                }
            }

            return s;
        }
    }

    public String translate(String translateKey, Object... parameters) {
        return this.formatMessage(translateKey, parameters);
    }

    private void loadLang(String lang) {
        String s = String.format("/assets/mc2discord/m2d-lang/core/%s.json", lang);
        try {
            InputStream inputStream = LangManager.class.getResourceAsStream(s);
            this.loadLocaleData(inputStream);
        } catch (Exception exception) {
            Mc2Discord.LOGGER.warn("Skipped Minecraft2Discord language file: {} ({})", s, exception.toString());
        }
    }

    private void loadLocaleData(InputStream inputStreamIn) {
        try {
            JsonElement jsonelement = GSON.fromJson(new InputStreamReader(inputStreamIn, StandardCharsets.UTF_8), JsonElement.class);
            JsonObject jsonobject = getJsonObject(jsonelement, "strings");

            for (Map.Entry<String, JsonElement> entry : jsonobject.entrySet()) {
                String s = FLOAT_REPLACE_PATTERN.matcher(getString(entry.getValue(), entry.getKey())).replaceAll("%$1s");
                this.properties.put(entry.getKey(), s);
            }
        } finally {
            IOUtils.closeQuietly(inputStreamIn);
        }
    }

    private String translateKeyPrivate(String translateKey) {
        String s = this.properties.get(translateKey);
        return s == null ? translateKey : s;
    }

    public String formatMessage(String translateKey, Object... parameters) {
        String s = this.translateKeyPrivate(translateKey);

        try {
            return String.format(s, parameters);
        } catch (IllegalFormatException var5) {
            return "Format error: " + s;
        }
    }
}
