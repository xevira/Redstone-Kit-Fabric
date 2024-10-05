package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.Registration;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.jetbrains.annotations.Nullable;

public class TeleporterBlock extends BlockWithEntity {
    protected static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0000, 0.0000, 0.0000, 1.0000, 0.0625, 1.0000),

            VoxelShapes.cuboid(0.0625, 0.0625, 0.0625, 0.8750, 0.1250, 0.1250),
            VoxelShapes.cuboid(0.0625, 0.0625, 0.1250, 0.1250, 0.1250, 0.9375),
            VoxelShapes.cuboid(0.1250, 0.0625, 0.8750, 0.9375, 0.1250, 0.9375),
            VoxelShapes.cuboid(0.8750, 0.0625, 0.0625, 0.9375, 0.1250, 0.8750),

            VoxelShapes.cuboid(0.1250, 0.0625, 0.1250, 0.2500, 0.1875, 0.7500),
            VoxelShapes.cuboid(0.1250, 0.0625, 0.7500, 0.7500, 0.1875, 0.8750),
            VoxelShapes.cuboid(0.7500, 0.0625, 0.2500, 0.8750, 0.1875, 0.8750),
            VoxelShapes.cuboid(0.2500, 0.0625, 0.1250, 0.8750, 0.1875, 0.2500)
    ).simplify();

    public static final MapCodec<TeleporterBlock> CODEC = createCodec(TeleporterBlock::new);


    public TeleporterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return  BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.TELEPORTER_BLOCK_ENTITY.instantiate(pos, state);
    }
}
