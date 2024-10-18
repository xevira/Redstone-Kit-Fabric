package github.xevira.redstone_kit.mixin;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.util.ComparatorLike;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("deprecation")
@Mixin(World.class)
public class WorldMixin
{
    @Inject(method = "updateComparators(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;)V", at = @At("TAIL"))
    void updateComparatorsMixin(BlockPos pos, Block block, CallbackInfo cb) {
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            if (((World)(Object)this).isChunkLoaded(blockPos)) {
                BlockState blockState = ((World)(Object)this).getBlockState(blockPos);
                if (blockState.getBlock() instanceof ComparatorLike) {
                    ((World)(Object)this).updateNeighbor(blockState, blockPos, block, pos, false);
                } else if (blockState.isSolidBlock((World)(Object)this, blockPos)) {
                    blockPos = blockPos.offset(direction);
                    blockState = ((World)(Object)this).getBlockState(blockPos);
                    if (blockState.getBlock() instanceof ComparatorLike) {
                        ((World)(Object)this).updateNeighbor(blockState, blockPos, block, pos, false);
                    }
                }
            }
        }
    }
}
