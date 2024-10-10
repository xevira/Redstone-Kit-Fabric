package github.xevira.redstone_kit.poi;

import com.google.gson.JsonObject;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.config.ServerConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class POILoader {

    public static void onServerWorldLoad(MinecraftServer server, ServerWorld world)
    {
        Path path = DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT)).resolve(RedstoneKit.MOD_ID + "/poi.json");

        try {
            if (Files.notExists(path)) {
                return;
            }

            String jsonStr = Files.readString(path);
            JsonObject json = RedstoneKit.GSON.fromJson(jsonStr, JsonObject.class);

            deserialize(json, server, world);
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to read POI file!", exception);
        }
    }

    public static void onServerWorldSave(MinecraftServer server, ServerWorld world)
    {
        Path path = DimensionType.getSaveDirectory(world.getRegistryKey(), server.getSavePath(WorldSavePath.ROOT)).resolve(RedstoneKit.MOD_ID + "/poi.json");

        try {
            Files.createDirectories(path.getParent());
            JsonObject json = serialize(world);
            Files.writeString(path, RedstoneKit.GSON.toJson(json));
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to write POI file!", exception);
        }
    }

    private static void deserialize(JsonObject json, MinecraftServer server, ServerWorld world)
    {
        if (!TeleportInhibitors.readJSON(json, server, world))
        {
            RedstoneKit.LOGGER.error("Failed to load Teleportors POI data for world {}.", world.getRegistryKey().getValue());
        }
    }

    private static JsonObject serialize(ServerWorld world)
    {
        JsonObject json = new JsonObject();

        TeleportInhibitors.writeJSON(json, world);

        return json;
    }
}
