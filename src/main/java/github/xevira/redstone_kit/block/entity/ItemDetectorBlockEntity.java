package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.ItemDetectorBlock;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class ItemDetectorBlockEntity extends BlockEntity implements ServerTickableBlockEntity {
    public static final int MAX_ITEMS = 64;
    private int powerLevel;

    public ItemDetectorBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.ITEM_DETECTOR_BLOCK_ENTITY, pos, state);

        this.powerLevel = 0;
    }


    private int getItemCount(@NotNull World world, @NotNull Box box)
    {
        List<ItemEntity> list = world.getEntitiesByClass(ItemEntity.class, box, Objects::nonNull);

        return list.stream().mapToInt(entity -> entity.getStack().getCount()).sum();
    }


    @Override
    public void serverTick() {
        if (this.world == null || this.world.isClient) return;

        BlockState state = this.world.getBlockState(this.pos);
        Direction facing = state.get(ItemDetectorBlock.FACING);
        Box box = new Box(this.pos.offset(facing));

        int count = getItemCount(this.world, box);

//        if ((this.world.getTime() & 20) == 0)
//        {
//            RedstoneKit.LOGGER.info("ItemDetector: {}, {}, {}", facing, box, count);
//        }

        int w = Math.min(count, MAX_ITEMS);
        int power;
        if (w > 0) {
            float f = (float)w / MAX_ITEMS;
            power = MathHelper.ceil(f * 15.0f);
        }
        else
            power = 0;

        if (this.powerLevel != power) {
            this.powerLevel = power;
            this.world.setBlockState(this.pos, state.with(ItemDetectorBlock.POWER, power), Block.NOTIFY_ALL);
            markDirty();
        }
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null)
            this.world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

}
