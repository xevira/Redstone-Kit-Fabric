package github.xevira.redstone_kit.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.redstone_kit.RedstoneKit;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ServerConfig {
    private static final String CONFIG_PATH = "config/" + RedstoneKit.MOD_ID + ".json";
    private static final String BACKUP_PATH = "config/" + RedstoneKit.MOD_ID + ".json.bak";

    private static final String USE_XP_TO_LOCK_FIELD = "useXPtoLock";
    private static final String XP_LOCK_LEVELS_FIELD = "xpLockLevels";
    private static final String INTERDIMENSIONAL_PEARL_COST_FIELD = "interdimensionalPearlCost";
    private static final String INTERDIMENSIONAL_XP_COST_FIELD = "interdimensionalXPCost";
    private static final String INHIBITOR_COST_FIELD = "inhibitorCost";

    private static ServerConfig currentConfig;

    private boolean useXPtoLock;
    private int xpLockLevels;
    private long inhibitorCost;
    private int interdimensionalPearlCost;
    private int interdimensionalXPCost;

    public ServerConfig()
    {
        this.useXPtoLock = false;
        this.xpLockLevels = 1;
        this.interdimensionalPearlCost = 250;
        this.interdimensionalXPCost = 250;
        this.inhibitorCost = FluidConstants.NUGGET;
    }

    public static void onServerLoad(MinecraftServer server) {
        currentConfig = readConfig(server);
    }

    public static void onServerSave(MinecraftServer server) {
        if (currentConfig == null)
            currentConfig = new ServerConfig();

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
        json.add(INTERDIMENSIONAL_PEARL_COST_FIELD, new JsonPrimitive(this.interdimensionalPearlCost));
        json.add(INTERDIMENSIONAL_XP_COST_FIELD, new JsonPrimitive(this.interdimensionalXPCost));
        json.add(INHIBITOR_COST_FIELD, new JsonPrimitive(this.inhibitorCost));

        return json;
    }

    private boolean isJsonPrimitive(JsonObject json, String key)
    {
        if (!json.has(key)) return false;

        JsonElement element = json.get(key);
        return element.isJsonPrimitive();
    }

    private void deserialize(JsonObject json) {
        if (isJsonPrimitive(json, USE_XP_TO_LOCK_FIELD))
            this.useXPtoLock = json.getAsJsonPrimitive(USE_XP_TO_LOCK_FIELD).getAsBoolean();

        if (isJsonPrimitive(json, XP_LOCK_LEVELS_FIELD))
            this.xpLockLevels = json.getAsJsonPrimitive(XP_LOCK_LEVELS_FIELD).getAsInt();

        if (isJsonPrimitive(json, INTERDIMENSIONAL_PEARL_COST_FIELD))
            this.interdimensionalPearlCost = json.getAsJsonPrimitive(INTERDIMENSIONAL_PEARL_COST_FIELD).getAsInt();

        if (isJsonPrimitive(json, INTERDIMENSIONAL_XP_COST_FIELD))
            this.interdimensionalXPCost = json.getAsJsonPrimitive(INTERDIMENSIONAL_XP_COST_FIELD).getAsInt();


        if (isJsonPrimitive(json, INHIBITOR_COST_FIELD))
            this.inhibitorCost = json.getAsJsonPrimitive(INHIBITOR_COST_FIELD).getAsLong();
    }


    // Getters
    public boolean useXPtoLock() { return this.useXPtoLock; }
    public int xpLockLevels() { return this.xpLockLevels; }
    public int getInterdimensionalPearlCost() { return this.interdimensionalPearlCost; }
    public int getInterdimensionalXPCost() { return this.interdimensionalXPCost; }
    public long inhibitorCost() { return this.inhibitorCost; }

}
