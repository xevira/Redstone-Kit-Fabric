package github.xevira.redstone_kit.item;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


import java.util.List;

public class ResonatorItem extends Item {
    public static final String LINKED_MSG = RedstoneKit.textPath("text", "resonator.linked");
    public static final Text UNLINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.unlinked"));
    public static final Text NOT_LINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.not_linked"));
    public static final Text SOURCE_ALREADY_LINKED_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.source_already_linked"));
    public static final String TARGET_ALREADY_LINKED_MSG = RedstoneKit.textPath("text", "resonator.target_already_linked");
    public static final Text WRONG_DIMENSION_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.wrong_dimension"));
    public static final Text DIFFERENT_DIMENSION_TEXT = Text.translatable(RedstoneKit.textPath("text", "resonator.linked_different_dimension"));

    public ResonatorItem(Settings settings) {
        super(settings);
    }

    private static void unlinkResonator(ItemStack stack, PlayerEntity player)
    {
        if (stack.get(Registration.COORDINATES) != null && stack.get(Registration.WORLD_ID) != null) {
            stack.set(Registration.COORDINATES, null);
            stack.set(Registration.WORLD_ID, null);

            // TODO: Play Sound

            player.sendMessage(Text.literal("Resonator unlinked"));
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

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();

        BlockState state = world.getBlockState(context.getBlockPos());

        if (state.isOf(Registration.TELEPORTER_BLOCK))
        {
            if (!world.isClient)
            {
                ItemStack stack = context.getStack();

                Identifier sourceWorldId = world.getRegistryKey().getValue();
                Identifier targetWorldId = stack.get(Registration.WORLD_ID);

                BlockPos sourcePos = context.getBlockPos();
                BlockPos targetPos = stack.get(Registration.COORDINATES);

                if (targetWorldId != null && targetPos != null)
                {
                    BlockState targetState = world.getBlockState(targetPos);
                    if (targetState.isOf(Registration.TELEPORTER_BLOCK))
                    {
                        if (world.getBlockEntity(targetPos) instanceof TeleporterBlockEntity targetTeleporter &&
                            world.getBlockEntity(sourcePos) instanceof  TeleporterBlockEntity sourceTeleporter)
                        {
                            if (!targetWorldId.equals(sourceWorldId)) {
                                if (context.getPlayer() != null)
                                    context.getPlayer().sendMessage(WRONG_DIMENSION_TEXT);
                            } else if (sourcePos.equals(targetPos)){
                                if (targetTeleporter.isLinked())
                                {
                                    if (world.getBlockEntity(targetTeleporter.getLinkedTeleporter()) instanceof TeleporterBlockEntity linkedTeleporter)
                                        linkedTeleporter.clearLinkedTeleporter();
                                    targetTeleporter.clearLinkedTeleporter();

                                    if (context.getPlayer() != null)
                                        context.getPlayer().sendMessage(UNLINKED_TEXT);
                                }
                                else
                                {
                                    if (context.getPlayer() != null)
                                        context.getPlayer().sendMessage(NOT_LINKED_TEXT);

                                }


                                // TODO: Play sound

                                context.getStack().set(Registration.COORDINATES, null);
                                context.getStack().set(Registration.WORLD_ID, null);


                            } else if (targetTeleporter.isLinked()) {
                                if (context.getPlayer() != null) {
                                    Text msg = Text.translatable(TARGET_ALREADY_LINKED_MSG, targetPos.getX(), targetPos.getY(), targetPos.getZ());
                                    context.getPlayer().sendMessage(msg);
                                }
                            } else if(sourceTeleporter.isLinked()) {
                                if (context.getPlayer() != null)
                                    context.getPlayer().sendMessage(SOURCE_ALREADY_LINKED_TEXT);
                            } else {

                                targetTeleporter.setLinkedTeleporter(sourcePos);
                                sourceTeleporter.setLinkedTeleporter(targetPos);

                                // TODO: Play sound

                                if (context.getPlayer() != null)
                                    context.getPlayer().sendMessage(Text.translatable(LINKED_MSG,  targetPos.getX(), targetPos.getY(), targetPos.getZ()));

                                context.getStack().set(Registration.COORDINATES, null);
                                context.getStack().set(Registration.WORLD_ID, null);
                            }
                        }
                    }
                }
                else {
                    context.getStack().set(Registration.COORDINATES, sourcePos);
                    context.getStack().set(Registration.WORLD_ID, world.getRegistryKey().getValue());
                    // TODO: Play sound
                }
            }

            return ActionResult.SUCCESS;
        } else {
            if (context.getPlayer() != null && context.getPlayer().isSneaking())
            {
                if (!world.isClient)
                    unlinkResonator(context.getStack(), context.getPlayer());
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
            tooltip.add(Text.translatable(RedstoneKit.textPath("tooltip", "resonator.shift_down")));
        else
            tooltip.add(Text.translatable(RedstoneKit.textPath("tooltip", "resonator")));

        BlockPos pos = stack.get(Registration.COORDINATES);
        Identifier worldId = stack.get(Registration.WORLD_ID);

        if(pos != null && worldId != null) {

            tooltip.add(Text.literal(String.format("worldId = §e%s§r", worldId)));
            if (MinecraftClient.getInstance().world == null)
                tooltip.add(Text.literal("client world == null"));
            else
                tooltip.add(Text.literal(String.format("this world = §e%s§r", MinecraftClient.getInstance().world.getRegistryKey().getValue())));

            if (isSameWorld(worldId))
                tooltip.add(Text.translatable(RedstoneKit.textPath("tooltip", "resonator.pending"), pos.getX(), pos.getY(), pos.getZ()));
            else
                tooltip.add(DIFFERENT_DIMENSION_TEXT);
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
