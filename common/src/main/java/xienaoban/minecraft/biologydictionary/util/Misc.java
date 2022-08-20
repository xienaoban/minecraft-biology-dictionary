package xienaoban.minecraft.biologydictionary.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.injectables.annotations.ExpectPlatform;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import static xienaoban.minecraft.biologydictionary.BiologyDictionary.LOGGER;

public interface Misc {

    @SuppressWarnings("unchecked")
    static <T> T cast(Object obj) {
        return (T) obj;
    }

    @ExpectPlatform
    static Path getConfigPath() {
        throw new AssertionError();
    }

    static BufferedReader getFileReader(Path path) throws IOException {
        return Files.newBufferedReader(path);
    }

    static BufferedWriter getFileWriter(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.newBufferedWriter(path, StandardOpenOption.CREATE);
    }

    static BufferedReader getResourceReader(String path) throws IOException {
        InputStream inputStream = Misc.class.getResourceAsStream(path);
        if (inputStream == null) {
            throw new IOException("Resource not found: " + path);
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        return new BufferedReader(inputStreamReader);
    }

    static String requestMojangApiGetPlayerName(UUID uuid) {
        String uuidString = uuid.toString().replaceAll("-", "");
        final String url = "https://api.mojang.com/user/profiles/" + uuidString + "/names";
        try {
            String res = requestHttpToString(url);
            if (res == null) {
                return Keys.TEXT_NOT_GENUINE_PLAYER;
            }
            JsonArray names = JsonParser.parseString(res).getAsJsonArray();
            JsonObject json = names.get(names.size() - 1).getAsJsonObject();
            return json.get("name").getAsString();
        } catch (Exception e) {
            LOGGER.error("Unable to parse mojang api: " + url);
            e.printStackTrace();
            return Keys.TEXT_FAIL_TO_REQUEST_MOJANG_API;
        }
    }

    private static String requestHttpToString(String url) throws IOException {
        HttpURLConnection con = null;
        try {
            URL uri = new URL(url);
            con = (HttpURLConnection) uri.openConnection();
            con.setReadTimeout(1000 * 6);
            int code = con.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                if (code != HttpURLConnection.HTTP_NO_CONTENT) {
                    LOGGER.error("Bad response code [" + code + "]: " + url);
                }
                return null;
            }
            try (InputStreamReader input = new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(input)) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = reader.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        }
        finally {
            if (con != null) {
                con.disconnect();
            }
        }
    }
}
