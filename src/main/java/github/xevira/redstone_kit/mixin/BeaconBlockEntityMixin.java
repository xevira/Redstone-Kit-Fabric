package github.xevira.redstone_kit.mixin;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.BeaconLensBlock;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Stainable;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Debug(export = true)
@Mixin(BeaconBlockEntity.class)
public class BeaconBlockEntityMixin /*extends BlockEntity implements IBeaconBlockEntityMixin*/ {
//    @Shadow
//    private List<BeaconBlockEntity.BeamSegment> field_19178;
//
//    @Shadow
//    private List<BeaconBlockEntity.BeamSegment> beamSegments;
//
//    @Shadow
//    private int minY;
//
//    @Shadow
//    private int level;
//
//    @Shadow
//    @Nullable
//    RegistryEntry<StatusEffect> primary;
//
//    @Shadow
//    @Nullable
//    RegistryEntry<StatusEffect> secondary;
//
//    @Shadow
//    private static int updateLevel(World world, int x, int y, int z) { return 0; }
//
//    @Shadow
//    private static void applyPlayerEffects(
//            World world, BlockPos pos, int beaconLevel, @Nullable RegistryEntry<StatusEffect> primaryEffect, @Nullable RegistryEntry<StatusEffect> secondaryEffect) { }
//
//    @Shadow
//    static void playSound(World world, BlockPos pos, SoundEvent sound) { }
//
//    public BeaconBlockEntityMixin(BlockPos pos, BlockState state) {
//        super(BlockEntityType.BEACON, pos, state);
//    }
//
//    /*
//    @ModifyExpressionValue(method = "tick",
//        at = @At(value = "INVOKE",
//                    target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z")
//    )
//    private static boolean tickMixinCheckForBedrock(boolean original, @Local(ordinal = 1) BlockState blockState)
//    {
//        if (original)
//            return true;
//
//        if (!blockState.isOf(Registration.BEACON_LENS_BLOCK)) return false;
//
//        return !blockState.get(BeaconLensBlock.POWERED);
//    }
//*/
//
//    @Redirect(method="tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeaconBlockEntity;)V",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BeaconBlockEntity;tick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/entity/BeaconBlockEntity;)V"))
//    private static void tickRedirect(World world, BlockPos pos, BlockState state, BeaconBlockEntity blockEntity)
//    {
//        ((IBeaconBlockEntityMixin)((Object)blockEntity)).internalTick(world, pos, state);
//
////        int i = pos.getX();
////        int j = pos.getY();
////        int k = pos.getZ();
////        BlockPos blockPos;
////        if (blockEntity.minY < j) {
////            blockPos = pos;
////            blockEntity.field_19178 = Lists.<BeaconBlockEntity.BeamSegment>newArrayList();
////            blockEntity.minY = pos.getY() - 1;
////        } else {
////            blockPos = new BlockPos(i, blockEntity.minY + 1, k);
////        }
////
////        BeaconBlockEntity.BeamSegment beamSegment = blockEntity.field_19178.isEmpty()
////                ? null
////                : (BeaconBlockEntity.BeamSegment)blockEntity.field_19178.get(blockEntity.field_19178.size() - 1);
////        int l = world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);
////
////        for (int m = 0; m < 10 && blockPos.getY() <= l; m++) {
////            BlockState blockState = world.getBlockState(blockPos);
////            if (blockState.getBlock() instanceof Stainable stainable) {
////                int n = stainable.getColor().getEntityColor();
////                if (blockEntity.field_19178.size() <= 1) {
////                    beamSegment = new BeaconBlockEntity.BeamSegment(n);
////                    blockEntity.field_19178.add(beamSegment);
////                } else if (beamSegment != null) {
////                    if (n == beamSegment.color) {
////                        beamSegment.increaseHeight();
////                    } else {
////                        beamSegment = new BeaconBlockEntity.BeamSegment(ColorHelper.Argb.averageArgb(beamSegment.color, n));
////                        blockEntity.field_19178.add(beamSegment);
////                    }
////                }
////            } else {
////                if (beamSegment == null || blockState.getOpacity(world, blockPos) >= 15 && !blockState.isOf(Blocks.BEDROCK)) {
////                    blockEntity.field_19178.clear();
////                    blockEntity.minY = l;
////                    break;
////                }
////
////                beamSegment.increaseHeight();
////            }
////
////            blockPos = blockPos.up();
////            blockEntity.minY++;
////        }
////
////        int m = blockEntity.level;
////        if (world.getTime() % 80L == 0L) {
////            if (!blockEntity.beamSegments.isEmpty()) {
////                blockEntity.level = updateLevel(world, i, j, k);
////            }
////
////            if (blockEntity.level > 0 && !blockEntity.beamSegments.isEmpty()) {
////                applyPlayerEffects(world, pos, blockEntity.level, blockEntity.primary, blockEntity.secondary);
////                playSound(world, pos, SoundEvents.BLOCK_BEACON_AMBIENT);
////            }
////        }
////
////        if (blockEntity.minY >= l) {
////            blockEntity.minY = world.getBottomY() - 1;
////            boolean bl = m > 0;
////            blockEntity.beamSegments = blockEntity.field_19178;
////            if (!world.isClient) {
////                boolean bl2 = blockEntity.level > 0;
////                if (!bl && bl2) {
////                    playSound(world, pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
////
////                    for (ServerPlayerEntity serverPlayerEntity : world.getNonSpectatingEntities(
////                            ServerPlayerEntity.class, new Box((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k).expand(10.0, 5.0, 10.0)
////                    )) {
////                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, blockEntity.level);
////                    }
////                } else if (bl && !bl2) {
////                    playSound(world, pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
////                }
////            }
////        }
//    }
//
//    @Override
//    public void internalTick(World _world, BlockPos _pos, BlockState _state)
//    {
//        int i = _pos.getX();
//        int j = _pos.getY();
//        int k = _pos.getZ();
//        BlockPos blockPos;
//
//        if (this.minY < j) {
//            blockPos = _pos;
//            this.field_19178 = Lists.newArrayList();
//            this.minY = _pos.getY() - 1;
//        } else {
//            blockPos = new BlockPos(i, this.minY + 1, k);
//        }
//
//        BeaconBlockEntity.BeamSegment beamSegment = this.field_19178.isEmpty()
//                ? null
//                : this.field_19178.get(this.field_19178.size() - 1);
//        int l = _world.getTopY(Heightmap.Type.WORLD_SURFACE, i, k);
//
//        for (int m = 0; m < 10 && blockPos.getY() <= l; m++) {
//            BlockState blockState = _world.getBlockState(blockPos);
//            if (blockState.getBlock() instanceof Stainable stainable) {
//                int n = stainable.getColor().getEntityColor();
//                if (this.field_19178.size() <= 1) {
//                    beamSegment = new BeaconBlockEntity.BeamSegment(n);
//                    this.field_19178.add(beamSegment);
//                } else if (beamSegment != null) {
//                    if (n == beamSegment.getColor()) {
//                        ((IBeamSegmentMixin)(Object)beamSegment).grow();
//                    } else {
//                        beamSegment = new BeaconBlockEntity.BeamSegment(ColorHelper.Argb.averageArgb(beamSegment.getColor(), n));
//                        this.field_19178.add(beamSegment);
//                    }
//                }
//            } else {
//                if (beamSegment == null || blockState.getOpacity(world, blockPos) >= 15 && !blockState.isOf(Blocks.BEDROCK) && (!blockState.isOf(Registration.BEACON_LENS_BLOCK) || blockState.get(BeaconLensBlock.POWERED))) {
//                    this.field_19178.clear();
//                    this.minY = l;
//                    break;
//                }
//
//                if (blockState.isOf(Registration.BEACON_LENS_BLOCK))
//                    break;
//
//                ((IBeamSegmentMixin)(Object)beamSegment).grow();
//            }
//
//            blockPos = blockPos.up();
//            this.minY++;
//        }
//
//        int m = this.level;
//        if (_world.getTime() % 80L == 0L) {
//            if (!this.beamSegments.isEmpty()) {
//                this.level = updateLevel(_world, i, j, k);
//            }
//
//            if (this.level > 0 && !this.beamSegments.isEmpty()) {
//                applyPlayerEffects(_world, _pos, this.level, this.primary, this.secondary);
//                playSound(_world, _pos, SoundEvents.BLOCK_BEACON_AMBIENT);
//            }
//        }
//
//        if (this.minY >= l) {
//            this.minY = _world.getBottomY() - 1;
//            boolean bl = m > 0;
//            this.beamSegments = this.field_19178;
//            if (!_world.isClient) {
//                boolean bl2 = this.level > 0;
//                if (!bl && bl2) {
//                    playSound(_world, _pos, SoundEvents.BLOCK_BEACON_ACTIVATE);
//
//                    for (ServerPlayerEntity serverPlayerEntity : _world.getNonSpectatingEntities(
//                            ServerPlayerEntity.class, new Box((double)i, (double)j, (double)k, (double)i, (double)(j - 4), (double)k).expand(10.0, 5.0, 10.0)
//                    )) {
//                        Criteria.CONSTRUCT_BEACON.trigger(serverPlayerEntity, this.level);
//                    }
//                } else if (bl && !bl2) {
//                    playSound(_world, _pos, SoundEvents.BLOCK_BEACON_DEACTIVATE);
//                }
//            }
//        }
//    }
}
