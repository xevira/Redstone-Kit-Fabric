package github.xevira.redstone_kit.item;

import com.sun.jna.platform.unix.X11;
import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


import java.util.List;

public class ResonatorItem extends Item {
    public ResonatorItem(Settings settings) {
        super(settings);
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

                BlockPos pos = stack.get(Registration.COORDINATES);

                if (pos != null)
                {
                    BlockState otherState = world.getBlockState(pos);
                    if (otherState.isOf(Registration.TELEPORTER_BLOCK))
                    {
                        if (world.getBlockEntity(pos) instanceof TeleporterBlockEntity teleporter1 &&
                            world.getBlockEntity(context.getBlockPos()) instanceof  TeleporterBlockEntity teleporter2)
                        {
                            teleporter1.setLinkedTeleporter(context.getBlockPos());
                            teleporter2.setLinkedTeleporter(pos);

                            // TODO: Play sound
                        }
                    }
                    context.getStack().set(Registration.COORDINATES, null);
                }
                else {
                    context.getStack().set(Registration.COORDINATES, context.getBlockPos());
                    // TODO: Play sound
                }
            }

        }

        return ActionResult.SUCCESS;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        if (Screen.hasShiftDown())
            tooltip.add(Text.translatable(RedstoneKit.textPath("tooltip", "resonator.shift_down")));
        else
            tooltip.add(Text.translatable(RedstoneKit.textPath("tooltip", "resonator")));

        if(stack.get(Registration.COORDINATES) != null) {
            tooltip.add(Text.literal("Pending Linkage to " + stack.get(Registration.COORDINATES)));
        }

        super.appendTooltip(stack, context, tooltip, type);
    }
}
