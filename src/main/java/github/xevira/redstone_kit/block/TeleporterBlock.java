package github.xevira.redstone_kit.block;

import com.mojang.serialization.MapCodec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.PlayerDetectorBlockEntity;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import github.xevira.redstone_kit.config.ServerConfig;
import github.xevira.redstone_kit.network.TeleporterTeleportPlayerPayload;
import github.xevira.redstone_kit.util.ServerTickableBlockEntity;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TeleporterBlock extends BlockWithEntity {
    public static final String LINKED_PEARL_MSG = RedstoneKit.textPath("text", "teleporter.linked.pearl");
    public static final String LINKED_PEARLS_MSG = RedstoneKit.textPath("text", "teleporter.linked.pearls");
    public static final String LINKED_XP_MSG = RedstoneKit.textPath("text", "teleporter.linked.xp");
    public static final String LINKED_XPS_MSG = RedstoneKit.textPath("text", "teleporter.linked.xps");
    public static final Text NOT_LINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "teleporter.not_linked"));

    protected static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0000, 0.0000, 0.0000, 1.0000, 0.0625, 1.0000),

            VoxelShapes.cuboid(0.0625, 0.0625, 0.0625, 0.8750, 0.1250, 0.1250),
            VoxelShapes.cuboid(0.0625, 0.0625, 0.1250, 0.1250, 0.1250, 0.9375),
            VoxelShapes.cuboid(0.1250, 0.0625, 0.8750, 0.9375, 0.1250, 0.9375),
            VoxelShapes.cuboid(0.8750, 0.0625, 0.0625, 0.9375, 0.1250, 0.8750),

            VoxelShapes.cuboid(0.1250, 0.0625, 0.1250, 0.2500, 0.1875, 0.7500),
            VoxelShapes.cuboid(0.1250, 0.0625, 0.7500, 0.7500, 0.1875, 0.8750),
            VoxelShapes.cuboid(0.7500, 0.0625, 0.2500, 0.8750, 0.1875, 0.8750),
            VoxelShapes.cuboid(0.2500, 0.0625, 0.1250, 0.8750, 0.1875, 0.2500)
    ).simplify();

    public static final MapCodec<TeleporterBlock> CODEC = createCodec(TeleporterBlock::new);


    public TeleporterBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return  BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return Registration.TELEPORTER_BLOCK_ENTITY.instantiate(pos, state);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
        if (!stack.isEmpty())
            return ActionResult.PASS;

        if(!world.isClient)
        {
            if (world.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
                if (teleporter.isLinked())
                {
                    BlockPos targetPos = teleporter.getLinkedTeleporter();
                    if (ServerConfig.getConfig().getTeleportUseXP())
                    {
                        int cost = teleporter.getTeleportXPCost();

                        if (cost == 1)
                            player.sendMessage(Text.translatable(LINKED_XP_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ()));
                        else
                            player.sendMessage(Text.translatable(LINKED_XPS_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ(), cost));
                    }
                    else
                    {
                        int cost = teleporter.getTeleportPearlCost();

                        if (cost == 1)
                            player.sendMessage(Text.translatable(LINKED_PEARL_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ()));
                        else
                            player.sendMessage(Text.translatable(LINKED_PEARLS_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ(), cost));
                    }
                }
                else
                {
                    player.sendMessage(NOT_LINKED_TEXT);
                }
            }
        }
        return ActionResult.success(world.isClient);
    }


    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return ServerTickableBlockEntity.getTicker(world);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if(world.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter) {
            if (!teleporter.hasCooldown() && teleporter.isLinked()) {
                for (int i = 0; i < 3; i++) {
                    double x = (double) pos.getX() + random.nextDouble() * 0.5 + 0.25;
                    double y = (double) pos.getY() + random.nextDouble() * 0.0625 + 0.125;
                    double z = (double) pos.getZ() + random.nextDouble() * 0.5 + 0.25;
                    world.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0, 0.0, 0.0);
                }
            }
        }
    }

    // CREDIT: OpenBlocks' ElevatorMod for the algorithm for handling this.
    @Nullable
    public static class TeleportHandler {
        private static boolean lastJumping;

        public static void handleInput(MinecraftClient client)
        {
            PlayerEntity player = client.player;

            if (player == null || player.isSpectator() || !player.isAlive()) return;

            boolean isJumping = client.options.jumpKey.isPressed();
            if (lastJumping != isJumping)
            {
                lastJumping = isJumping;
                if (isJumping)
                    tryTelport(player);
            }
        }

        private static void tryTelport(PlayerEntity player)
        {
            World world = player.getWorld();

            BlockPos fromPos = getTeleporter(player);
            if (fromPos == null) return;

            if(world.getBlockEntity(fromPos) instanceof TeleporterBlockEntity fromTeleporter)
            {
                ClientPlayNetworking.send(new TeleporterTeleportPlayerPayload(fromPos));
            }
        }

        private static BlockPos getTeleporter(PlayerEntity player)
        {
            BlockPos pos = player.getBlockPos();
            World world = player.getWorld();

            for(int i = 0; i < 3; i++)
            {
                BlockState state = world.getBlockState(pos);
                if (state != null && state.isOf(Registration.TELEPORTER_BLOCK))
                    return pos;

                pos = pos.down();
            }

            return null;
        }
    }
}
