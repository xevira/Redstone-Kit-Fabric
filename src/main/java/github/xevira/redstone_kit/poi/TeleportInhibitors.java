package github.xevira.redstone_kit.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.entity.TeleportInhibitorBlockEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.pattern.AbstractStyleNameConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleportInhibitors {
    public static final double RANGE = 64.0;
    public static final double RANGE_SQ = RANGE * RANGE;

    public static final String JSON_TAG = "teleport inhibitors";

    private static final Map<World, List<BlockPos>> POI = new HashMap<>();

    public static boolean add(World world, BlockPos pos)
    {
        if (!POI.containsKey(world))
        {
            POI.put(world, List.of(pos));   // First one for that world
            return true;
        }

        List<BlockPos> poiPos = POI.get(world);

        if (poiPos.contains(pos)) return false;

        RedstoneKit.LOGGER.info("Adding {} at {},{},{} to Inhibitor POI.", world.getRegistryKey().getValue(), pos.getX(), pos.getY(), pos.getZ());

        return poiPos.add(pos);
    }

    public static boolean remove(World world, BlockPos pos)
    {
        if (!POI.containsKey(world))
            return false;

        List<BlockPos> poiPos = POI.get(world);

        if (!poiPos.contains(pos)) return false;

        RedstoneKit.LOGGER.info("Removing {} at {},{},{} to Inhibitor POI.", world.getRegistryKey().getValue(), pos.getX(), pos.getY(), pos.getZ());

        return poiPos.remove(pos);
    }

    public static boolean isValidEndermanTeleport(EndermanEntity enderman)
    {
        return isValidEndermanTeleport(enderman.getWorld(), enderman.getX(), enderman.getY(), enderman.getZ());
    }

    public static boolean isValidEndermanTeleport(World world, Vec3d v)
    {
        return isValidEndermanTeleport(world, v.x, v.y, v.z);
    }

    @SuppressWarnings("deprecation")
    public static boolean isValidEndermanTeleport(World world, double x, double y, double z)
    {
        // Shortcut if there's no POIs or for that world
        if (POI.isEmpty() || !POI.containsKey(world)) return true;

        // Copied from the EndermanEntity.teleportTo function to get the *actual* location
        BlockPos.Mutable mutable = new BlockPos.Mutable(x, y, z);

        while (mutable.getY() > world.getBottomY() && !world.getBlockState(mutable).blocksMovement()) {
            mutable.move(Direction.DOWN);
        }

        // Check the list
        List<BlockPos> poiPos = POI.get(world);
        for(BlockPos pos : poiPos)
        {
            if (pos.getSquaredDistance(mutable) <= RANGE_SQ)
            {
                if (world.getBlockEntity(pos) instanceof TeleportInhibitorBlockEntity inhibitor)
                {
                    if (inhibitor.useCharge())
                        return false;
                }
            }
        }
        return true;
    }

    public static void writeJSON(JsonObject json, ServerWorld world)
    {
        // Format:
        // "teleport inhibitors" : [  - All inhibitors in the specified world
        //   ...
        //   {
        //     "x": <x coordinate>,
        //     "y": <y coordinate>,
        //     "z": <z coordinate>,
        //   }
        //   ...
        // ]


        if (!POI.containsKey(world))
            return;

        JsonArray posArray = new JsonArray();

        for(BlockPos pos : POI.get(world))
        {
            JsonObject jsonPos = new JsonObject();
            jsonPos.add("x", new JsonPrimitive(pos.getX()));
            jsonPos.add("y", new JsonPrimitive(pos.getY()));
            jsonPos.add("z", new JsonPrimitive(pos.getZ()));

            posArray.add(jsonPos);
        }

        json.add(JSON_TAG, posArray);
    }

    private static boolean notNumber(JsonElement e) {
        if (!e.isJsonPrimitive()) return true;

        JsonPrimitive p = e.getAsJsonPrimitive();

        return !p.isNumber();
    }

    public static void init()
    {
        POI.clear();
    }

    public static boolean readJSON(JsonObject json, MinecraftServer server, ServerWorld world)
    {
        if (json.has(JSON_TAG)) {
            JsonElement element = json.get(JSON_TAG);
            if (element.isJsonArray())
            {
                JsonArray posArray = element.getAsJsonArray();
                List<BlockPos> poiPos = new ArrayList<>();
                String w = world.getRegistryKey().getValue().toString();

                for(JsonElement e : posArray)
                {
                    if (e.isJsonObject())
                    {
                        JsonObject o = e.getAsJsonObject();

                        if (!o.has("x"))
                        {
                            RedstoneKit.LOGGER.error("Missing 'x' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        JsonElement x = o.get("x");
                        if (notNumber(x))
                        {
                            RedstoneKit.LOGGER.error("Invalid 'x' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        if (!o.has("y"))
                        {
                            RedstoneKit.LOGGER.error("Missing 'y' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        JsonElement y = o.get("y");
                        if (notNumber(y))
                        {
                            RedstoneKit.LOGGER.error("Invalid 'y' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        if (!o.has("z"))
                        {
                            RedstoneKit.LOGGER.error("Missing 'z' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        JsonElement z = o.get("z");
                        if (notNumber(z))
                        {
                            RedstoneKit.LOGGER.error("Invalid 'z' coordinate in '{}' list in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        BlockPos pos = new BlockPos(x.getAsInt(), y.getAsInt(), z.getAsInt());

                        if (poiPos.contains(pos))
                        {
                            RedstoneKit.LOGGER.warn("Duplicate position ({}, {}, {}) encountered in '{}' list in {} POI.  Ignoring.", pos.getX(), pos.getY(), pos.getZ(), w, JSON_TAG);
                            continue;
                        }

                        poiPos.add(pos);
                    }
                    else
                        RedstoneKit.LOGGER.error("Invalid element in '{}' array in {} POI.", w, JSON_TAG);
                }

                POI.put(world, poiPos);
                return true;
            }

            return false;
        }

        // This is when there are no POIs saved for this world.
        return true;
    }
}
