package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.util.RedstoneConnect;
import github.xevira.redstone_kit.util.RedstoneConnectEnum;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BouncyPadBlock extends Block {
    public static final MapCodec<BouncyPadBlock> CODEC = createCodec(BouncyPadBlock::new);

    public static final VoxelShape SHAPE = VoxelShapes.union(
            Block.createCuboidShape(0,0,0, 16, 2, 16),
            Block.createCuboidShape(2,2,2, 14, 3, 14)
    ).simplify();

    public static final VoxelShape SHAPE_EXTENDED = VoxelShapes.union(
            Block.createCuboidShape(2,0,2,14,1,14),
            Block.createCuboidShape(0,0,0,14,2,2),
            Block.createCuboidShape(14,0,0,16,2,14),
            Block.createCuboidShape(2,0,14,16,2,16),
            Block.createCuboidShape(0,0,2,2,2,16),
            Block.createCuboidShape(7,1,7,9,13,9),
            Block.createCuboidShape(2,13,2,14,16,14)
    ).simplify();

//    public static final BooleanProperty EXTENDED = Properties.EXTENDED;
//    public static final BooleanProperty AUTOMATIC = BooleanProperty.of("automatic");

    public BouncyPadBlock(Settings settings) {
        super(settings);
//        this.setDefaultState(this.getStateManager().getDefaultState().with(EXTENDED, false));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return state.get(EXTENDED) ? SHAPE_EXTENDED : SHAPE;
        return SHAPE;
    }


//    @Override
//    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
//        if (state.get(AUTOMATIC)) {
//            if (state.get(EXTENDED))
//                world.setBlockState(pos, state.with(EXTENDED, false), Block.NOTIFY_LISTENERS);
//        }
//        else {
//            // TODO: Check redstone power coming in
//        }
//    }
    private boolean isOnPad(BlockPos pos, Entity entity)
    {
        if (((entity.getX() < (pos.getX() + 0.125f)) || (entity.getX() > (pos.getX() + 0.875f)))) return false;
        if (((entity.getZ() < (pos.getZ() + 0.125f)) || (entity.getZ() > (pos.getZ() + 0.875f)))) return false;

        return (entity.getY() <= (pos.getY() + 0.1875f + 1.0E-7));
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        //RedstoneKit.LOGGER.info("onEntityCollision: {}", world.isClient ? "CLIENT" : "SERVER");
        if (!world.isClient)
        {
            if (isOnPad(pos, entity))
            {
                entity.move(MovementType.SELF, new Vec3d(0,10,0));
                RedstoneKit.LOGGER.info("onEntityCollision: on pad");
            }
        }
    }


//    @Override
//    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
//        builder.add(EXTENDED, AUTOMATIC);
//    }
}
