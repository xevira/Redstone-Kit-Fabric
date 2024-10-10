package github.xevira.redstone_kit.util;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.world.World;

public interface ClientTickableBlockEntity {
    void clientTick();

    static <T extends BlockEntity> BlockEntityTicker<T> getTicker(World pWorld) {
        return pWorld.isClient ? (world, pos, state, blockEntity) -> {
            if (blockEntity instanceof ClientTickableBlockEntity tickableBlockEntity) {
                tickableBlockEntity.clientTick();
            }
        } : null;
    }
}