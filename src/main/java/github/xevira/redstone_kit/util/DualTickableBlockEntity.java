package github.xevira.redstone_kit.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;

public interface DualTickableBlockEntity {

    void clientTick();

    void serverTick();

    static <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pWorld) {
        return pWorld.isClient ? (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof DualTickableBlockEntity tickableBlockEntity) {
                tickableBlockEntity.clientTick();
            }
        } : (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof DualTickableBlockEntity tickableBlockEntity) {
                tickableBlockEntity.serverTick();
            }
        };
    }
}