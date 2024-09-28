package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;

public class RedstoneTickerBlockEntity extends BlockEntity {
    public RedstoneTickerBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REDSTONE_TICKER_BLOCK_ENTITY, pos, state);
    }
}
