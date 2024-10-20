package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.RedstoneCounterBlock;
import github.xevira.redstone_kit.network.BlockPosPayload;
import github.xevira.redstone_kit.network.RedstoneCounterPayload;
import github.xevira.redstone_kit.screenhandler.RedstoneCounterScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RedstoneCounterBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory<RedstoneCounterPayload> {
    public static final Text TITLE = Text.translatable(RedstoneKit.textPath("gui", "counter.title"));

    private int counter;
    private int maxCounter;
    private boolean automatic;
    private DyeColor color;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch(index)
            {
                case 0 -> RedstoneCounterBlockEntity.this.automatic ? 1 : 0;
                case 1 -> RedstoneCounterBlockEntity.this.maxCounter;
                case 2 -> RedstoneCounterBlockEntity.this.getInverted() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch(index)
            {
                case 0 -> RedstoneCounterBlockEntity.this.setAutomatic(value != 0);
                case 1 -> RedstoneCounterBlockEntity.this.setMaxCount(value);
                case 2 -> RedstoneCounterBlockEntity.this.setInverted(value != 0);
            }
        }

        @Override
        public int size() {
            return 3;
        }
    };

    public RedstoneCounterBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.REDSTONE_COUNTER_BLOCK_ENTITY, pos, state);

        this.counter = 0;
        this.maxCounter = 15;
        this.automatic = true;
        this.color = DyeColor.LIME;     // Default to lime green
    }

    public void setCounterState(boolean carry)
    {
        if (this.world == null) return;

        BlockState oldState = getCachedState();
        int old_power = oldState.get(RedstoneCounterBlock.POWER);
        boolean had_carry = oldState.get(RedstoneCounterBlock.CARRY);

        int power = 15 * this.counter / this.maxCounter;

        if (power != old_power || carry != had_carry) {
            BlockState state = oldState.with(RedstoneCounterBlock.POWER, power).with(RedstoneCounterBlock.CARRY, carry);
            this.world.setBlockState(this.pos, state, Block.NOTIFY_ALL);
            this.updateTarget(this.world, this.pos, state);
        }
    }

    private void updateNeighbor(World world, BlockPos pos, Direction dir)
    {
        BlockPos blockPos = pos.offset(dir);
        world.updateNeighbor(blockPos, Registration.REDSTONE_COUNTER_BLOCK, pos);
        world.updateNeighborsExcept(blockPos, Registration.REDSTONE_COUNTER_BLOCK, dir);
    }

    protected void updateTarget(World world, BlockPos pos, BlockState state) {
        Direction output = state.get(RedstoneCounterBlock.FACING).getOpposite();
        Direction carry = state.get(RedstoneCounterBlock.INVERTED) ? output.rotateYCounterclockwise() : output.rotateYClockwise();

        updateNeighbor(world, pos, output);
        updateNeighbor(world, pos, carry);
    }

    public int getPowerLevel()
    {
        if (this.maxCounter < 1) return 0;

        return 15 * this.counter / this.maxCounter;
    }

    public boolean incrementCounter()
    {
        assert this.world != null;
        if (this.counter < this.maxCounter) {
            ++this.counter;

            boolean carry;

            if (this.automatic && this.counter >= this.maxCounter)
            {
                this.counter = 0;
                carry = true;

            }
            else
            {
                if (this.counter >= this.maxCounter) {
                    this.counter = this.maxCounter;
                    carry = true;
                }
                else
                    carry = false;
            }

            markDirty();
            return carry;
        }

        return false;
    }

    public void resetCounter()
    {
        this.counter = 0;
        markDirty();
    }

    public int getCount()
    {
        return this.counter;
    }

    public void setMaxCount(int maxCount)
    {
        if (maxCount < 1) return;

        this.maxCounter = maxCount;

        resetCounter();
    }

    public void setAutomatic(boolean value)
    {
        if (this.automatic != value)
        {
            this.automatic = value;

            if (value && this.counter >= this.maxCounter) {
                this.counter = 0;
                setCounterState(false);
            }
            markDirty();
        }
    }

    protected boolean getInverted()
    {
        if (this.world == null) return false;

        return getCachedState().get(RedstoneCounterBlock.INVERTED);
    }

    protected void setInverted(boolean value)
    {
        if (this.world == null) return;

        this.world.setBlockState(this.pos,getCachedState().with(RedstoneCounterBlock.INVERTED, value));
    }

    public DyeColor getColor()
    {
        return this.color;
    }

    public void setColor(DyeColor color)
    {
        this.color = color;
        markDirty();
    }

    public boolean willCarry(boolean powered, boolean was_powered)
    {
        // Assumes not resetting the counter
        if (powered && !was_powered && this.counter < this.maxCounter) {
            return ((this.counter + 1) == this.maxCounter);
        }

        return false;
    }

    public int newPowerOnIncrement()
    {
        return 15 * Math.min(this.counter + 1, this.maxCounter) / this.maxCounter;
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), Block.NOTIFY_ALL);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);

        nbt.putBoolean("automatic", this.automatic);
        nbt.putInt("counter", this.counter);
        nbt.putInt("maxCounter", this.maxCounter);
        nbt.putInt("color", this.color.getId());
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        this.automatic = nbt.getBoolean("automatic");
        this.counter = nbt.getInt("counter");
        this.maxCounter = nbt.getInt("maxCounter");
        this.color = DyeColor.byId(nbt.getInt("color"));
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
    public RedstoneCounterPayload getScreenOpeningData(ServerPlayerEntity player) {
        return new RedstoneCounterPayload(this.pos, this.automatic, this.getInverted(), this.maxCounter);
    }

    @Override
    public Text getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RedstoneCounterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    private void logInfo(String name)
    {
        RedstoneKit.LOGGER.info("{}({}) - called", name, (this.world == null) ? "(null)" : (this.world.isClient ? "CLIENT" : "SERVER"));
    }

}
