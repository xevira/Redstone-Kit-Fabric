package github.xevira.redstone_kit.item;

import com.mojang.serialization.Codec;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.session.report.ReporterEnvironment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class ResonatorItem extends Item {
    public static final Text RESONATOR_UNLINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.unlinked"));

    public static final Text RESONATOR_SHIFT_DOWN_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "resonator.shift_down"));
    public static final Text RESONATOR_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "resonator"));
    public static final Text RESONATOR_PENDING_TOOLTIP = Text.translatable(RedstoneKit.textPath("tooltip", "resonator.pending"));

    public static final String RESONATOR_SOURCE_PREFIX_MSG = RedstoneKit.textPath("tooltip", "resonator.source.");
    public static final String RESONATOR_DIMENSION_MSG = RedstoneKit.textPath("tooltip", "resonator.dimension");
    public static final String RESONATOR_POSITION_MSG = RedstoneKit.textPath("tooltip", "resonator.position");

    public static final String RESONATOR_PENDING_PREFIX_MSG = RedstoneKit.textPath("text", "resonator.pending.");
    public static final String TELEPORTER_LINKED_MSG = RedstoneKit.textPath("text", "resonator.teleporter.linked");
    public static final Text TELEPORTER_UNLINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.teleporter.unlinked"));
    public static final Text TELEPORTER_NOT_LINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.teleporter.not_linked"));
    public static final Text TELEPORTER_SOURCE_ALREADY_LINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.teleporter.source_already_linked"));
    public static final String TELEPORTER_TARGET_ALREADY_LINKED_MSG = RedstoneKit.textPath("text", "resonator.teleporter.target_already_linked");
    public static final Text TELEPORTER_WRONG_DIMENSION_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.teleporter.wrong_dimension"));
    public static final Text TELEPORTER_DIFFERENT_DIMENSION_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.teleporter.linked_different_dimension"));

    public ResonatorItem(Settings settings) {
        super(settings);
    }

    private static void setResonatorData(@NotNull ItemStack stack, @NotNull ResonatorTypeEnum type, @NotNull World world, @NotNull BlockPos pos)
    {
        stack.set(Registration.RESONATOR_TYPE, type);
        stack.set(Registration.COORDINATES, pos);
        stack.set(Registration.WORLD_ID, world.getRegistryKey().getValue());
    }

    private static void clearResonatorData(@NotNull ItemStack stack)
    {
        stack.set(Registration.RESONATOR_TYPE, null);
        stack.set(Registration.COORDINATES, null);
        stack.set(Registration.WORLD_ID, null);
    }

    private static void unlinkResonator(@NotNull ItemStack stack, @NotNull PlayerEntity player)
    {
        if (stack.get(Registration.RESONATOR_TYPE) != null ||
                stack.get(Registration.COORDINATES) != null ||
                stack.get(Registration.WORLD_ID) != null) {
            clearResonatorData(stack);

            // TODO: Play Sound

            player.sendMessage(RESONATOR_UNLINKED_TEXT);
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (!world.isClient) {
            if (user.isSneaking()) {

                unlinkResonator(itemStack, user);

                return TypedActionResult.success(itemStack);
            }
        }

        return TypedActionResult.pass(itemStack);
    }

    private TeleporterBlockEntity getTeleporter(World world, @Nullable TeleporterBlockEntity.TeleportLocation location)
    {
        if (location == null) return null;

        if (world instanceof ServerWorld serverWorld)
        {
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, location.worldId());

            World targetWorld = serverWorld.getServer().getWorld(key);
            if (targetWorld == null) return null;   // World doesn't exist

            if (targetWorld.getBlockEntity(location.pos()) instanceof TeleporterBlockEntity be)
                return be;
        }

        return null;
    }

    private @Nullable World getWorldFromId(World sourceWorld, Identifier targetId)
    {
        if (targetId == null) return null;

        if (sourceWorld instanceof ServerWorld serverWorld)
        {
            RegistryKey<World> key = RegistryKey.of(RegistryKeys.WORLD, targetId);
            return serverWorld.getServer().getWorld(key);
        }

        return null;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();

        if (world.isClient)
            return ActionResult.SUCCESS;

        PlayerEntity player = context.getPlayer();

        // This must be done by a player in their hand.
        if (player == null)
            return ActionResult.FAIL;

        BlockState state = world.getBlockState(context.getBlockPos());
        ItemStack stack = context.getStack();

        ResonatorTypeEnum type = stack.get(Registration.RESONATOR_TYPE);
        Identifier sourceWorldId = world.getRegistryKey().getValue();
        Identifier targetWorldId = stack.get(Registration.WORLD_ID);
        World targetWorld = getWorldFromId(world, targetWorldId);
        BlockPos sourcePos = context.getBlockPos();
        BlockPos targetPos = stack.get(Registration.COORDINATES);

        if (state.isOf(Registration.TELEPORTER_BLOCK))
        {
            if (world.getBlockEntity(sourcePos) instanceof  TeleporterBlockEntity sourceTeleporter) {
                if (!sourceTeleporter.canConfigure(player))
                    return ActionResult.SUCCESS;

                if (type != null && type != ResonatorTypeEnum.TELEPORTER) {
                    player.sendMessage(Text.translatable(RESONATOR_PENDING_PREFIX_MSG + type));

                } else if (targetWorld != null && targetPos != null) {
                    BlockState targetState = targetWorld.getBlockState(targetPos);
                    if (targetState.isOf(Registration.TELEPORTER_BLOCK)) {
                        if (targetWorld.getBlockEntity(targetPos) instanceof TeleporterBlockEntity targetTeleporter) {
                            if (!targetTeleporter.canConfigure(player))
                                return ActionResult.SUCCESS;

                            if (targetWorld == world && sourcePos.equals(targetPos)) {
                                if (targetTeleporter.isLinked()) {
                                    TeleporterBlockEntity linkedTeleporter = getTeleporter(world, targetTeleporter.getLinkedTeleporter());
                                    if (linkedTeleporter != null)
                                        linkedTeleporter.clearLinkedTeleporter();
                                    targetTeleporter.clearLinkedTeleporter();

                                    player.sendMessage(TELEPORTER_UNLINKED_TEXT);
                                } else {
                                    player.sendMessage(TELEPORTER_NOT_LINKED_TEXT);
                                }

                                // TODO: Play sound

                                clearResonatorData(stack);


                            } else if (targetTeleporter.isLinked()) {
                                player.sendMessage(Text.translatable(TELEPORTER_TARGET_ALREADY_LINKED_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ()));
                            } else if (sourceTeleporter.isLinked()) {
                                player.sendMessage(TELEPORTER_SOURCE_ALREADY_LINKED_TEXT);
                            } else {

                                targetTeleporter.setLinkedTeleporter(sourceWorldId, sourcePos);
                                sourceTeleporter.setLinkedTeleporter(targetWorldId, targetPos);

                                // TODO: Play sound

                                player.sendMessage(Text.translatable(TELEPORTER_LINKED_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ()));

                                clearResonatorData(stack);
                            }
                        }
                    }

                } else {
                    setResonatorData(stack, ResonatorTypeEnum.TELEPORTER, world, sourcePos);

                    // TODO: Play sound
                }
            }

            return ActionResult.SUCCESS;

        } else {
            if (player.isSneaking())
            {
                unlinkResonator(stack, player);
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private static boolean isSameWorld(Identifier targetWorldId)
    {
        if (MinecraftClient.getInstance().world == null) return false;

        return targetWorldId.equals(MinecraftClient.getInstance().world.getRegistryKey().getValue());
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown())
            tooltip.add(RESONATOR_SHIFT_DOWN_TOOLTIP);
        else
            tooltip.add(RESONATOR_TOOLTIP);

        ResonatorTypeEnum resonatorType = stack.get(Registration.RESONATOR_TYPE);
        BlockPos pos = stack.get(Registration.COORDINATES);
        Identifier worldId = stack.get(Registration.WORLD_ID);

        if(resonatorType != null && pos != null && worldId != null) {
            tooltip.add(RESONATOR_PENDING_TOOLTIP);
            tooltip.add(Text.translatable(RESONATOR_DIMENSION_MSG, worldId.getPath()));
            tooltip.add(Text.translatable(RESONATOR_SOURCE_PREFIX_MSG + resonatorType));
            tooltip.add(Text.translatable(RESONATOR_POSITION_MSG, pos.getX(), pos.getY(), pos.getZ()));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }

    public enum ResonatorTypeEnum implements StringIdentifiable {
        RECEIVER("receiver"),           // Initial link was on a Receiver
        TELEPORTER("teleporter"),       // Initial link was on a Teleporter
        TRANSMITTER("transmitter")      // Initial link was on a Transmitter
        ;

        private final String name;

        ResonatorTypeEnum(final String name) {
            this.name = name;
        }

        public static final Codec<ResonatorTypeEnum> CODEC = StringIdentifiable.createCodec(ResonatorTypeEnum::values);

        public String toString() {
            return this.name;
        }

        @Override
        public String asString() {
            return this.name;
        }

    }
}
