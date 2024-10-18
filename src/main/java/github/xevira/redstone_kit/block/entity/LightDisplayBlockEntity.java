package github.xevira.redstone_kit.block.entity;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.LightDisplayBlock;
import github.xevira.redstone_kit.block.LightDisplayBulbBlock;
import github.xevira.redstone_kit.poi.LightDisplayBulbs;
import github.xevira.redstone_kit.util.Boxi;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LightDisplayBlockEntity extends BlockEntity implements ServerTickableBlockEntity {
    public static final int MAX_RANGE = 15;

    private int powerLevel;
    private @Nullable BlockPos bulbStart;
    private @Nullable Direction bulbDir;
    private int bulbs;

    public LightDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.LIGHT_DISPLAY_BLOCK_ENTITY, pos, state);

        this.powerLevel = 0;
        this.bulbStart = null;
        this.bulbDir = null;
        this.bulbs = -1;
    }

    private static void updateBulbs(World world, BlockPos start, Direction dir, int count, int signal)
    {
        if (dir == null && count > 1) return;   // Must have a direction for multiple bulbs

        BlockPos.Mutable blockPos = start.mutableCopy();

        for(int i = 0; i < count; i++) {
            BlockState s = world.getBlockState(blockPos);

            boolean lit = i < signal;
            if (s.isOf(Registration.LIGHT_DISPLAY_BULB_BLOCK) && (s.get(LightDisplayBulbBlock.LIT) != lit)) {
                world.setBlockState(blockPos, s.with(LightDisplayBulbBlock.LIT, lit));
            }

            if (dir != null)
                blockPos.move(dir);
        }

    }

    public void clearBulbs()
    {
        unregisterBulbs();

        this.bulbStart = null;
        this.bulbDir = null;
        this.bulbs = -1;
        markDirty();
    }

    public boolean setBulbs(BlockPos start, BlockPos end)
    {
        Boxi box = new Boxi(start, end);
        Direction.Axis axis = box.getColumnAxis();

        if (axis == null) return false;

        int length = box.getLength(axis) + 1;

        if (length > 15) return false;

        // TODO: Remove existing bulb line from POS

        BlockPos diff = end.subtract(start);
        Direction dir = Direction.fromVector(diff.getX(), diff.getY(), diff.getZ());

        if ((length > 1) && (dir == null)) return false;

        if (this.world == null) return false;

        Boxi oldBox = null;

        if (this.bulbStart != null && this.bulbDir != null)
        {
            BlockPos bulbEnd = (this.bulbs > 1) ? this.bulbStart.offset(this.bulbDir, this.bulbs - 1) : this.bulbStart;

            oldBox = new Boxi(this.bulbStart, bulbEnd);

            LightDisplayBulbs.remove(this.world, oldBox);
        }

        if (!LightDisplayBulbs.add(this.world, box))
        {
            if (oldBox != null)
                LightDisplayBulbs.add(this.world, oldBox);

            return false;
        }

        // Turn off old bulbs
        deactivateBulbs();

        this.bulbStart = start;
        this.bulbDir = dir;
        this.bulbs = length;

        // Turn on new bulbs
        activateBulbs();
        return true;
    }

    private int detectMaxRange(Direction facing)
    {
        if (this.world == null) return 0;

        BlockPos.Mutable blockPos = this.pos.mutableCopy();

        for(int i = 0; i < MAX_RANGE; i++)
        {
            blockPos.move(facing);

            BlockState state = this.world.getBlockState(blockPos);

            if (!state.isOf(Registration.LIGHT_DISPLAY_BULB_BLOCK))
                return i;
        }

        return MAX_RANGE;
    }

    public void deactivateBulbs()
    {
        assert this.world != null;

        BlockState state = getCachedState();
        Direction facing = state.get(LightDisplayBlock.FACING);

        BlockPos.Mutable blockPos;
        int maxRange;

        if (this.bulbStart == null)
        {
            blockPos = this.pos.mutableCopy().move(facing);
            maxRange = detectMaxRange(facing);
        }
        else
        {
            blockPos = this.bulbStart.mutableCopy();
            facing = this.bulbDir;
            maxRange = this.bulbs;
        }

        if (maxRange < 1) return;

        updateBulbs(this.world, blockPos, facing, maxRange, 0);
    }

    public void activateBulbs()
    {
        assert this.world != null;
        BlockState state = getCachedState();
        Direction facing = state.get(LightDisplayBlock.FACING);

        BlockPos.Mutable blockPos;
        int maxRange;

        if (this.bulbStart == null)
        {
            blockPos = this.pos.mutableCopy().move(facing);
            maxRange = detectMaxRange(facing);
        }
        else
        {
            blockPos = this.bulbStart.mutableCopy();
            facing = this.bulbDir;
            maxRange = this.bulbs;
        }

        if (maxRange < 1) return;
        int power = LightDisplayBlock.getMaxRedstonePowerReceived(this.world, this.pos, state);

        boolean dirty = power != this.powerLevel;
        if (dirty || (this.world.getTime() % 20) == 0)
        {
            this.powerLevel = power;

            int range = maxRange * this.powerLevel / 15;

            updateBulbs(this.world, blockPos, facing, maxRange, range);
        }

        if (dirty)
            markDirty();
    }

    public void unregisterBulbs()
    {
        if (this.world == null) return;

        deactivateBulbs();
        if (this.bulbStart != null)
        {
            BlockPos end = (this.bulbs > 1) ? this.bulbStart.offset(this.bulbDir, this.bulbs - 1) : this.bulbStart;

            Boxi box = new Boxi(this.bulbStart, end);

            LightDisplayBulbs.remove(this.world, box);
        }
    }

    @Override
    public void serverTick() {
        if(this.world == null) return;

        // Only do it on the "redstone" tick
        if((this.world.getTime() % 2) != 0) return;

        activateBulbs();
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (this.world != null)
            world.updateListeners(this.pos, getCachedState(), getCachedState(), 3);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("powerLevel", this.powerLevel);
        if (this.bulbStart != null && this.bulbDir != null) {
            nbt.putLong("bulbStart", this.bulbStart.asLong());
            nbt.putString("bulbDir", this.bulbDir.getName());
            nbt.putInt("bulbs", this.bulbs);
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        this.powerLevel = nbt.getInt("powerLevel");

        if (nbt.contains("bulbStart", NbtElement.LONG_TYPE))
            this.bulbStart = BlockPos.fromLong(nbt.getLong("bulbStart"));

        if (nbt.contains("bulbDir", NbtElement.STRING_TYPE))
            this.bulbDir = Direction.byName(nbt.getString("bulbDir"));

        if (nbt.contains("bulbs", NbtElement.INT_TYPE))
            this.bulbs = nbt.getInt("bulbs");
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

}
