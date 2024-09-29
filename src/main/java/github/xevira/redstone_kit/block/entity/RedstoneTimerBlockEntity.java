package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.RedstoneTickerBlock;
import github.xevira.redstone_kit.block.RedstoneTimerBlock;
import github.xevira.redstone_kit.network.BlockPosPayload;
import github.xevira.redstone_kit.screenhandler.RedstoneTimerScreenHandler;
import github.xevira.redstone_kit.util.BlockProperties;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class RedstoneTimerBlockEntity extends BlockEntity implements ServerTickableBlockEntity, ExtendedScreenHandlerFactory<BlockPosPayload> {
    public static final Text TITLE = Text.translatable("gui." + RedstoneKit.MOD_ID + ".redstone_timer.title");

    // NBT Tag constants
    private static final String REPEATS_NBT = "repeats";
    private static final String REMAINING_NBT = "ticksRemaining";
    private static final String TOTAL_NBT = "ticksTotal";

    private boolean was_powered;
    private boolean repeats;
    private int ticksRemaining;
    private int ticksTotal;

    protected final PropertyDelegate propertyDelegate;

    public RedstoneTimerBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REDSTONE_TIMER_BLOCK_ENTITY, pos, state);

        this.repeats = true;
        this.was_powered = false;
        this.ticksRemaining = 600;
        this.ticksTotal = 600;  // 30 seconds will be the default

        this.propertyDelegate = new PropertyDelegate() {
            @Override
            public int get(int index) {
                return switch(index) {
                    case 0 -> RedstoneTimerBlockEntity.this.ticksRemaining;
                    case 1 -> RedstoneTimerBlockEntity.this.ticksTotal;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch(index) {
                    case 0 -> RedstoneTimerBlockEntity.this.ticksRemaining = value;
                    case 1 -> RedstoneTimerBlockEntity.this.setTicksTotal(value);
                }
            }

            @Override
            public int size() {
                return 2;
            }
        };
    }

    public boolean getRepeats() { return this.repeats; }
    public int getTicksRemaining() { return this.ticksRemaining; }
    public int getTicksTotal() { return this.ticksTotal; }

    public void setRepeats(boolean repeats)
    {
        this.repeats = repeats;

        if (this.repeats && this.ticksRemaining <= 0)
        {
            this.ticksRemaining = this.ticksTotal;
            if (this.world != null && this.ticksTotal > 0) {
                BlockState state = this.world.getBlockState(pos).with(RedstoneTimerBlock.TIMER, RedstoneTimerBlock.MAX_TIMER);
                this.world.setBlockState(this.pos, state, Block.NOTIFY_LISTENERS);
            }
        }
        markDirty();
    }

    public void setTicksTotal(int total)
    {
        this.ticksTotal = total;

        // Only update the rest if it's on repeat mode or the remaining exceeds the new total
        if (this.repeats || this.ticksRemaining > this.ticksTotal)
            this.ticksRemaining = total;

        if (this.world != null && this.ticksTotal > 0) {
            int timer = MathHelper.clamp(RedstoneTimerBlock.MAX_TIMER * this.ticksRemaining / this.ticksTotal, 0, RedstoneTimerBlock.MAX_TIMER);
            BlockState state = this.world.getBlockState(pos).with(RedstoneTimerBlock.TIMER, timer);
            this.world.setBlockState(this.pos, state, Block.NOTIFY_LISTENERS);
        }

        markDirty();
    }

    @Override
    // Server Side only: !world.isClient
    public void tick() {
        if(this.world == null || this.ticksTotal <= 0) return;

        BlockState state = this.world.getBlockState(pos);
        boolean powered = state.get(RedstoneTimerBlock.POWERED);
        int old_timer = state.get(RedstoneTimerBlock.TIMER);

        int timer;
        boolean lit = false;

        int old_second = this.ticksRemaining / 20;

        if (powered) {
            // Getting any redstone signal will disable the timer, and reset all progress made
            this.ticksRemaining = this.ticksTotal;
            timer = RedstoneTimerBlock.MAX_TIMER;
        } else if (this.ticksRemaining > 0) {
            --this.ticksRemaining;
            if (this.ticksRemaining <= 0)
            {
                lit = true;
                if (this.repeats)
                    this.ticksRemaining = this.ticksTotal;
            }

            timer = MathHelper.clamp(RedstoneTimerBlock.MAX_TIMER * this.ticksRemaining / this.ticksTotal, 0, RedstoneTimerBlock.MAX_TIMER);
        }
        else
            timer = 0;

        int second = this.ticksRemaining / 20;

        boolean dirty = old_second != second || lit;
        if (timer != old_timer || lit) {
            BlockState newState = state.with(RedstoneTimerBlock.TIMER, timer).with(RedstoneTimerBlock.LIT, lit);

            this.world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS);

            if (lit) {
                Direction direction = newState.get(RedstoneTimerBlock.FACING);
                BlockPos blockPos = pos.offset(direction.getOpposite());
                world.updateNeighbor(blockPos, newState.getBlock(), pos);
                world.updateNeighborsExcept(blockPos, newState.getBlock(), direction);

                // To turn the LIT off
                world.scheduleBlockTick(pos, newState.getBlock(), 2);
            }
            dirty = true;
        }

        if (dirty)
            this.markDirty();

        was_powered = powered;
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains(REPEATS_NBT))
            this.repeats = nbt.getBoolean(REPEATS_NBT);

        if (nbt.contains(REMAINING_NBT))
            this.ticksRemaining = nbt.getInt(REMAINING_NBT);

        if (nbt.contains(TOTAL_NBT))
            this.ticksTotal = nbt.getInt(TOTAL_NBT);

        //RedstoneKit.LOGGER.info("Timer.readNbt({}): remaining = {}", this.world != null ? (this.world.isClient?"CLIENT":"SERVER"):"null", this.ticksRemaining);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.putBoolean(REPEATS_NBT, this.repeats);

        if (this.ticksRemaining >= 0)
            nbt.putInt(REMAINING_NBT, this.ticksRemaining);
        if (this.ticksTotal >= 0)
            nbt.putInt(TOTAL_NBT, this.ticksTotal);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        var nbt = super.toInitialChunkDataNbt(registryLookup);
        writeNbt(nbt, registryLookup);
        return nbt;
    }

    @Override
    public @Nullable Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public BlockPosPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new BlockPosPayload(this.pos);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }



    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RedstoneTimerScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }


}
