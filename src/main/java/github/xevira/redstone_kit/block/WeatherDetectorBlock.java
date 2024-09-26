package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.block.entity.WeatherDetectorBlockEntity;
import github.xevira.redstone_kit.Registration;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

// Detects the current weather
// 0 = no sky access or no weather
// 1 = clear
// 2 = raining/snowing
// 3 = thundering/blizzard
public class WeatherDetectorBlock extends BlockWithEntity {
    public static final MapCodec<WeatherDetectorBlock> CODEC = createCodec(WeatherDetectorBlock::new);
    public static final IntProperty POWER;
    protected static final VoxelShape SHAPE;

    // Power outputs
    public static final int NO_SKY = 0;         // Has no sky access or no weather
    public static final int CLEAR = 1;          // Weather is currently clear
    public static final int RAINING = 2;        // Weather is currently raining
    public static final int THUNDERING = 3;     // Weather is currently thundering

    public WeatherDetectorBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, NO_SKY));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() { return CODEC; }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.WEATHER_DETECTOR_BLOCK_ENTITY.instantiate(pos, state);
    }

    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.get(POWER);
    }

    private static void updateState(BlockState state, World world, BlockPos pos) {
        int p;
        if (world.isSkyVisible(pos)) {
            p = CLEAR;
            if (world.isThundering())
                p = THUNDERING;
            else if (world.isRaining())
                p = RAINING;
        }
        else
            p = NO_SKY;

        if (p != state.get(POWER))
        {
            world.setBlockState(pos, state.with(POWER, p));
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return !world.isClient && world.getDimension().hasSkyLight() ? validateTicker(type, Registration.WEATHER_DETECTOR_BLOCK_ENTITY, WeatherDetectorBlock::tick) : null;
    }

    private static void tick(World world, BlockPos pos, BlockState state, WeatherDetectorBlockEntity blockEntity) {
        if (world.getTime() % 20L == 0L) {
            updateState(state, world, pos);
        }
    }

    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }

    static {
        POWER = Properties.POWER;

        // Same shape as the daylight detector
        SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);
    }
}
