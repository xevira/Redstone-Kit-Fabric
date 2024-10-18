package github.xevira.redstone_kit.util;

import com.mojang.serialization.Codec;
import github.xevira.redstone_kit.RedstoneKit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.util.Util;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.util.stream.IntStream;

public class Boxi {
    private static final int SIZE_BITS_X = 1 + MathHelper.floorLog2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int SIZE_BITS_Z = SIZE_BITS_X;
    public static final int SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z;
    private static final long BITS_X = (1L << SIZE_BITS_X) - 1L;
    private static final long BITS_Y = (1L << SIZE_BITS_Y) - 1L;
    private static final long BITS_Z = (1L << SIZE_BITS_Z) - 1L;
    private static final int field_33083 = 0;
    private static final int BIT_SHIFT_Z = SIZE_BITS_Y;
    private static final int BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z;

    public static final Codec<Boxi> CODEC = Codec.INT_STREAM
            .comapFlatMap(
                    stream -> Util.decodeFixedLengthArray(stream, 6).map(values -> new Boxi(values[0], values[1], values[2], values[3], values[4], values[5])),
                    box -> IntStream.of(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ)
            )
            .stable();
    public static final PacketCodec<ByteBuf, Boxi> PACKET_CODEC = new PacketCodec<ByteBuf, Boxi>() {
        public Boxi decode(ByteBuf byteBuf) { return Boxi.readBuf(byteBuf); }
        public void encode(ByteBuf byteBuf, Boxi boxi) { Boxi.writeBuf(byteBuf, boxi); }
    };

    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;

    public Boxi(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }

    /**
     * Creates a box that only contains the given block position.
     */
    public Boxi(BlockPos pos) {
        this(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1));
    }

    public Boxi(Vec3i pos1, Vec3i pos2) {
        this(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public static Box from(Vec3i pos) {
        return new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
    }

    public static Boxi enclosing(BlockPos pos1, BlockPos pos2) {
        return new Boxi(
                Math.min(pos1.getX(), pos2.getX()),
                Math.min(pos1.getY(), pos2.getY()),
                Math.min(pos1.getZ(), pos2.getZ()),
                (Math.max(pos1.getX(), pos2.getX()) + 1),
                (Math.max(pos1.getY(), pos2.getY()) + 1),
                (Math.max(pos1.getZ(), pos2.getZ()) + 1)
        );
    }

    public BlockPos getMin()
    {
        return new BlockPos(this.minX, this.minY, this.minZ);
    }

    public BlockPos getMax()
    {
        return new BlockPos(this.maxX, this.maxY, this.maxZ);
    }

    public static void writeBuf(ByteBuf buf, Boxi box)
    {
        buf.writeLong(asLong(box.minX, box.minY, box.minZ));
        buf.writeLong(asLong(box.maxX, box.maxY, box.maxZ));
    }

    public static long asLong(int x, int y, int z) {
        long l = 0L;
        l |= ((long)x & BITS_X) << BIT_SHIFT_X;
        l |= ((long)y & BITS_Y);
        return l | ((long)z & BITS_Z) << BIT_SHIFT_Z;
    }

    public static Boxi readBuf(ByteBuf buf)
    {
        long minL = buf.readLong();
        long maxL = buf.readLong();

        return new Boxi(unpackLongX(minL), unpackLongY(minL), unpackLongZ(minL), unpackLongX(maxL), unpackLongY(maxL), unpackLongZ(maxL));
    }

    public static int unpackLongX(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_X - SIZE_BITS_X >> 64 - SIZE_BITS_X);
    }

    public static int unpackLongY(long packedPos) {
        return (int)(packedPos << 64 - SIZE_BITS_Y >> 64 - SIZE_BITS_Y);
    }

    public static int unpackLongZ(long packedPos) {
        return (int)(packedPos << 64 - BIT_SHIFT_Z - SIZE_BITS_Z >> 64 - SIZE_BITS_Z);
    }


    /**
     * {@return a new box with the minimum X provided and all other coordinates
     * of this box}
     */
    public Boxi withMinX(int minX) {
        return new Boxi(minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    /**
     * {@return a new box with the minimum Y provided and all other coordinates
     * of this box}
     */
    public Boxi withMinY(int minY) {
        return new Boxi(this.minX, minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    /**
     * {@return a new box with the minimum Z provided and all other coordinates
     * of this box}
     */
    public Boxi withMinZ(int minZ) {
        return new Boxi(this.minX, this.minY, minZ, this.maxX, this.maxY, this.maxZ);
    }

    /**
     * {@return a new box with the maximum X provided and all other coordinates
     * of this box}
     */
    public Boxi withMaxX(int maxX) {
        return new Boxi(this.minX, this.minY, this.minZ, maxX, this.maxY, this.maxZ);
    }

    /**
     * {@return a new box with the maximum Y provided and all other coordinates
     * of this box}
     */
    public Boxi withMaxY(int maxY) {
        return new Boxi(this.minX, this.minY, this.minZ, this.maxX, maxY, this.maxZ);
    }

    /**
     * {@return a new box with the maximum Z provided and all other coordinates
     * of this box}
     */
    public Boxi withMaxZ(int maxZ) {
        return new Boxi(this.minX, this.minY, this.minZ, this.maxX, this.maxY, maxZ);
    }


    /**
     * {@return the minimum coordinate for the given {@code axis} of this box}
     */
    public int getMin(Direction.Axis axis) {
        return axis.choose(this.minX, this.minY, this.minZ);
    }

    /**
     * {@return the maximum coordinate for the given {@code axis} of this box}
     */
    public int getMax(Direction.Axis axis) {
        return axis.choose(this.maxX, this.maxY, this.maxZ);
    }

    public int getLength(Direction.Axis axis) { return axis.choose(this.maxX - this.minX, this.maxY - this.minY, this.maxZ - this.minZ); }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof Boxi box)) {
            return false;
        } else if (box.minX != this.minX) {
            return false;
        } else if (box.minY != this.minY) {
            return false;
        } else if (box.minZ != this.minZ) {
            return false;
        } else if (box.maxX != this.maxX) {
            return false;
        } else {
            return box.maxY == this.maxY && box.maxZ == this.maxZ;
        }
    }

    public int hashCode() {
        long l = Integer.hashCode(this.minX);
        int i = Long.hashCode(l);
        l = Integer.hashCode(this.minY);
        i = 31 * i + Long.hashCode(l);
        l = Integer.hashCode(this.minZ);
        i = 31 * i + Long.hashCode(l);
        l = Integer.hashCode(this.maxX);
        i = 31 * i + Long.hashCode(l);
        l = Integer.hashCode(this.maxY);
        i = 31 * i + Long.hashCode(l);
        l = Integer.hashCode(this.maxZ);
        return 31 * i + Long.hashCode(l);
    }

    public Boxi shrink(int x, int y, int z) {
        int d = this.minX;
        int e = this.minY;
        int f = this.minZ;
        int g = this.maxX;
        int h = this.maxY;
        int i = this.maxZ;
        if (x < 0) {
            d -= x;
        } else if (x > 0) {
            g -= x;
        }

        if (y < 0) {
            e -= y;
        } else if (y > 0) {
            h -= y;
        }

        if (z < 0) {
            f -= z;
        } else if (z > 0) {
            i -= z;
        }

        return new Boxi(d, e, f, g, h, i);
    }

    public Boxi stretch(Vec3i scale) {
        return this.stretch(scale.getX(), scale.getY(), scale.getZ());
    }

    public Boxi stretch(int x, int y, int z) {
        int d = this.minX;
        int e = this.minY;
        int f = this.minZ;
        int g = this.maxX;
        int h = this.maxY;
        int i = this.maxZ;
        if (x < 0) {
            d += x;
        } else if (x > 0) {
            g += x;
        }

        if (y < 0) {
            e += y;
        } else if (y > 0) {
            h += y;
        }

        if (z < 0) {
            f += z;
        } else if (z > 0) {
            i += z;
        }

        return new Boxi(d, e, f, g, h, i);
    }

    /**
     * @see #contract(int, int, int)
     */
    public Boxi expand(int x, int y, int z) {
        int d = this.minX - x;
        int e = this.minY - y;
        int f = this.minZ - z;
        int g = this.maxX + x;
        int h = this.maxY + y;
        int i = this.maxZ + z;
        return new Boxi(d, e, f, g, h, i);
    }


    /**
     * @see #contract(int)
     */
    public Boxi expand(int value) {
        return this.expand(value, value, value);
    }

    /**
     * Creates the maximum box that this box and the given box contain.
     */
    public Boxi intersection(Boxi box) {
        int d = Math.max(this.minX, box.minX);
        int e = Math.max(this.minY, box.minY);
        int f = Math.max(this.minZ, box.minZ);
        int g = Math.min(this.maxX, box.maxX);
        int h = Math.min(this.maxY, box.maxY);
        int i = Math.min(this.maxZ, box.maxZ);
        return new Boxi(d, e, f, g, h, i);
    }



    /**
     * Creates the minimum box that contains this box and the given box.
     */
    public Boxi union(Boxi box) {
        int d = Math.min(this.minX, box.minX);
        int e = Math.min(this.minY, box.minY);
        int f = Math.min(this.minZ, box.minZ);
        int g = Math.max(this.maxX, box.maxX);
        int h = Math.max(this.maxY, box.maxY);
        int i = Math.max(this.maxZ, box.maxZ);
        return new Boxi(d, e, f, g, h, i);
    }

    /**
     * Creates a box that is translated by {@code x}, {@code y}, {@code z} on
     * each axis from this box.
     */
    public Boxi offset(int x, int y, int z) {
        return new Boxi(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    /**
     * Creates a box that is translated by {@code blockPos.getX()}, {@code
     * blockPos.getY()}, {@code blockPos.getZ()} on each axis from this box.
     *
     * @see #offset(int, int, int)
     */
    public Boxi offset(BlockPos blockPos) {
        return new Boxi(
                this.minX + blockPos.getX(),
                this.minY + blockPos.getY(),
                this.minZ + blockPos.getZ(),
                this.maxX + blockPos.getX(),
                this.maxY + blockPos.getY(),
                this.maxZ + blockPos.getZ()
        );
    }

    /**
     * Creates a box that is translated by {@code vec.x}, {@code vec.y}, {@code
     * vec.z} on each axis from this box.
     *
     * @see #offset(int, int, int)
     */
    public Boxi offset(Vec3i vec) {
        return this.offset(vec.getX(), vec.getY(), vec.getZ());
    }

    /**
     * Checks if this box intersects the given box.
     */
    public boolean intersects(Boxi box) {
        return this.intersects(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /**
     * Checks if this box intersects the box of the given coordinates.
     */
    public boolean intersects(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        return this.minX <= maxX && this.maxX >= minX && this.minY <= maxY && this.maxY >= minY && this.minZ <= maxZ && this.maxZ >= minZ;
    }

    /**
     * Checks if this box intersects the box of the given positions as
     * corners.
     */
    public boolean intersects(Vec3i pos1, Vec3i pos2) {
        return this.intersects(
                Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
                Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ())
        );
    }

    /**
     * Checks if the given position is in this box.
     */
    public boolean contains(Vec3i pos) {
        return this.contains(pos.getX(), pos.getY(), pos.getZ());
    }

    /**
     * Checks if the given position is in this box.
     */
    public boolean contains(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    public double getAverageSideLength() {
        double d = this.getLengthX();
        double e = this.getLengthY();
        double f = this.getLengthZ();
        return (d + e + f) / 3.0;
    }

    /**
     * {@return the length of this box on the X axis}
     */
    public int getLengthX() { return this.maxX - this.minX + 1; }

    /**
     * {@return the length of this box on the Y axis}
     */
    public int getLengthY() { return this.maxY - this.minY + 1; }

    /**
     * {@return the length of this box on the Z axis}
     */
    public int getLengthZ() {
        return this.maxZ - this.minZ + 1;
    }

    /**
     * @see #expand(int, int, int)
     */
    public Boxi contract(int x, int y, int z) {
        return this.expand(-x, -y, -z);
    }

    /**
     * @see #expand(int)
     */
    public Boxi contract(int value) {
        return this.expand(-value);
    }

    public String toString() {
        return "AABB[" + this.minX + ", " + this.minY + ", " + this.minZ + "] -> [" + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public @Nullable Direction.Axis getColumnAxis()
    {
        int x = this.maxX - this.minX;
        int y = this.maxY - this.minY;
        int z = this.maxZ - this.minZ;

        if (x != 0 && y == 0 && z == 0) return Direction.Axis.X;
        if (x == 0 && y != 0 && z == 0) return Direction.Axis.Y;
        if (x == 0 && y == 0 && z != 0) return Direction.Axis.Z;

        return null;
    }
}
