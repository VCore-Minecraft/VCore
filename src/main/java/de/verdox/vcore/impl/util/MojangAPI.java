package de.verdox.vcore.impl.util;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MojangAPI {

    public static String getName(UUID playerUUID) {
        // Hier die UUID des Spielers einsetzen
        try {
            // Verbindung zur Mojang API herstellen
            URL url = new URL("https://api.mojang.com/user/profiles/" + playerUUID
                    .toString()
                    .replace("-", "") + "/names");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Antwort der API auslesen
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Antwort in ein JSON-Objekt umwandeln
            JsonObject jsonObject = JsonParser.parseString(response.toString()).getAsJsonObject();

            // Den Namen aus dem JSON-Objekt auslesen
            return jsonObject.get("name").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static CompletableFuture<String> getNameAsync(UUID playerUUID) {
        var future = new CompletableFuture<String>();
        new Thread(() -> future.complete(getName(playerUUID))).start();
        return future;
    }

}
