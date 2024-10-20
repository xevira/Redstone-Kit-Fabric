package github.xevira.redstone_kit.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBeaconBlockEntityMixin {
    void internalTick(World _world, BlockPos _pos, BlockState _state);
}
