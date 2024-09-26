package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class WeatherDetectorBlockEntity extends BlockEntity {
    public WeatherDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.WEATHER_DETECTOR_BLOCK_ENTITY, pos, state);
    }
}
