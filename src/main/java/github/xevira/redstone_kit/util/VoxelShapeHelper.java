package github.xevira.redstone_kit.util;

import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class VoxelShapeHelper {
    public static VoxelShape scaleAndSimplify(VoxelShape shape, float factor)
    {
        VoxelShape[] voxelShapes = new VoxelShape[]{VoxelShapes.empty()};
        shape.forEachBox(
                (minX, minY, minZ, maxX, maxY, maxZ) -> voxelShapes[0] = VoxelShapes.combine(
                        voxelShapes[0], VoxelShapes.cuboid(minX * factor, minY * factor, minZ * factor, maxX * factor, maxY * factor, maxZ * factor), BooleanBiFunction.OR
                )
        );
        return voxelShapes[0];    }
}
