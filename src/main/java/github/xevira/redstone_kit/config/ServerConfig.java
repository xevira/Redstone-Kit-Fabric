package github.xevira.redstone_kit.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ServerConfig {
    private static final String CONFIG_PATH = "config/" + RedstoneKit.MOD_ID + ".json";
    private static final String BACKUP_PATH = "config/" + RedstoneKit.MOD_ID + ".json.bak";

    private static final String USE_XP_TO_LOCK_FIELD = "useXPtoLock";
    private static final String XP_LOCK_LEVELS_FIELD = "xpLockLevels";

    private static ServerConfig currentConfig;

    private boolean useXPtoLock;
    private int xpLockLevels;

    public ServerConfig()
    {
        this.useXPtoLock = false;
        this.xpLockLevels = 1;
    }

    public static void onServerLoad(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static void onServerSave(MinecraftServer server) {
        writeConfig(currentConfig, server);
    }

    public static void onReload(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static ServerConfig getConfig() {
        return currentConfig;
    }

    private static ServerConfig readConfig(MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        try {
            var config = new ServerConfig();
            if (Files.notExists(configPath)) {
                writeConfig(config, server);
                return config;
            }

            String jsonStr = Files.readString(configPath);
            JsonObject json = RedstoneKit.GSON.fromJson(jsonStr, JsonObject.class);
            config.deserialize(json);
            return config;
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to read config file!", exception);

            // make a backup of the config file
            backupConfig(server);

            var config = new ServerConfig();
            writeConfig(config, server);
            return config;
        }
    }

    private static void writeConfig(ServerConfig config, MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        try {
            Files.createDirectories(configPath.getParent());
            JsonObject json = config.serialize();
            Files.writeString(configPath, RedstoneKit.GSON.toJson(json));
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to write config file!", exception);
        }
    }

    private static void backupConfig(MinecraftServer server) {
        Path configPath = server.getPath(CONFIG_PATH);
        if (Files.notExists(configPath))
            return;

        Path backupPath = server.getPath(BACKUP_PATH);
        try {
            Files.createDirectories(backupPath.getParent());
            Files.move(configPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to backup config file!", exception);
        }
    }

    private JsonObject serialize() {
        JsonObject json = new JsonObject();

        json.add(USE_XP_TO_LOCK_FIELD, new JsonPrimitive(this.useXPtoLock));
        json.add(XP_LOCK_LEVELS_FIELD, new JsonPrimitive(this.xpLockLevels));

        return json;
    }

    private void deserialize(JsonObject json) {
        this.useXPtoLock = json.getAsJsonPrimitive(USE_XP_TO_LOCK_FIELD).getAsBoolean();
        this.xpLockLevels = json.getAsJsonPrimitive(XP_LOCK_LEVELS_FIELD).getAsInt();
    }


    // Getters
    public boolean useXPtoLock() { return this.useXPtoLock; }
    public int xpLockLevels() { return this.xpLockLevels; }

}
