package github.xevira.redstone_kit.poi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.util.Boxi;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightDisplayBulbs {
    public static final String JSON_TAG = "light display bulbs";

    private static final Map<World, List<Boxi>> POI = new HashMap<>();

    public static boolean add(World world, Boxi box)
    {
        if (!POI.containsKey(world))
        {
            RedstoneKit.LOGGER.info("Adding {} at {} to LightDisplayBulbs POI.", world.getRegistryKey().getValue(), box);
            POI.put(world, List.of(box));   // First one for that world
            return true;
        }

        List<Boxi> poiBoxes = POI.get(world);

        for(Boxi b : poiBoxes)
            if (b.equals(box) || b.intersects(box)) return false;

        RedstoneKit.LOGGER.info("Adding {} at {} to LightDisplayBulbs POI.", world.getRegistryKey().getValue(), box);

        return poiBoxes.add(box);
    }

    public static boolean remove(World world, Boxi box)
    {
        if (!POI.containsKey(world))
            return false;

        List<Boxi> poiBoxes = POI.get(world);

        if (!poiBoxes.contains(box)) return false;

        RedstoneKit.LOGGER.info("Removing {} at {} to Inhibitor POI.", world.getRegistryKey().getValue(), box);

        return poiBoxes.remove(box);
    }

    public static void writeJSON(JsonObject json, ServerWorld world)
    {
        // Format:
        // "light display bulbs" : [  - All inhibitors in the specified world
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

        JsonArray boxArray = new JsonArray();

        for(Boxi box : POI.get(world))
        {
            JsonArray jsonArray = new JsonArray();
            jsonArray.add(new JsonPrimitive(box.minX));
            jsonArray.add(new JsonPrimitive(box.minY));
            jsonArray.add(new JsonPrimitive(box.minZ));
            jsonArray.add(new JsonPrimitive(box.maxX));
            jsonArray.add(new JsonPrimitive(box.maxY));
            jsonArray.add(new JsonPrimitive(box.maxZ));

            boxArray.add(jsonArray);
        }

        json.add(JSON_TAG, boxArray);
    }

    private static boolean notNumber(JsonElement e) {
        if (!e.isJsonPrimitive()) return true;

        JsonPrimitive p = e.getAsJsonPrimitive();

        return !p.isNumber();
    }

    public static boolean readJSON(JsonObject json, MinecraftServer server, ServerWorld world)
    {
        if (json.has(JSON_TAG)) {
            JsonElement element = json.get(JSON_TAG);
            if (element.isJsonArray()) {
                JsonArray boxArray = element.getAsJsonArray();
                List<Boxi> poiBoxes = new ArrayList<>();
                String w = world.getRegistryKey().getValue().toString();
                for(JsonElement e : boxArray) {
                    if (e.isJsonArray()) {
                        JsonArray a = e.getAsJsonArray();

                        int[] values = new int[6];

                        if (a.size() != values.length)
                        {
                            RedstoneKit.LOGGER.error("Invalid array size in '{}' array in {} POI.", w, JSON_TAG);
                            continue;
                        }

                        boolean valid = true;
                        for(int i = 0; valid && i < values.length; i++)
                        {
                            JsonElement v = a.get(i);
                            if (notNumber(v))
                            {
                                RedstoneKit.LOGGER.error("Invalid coordinate in '{}' list in {} POI.", w, JSON_TAG);
                                valid = false;
                                break;
                            }

                            values[i] = v.getAsJsonPrimitive().getAsInt();
                        }

                        if (valid)
                        {
                            Boxi box = new Boxi(values[0],values[1],values[2],values[3],values[4],values[5]);

                            for(Boxi b : poiBoxes)
                                if (b.equals(box) || b.intersects(box)) { valid = false; break; }

                            if (valid)
                                poiBoxes.add(box);
                            else
                                RedstoneKit.LOGGER.error("Duplicate element in '{}' array in {} POI.", w, JSON_TAG);
                        }

                    } else {
                        RedstoneKit.LOGGER.error("Invalid element in '{}' array in {} POI.", w, JSON_TAG);
                    }
                }

                POI.put(world, poiBoxes);
                return true;
            }

            return false;
        }

        return true;
    }

}
