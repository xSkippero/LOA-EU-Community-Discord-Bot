package de.Skippero.LOA.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import java.io.*;

public class ConfigManager {

    private final Gson gson;
    private final JsonParser parser;
    private JsonObject jsonObject;

    public ConfigManager() {
        gson = new Gson();
        parser = new JsonParser();
        jsonObject = new JsonObject();
        createDefaultConfig();
        loadConfig();
    }

    public String getData(String property) {
        return jsonObject.get(property).getAsString();
    }

    private void createDefaultConfig() {
        File dataFile = new File("config.json");
        if (!dataFile.exists()) {
            setData("mysql.host", "localhost");
            setData("mysql.port", "3306");
            setData("mysql.database", "loabot");
            setData("mysql.user", "loa");
            setData("mysql.password", "password");
        }
    }

    private void setData(String path, String data) {
        jsonObject.add(path, new JsonPrimitive(data));
        saveConfig();
    }

    private void saveConfig() {
        String jsonString = gson.toJson(jsonObject);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("config.json"));
            writer.write(jsonString);
            writer.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void loadConfig() {
        File dataFile = new File("config.json");
        StringBuilder jsonString = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(dataFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        if (jsonString.toString().length() < 2) {
            jsonString = new StringBuilder("{}");
        }
        jsonObject = parser.parse(jsonString.toString()).getAsJsonObject();
    }

}
