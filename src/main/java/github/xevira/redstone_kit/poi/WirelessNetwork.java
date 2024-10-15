package github.xevira.redstone_kit.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.redstone_kit.RedstoneKit;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.apache.http.impl.conn.Wire;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class WirelessNetwork {

    private static final Map<Integer, WirelessChannelData> CHANNELS = new HashMap<>();

    private static String decodeChannel(int channel)
    {
        DyeColor a = DyeColor.byId((channel >> 12) & 15);
        DyeColor b = DyeColor.byId((channel >> 8) & 15);
        DyeColor c = DyeColor.byId((channel >> 4) & 15);
        DyeColor d = DyeColor.byId((channel) & 15);

        return a + "," + b + "," + c + "," + d;
    }

    public static boolean registerChannel(int channel, @NotNull World world, @NotNull BlockPos pos, int initialValue)
    {
        RedstoneKit.LOGGER.info("Registering channel {}", decodeChannel(channel));

        if (hasChannel(channel)) return false;

        WirelessChannelData data = new WirelessChannelData(initialValue, world, pos);
        CHANNELS.put(channel, data);
        return true;
    }


    public static boolean hasChannel(int channel)
    {
        return CHANNELS.containsKey(channel);
    }

    public static @Nullable WirelessChannelData getChannel(int channel)
    {
        if (CHANNELS.containsKey(channel))
            return CHANNELS.get(channel);

        return null;
    }

    public static boolean setChannelValue(int channel, int value)
    {
        WirelessChannelData data = getChannel(channel);
        if (data == null) return false;

        data.setSignal(value);
        return true;
    }


    public static int getChannelValue(int channel)
    {
        WirelessChannelData data = getChannel(channel);

        return (data != null) ? data.getSignal() : -1;
    }

    /**
     * Removes a channel from the wireless network.  Called by transmitters when broken.
     */
    public static void unregisterChannel(int channel)
    {
        RedstoneKit.LOGGER.info("Unregistering channel {}", decodeChannel(channel));
        CHANNELS.remove(channel);
    }

    private static Path getSaveFile(MinecraftServer server)
    {
        return DimensionType.getSaveDirectory(server.getOverworld().getRegistryKey(), server.getSavePath(WorldSavePath.ROOT)).resolve(RedstoneKit.MOD_ID + "/wireless.json");
    }

    public static void onServerStarted(MinecraftServer server)
    {
        Path path = getSaveFile(server);

        try {
            if (Files.notExists(path)) {
                return;
            }

            String jsonStr = Files.readString(path);
            JsonArray json = RedstoneKit.GSON.fromJson(jsonStr, JsonArray.class);

            deserialize(json, server);
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to read Wireless Network file!", exception);
        }
    }

    public static void onServerStopped(MinecraftServer server)
    {
        Path path = getSaveFile(server);

        try {
            Files.createDirectories(path.getParent());
            JsonElement json = serialize();
            Files.writeString(path, RedstoneKit.GSON.toJson(json));
        } catch (IOException exception) {
            RedstoneKit.LOGGER.error("Failed to write Wireless Network file!", exception);
        }
    }

    public static void onAfterSave(MinecraftServer server, boolean flush, boolean force)
    {
        onServerStopped(server);
    }

    private static int getInt(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isNumber())
                {
                    return p.getAsInt();
                }
            }
        }

        return 0;
    }

    private static String getString(JsonObject json, String key)
    {
        if (json.has(key))
        {
            JsonElement e = json.get(key);
            if (e.isJsonPrimitive())
            {
                JsonPrimitive p = e.getAsJsonPrimitive();

                if (p.isString())
                {
                    return p.getAsString();
                }
            }
        }

        return "";
    }

    private static void deserialize(JsonArray array, MinecraftServer server)
    {
        CHANNELS.clear();

        for(JsonElement e : array)
        {
            if (e.isJsonObject())
            {
                JsonObject o = e.getAsJsonObject();

                int channel = getInt(o, "channel");
                JsonObject jsonData = o.getAsJsonObject("data");

                WirelessChannelData  data = WirelessChannelData.readNbt(jsonData, server);

                CHANNELS.put(channel, data);
            }
        }
    }

    private static JsonElement serialize()
    {
        JsonArray array = new JsonArray();

        CHANNELS.forEach((c, d) -> {
            JsonObject channel = new JsonObject();
            channel.add("channel", new JsonPrimitive(c));
            channel.add("data", d.writeJson());
            array.add(channel);
        });

        return array;
    }

    public static int getChannelByDyeColors(DyeColor a, DyeColor b, DyeColor c, DyeColor d)
    {
        return (a.getId() << 12) | (b.getId() << 8) | (c.getId() << 4) | (d.getId());
    }

    public static class WirelessChannelData
    {
        private int signal;
        private final World world;
        private final BlockPos pos;

        public WirelessChannelData(int signal, World world, BlockPos pos)
        {
            this.signal = signal;
            this.world = world;
            this.pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        }

        public WirelessChannelData(int signal, World world, int x, int y, int z)
        {
            this.signal = signal;
            this.world = world;
            this.pos = new BlockPos(x, y, z);
        }

        public int getSignal()
        {
            return this.signal;
        }

        public World getWorld()
        {
            return this.world;
        }

        public BlockPos getPos()
        {
            return this.pos;
        }

        public int getX()
        {
            return this.pos.getX();
        }

        public int getY()
        {
            return this.pos.getY();
        }

        public int getZ()
        {
            return this.pos.getZ();
        }

        public void setSignal(int value)
        {
            this.signal = value;
        }

        public JsonObject writeJson()
        {
            JsonObject json = new JsonObject();

            json.add("signal", new JsonPrimitive(signal));
            json.add("world", new JsonPrimitive(world.getRegistryKey().getValue().toString()));
            json.add("x", new JsonPrimitive(pos.getX()));
            json.add("y", new JsonPrimitive(pos.getY()));
            json.add("z", new JsonPrimitive(pos.getZ()));

            return json;
        }

        public static WirelessChannelData readNbt(JsonObject json, MinecraftServer server)
        {
            int signal = WirelessNetwork.getInt(json, "signal");
            String worldIdStr = WirelessNetwork.getString(json, "world");
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, Identifier.of(worldIdStr));
            World world = server.getWorld(key);
            int x = WirelessNetwork.getInt(json, "x");
            int y = WirelessNetwork.getInt(json, "y");
            int z = WirelessNetwork.getInt(json, "z");

            return new WirelessChannelData(signal, world, x, y, z);
        }
    }
}
