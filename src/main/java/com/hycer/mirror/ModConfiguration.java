package com.hycer.mirror;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfiguration {

    private int maxBackupFiles;
    private boolean autoBackup;
    private String autoBackupTime;

    private static final String CONFIG_DIR = Constants.CONFIG_PATH;
    private static final String CONFIG_FILE_PATH = CONFIG_DIR + Constants.CONFIG_FILE;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    ModConfiguration(){
        checkAndLoadConfig();
    }

    public void checkAndLoadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists()) {
            createDefaultConfig(configFile);
        } else {
            loadConfig(configFile);
        }
    }

    private void createDefaultConfig(File configFile) {
        JsonObject configObject = new JsonObject();
        configObject.addProperty("maxBackupFiles", 5);
        configObject.addProperty("autoBackup", false);
        configObject.addProperty("autoBackupTime", "1:00");

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(configObject, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadConfig(File configFile) {
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject configObject = JsonParser.parseReader(reader).getAsJsonObject();

            maxBackupFiles = configObject.get("maxBackupFiles").getAsInt();
            autoBackup = configObject.get("autoBackup").getAsBoolean();
            autoBackupTime = configObject.get("autoBackupTime").getAsString();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getMaxBackupFiles() {
        return maxBackupFiles;
    }

    public boolean isAutoBackup() {
        return autoBackup;
    }

    public String getAutoBackupTime() {
        return autoBackupTime;
    }

    public void setMaxBackupFiles(int maxBackupFiles) {
        this.maxBackupFiles = maxBackupFiles;
    }

    public void setAutoBackup(boolean autoBackup) {
        this.autoBackup = autoBackup;
    }

    public void setAutoBackupTime(String autoBackupTime) {
        this.autoBackupTime = autoBackupTime;
    }
}
