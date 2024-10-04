package github.xevira.redstone_kit.mixin;

import github.xevira.redstone_kit.Registration;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneWireBlock.class)
public class RedstoneWireBlockMixin {

    @Inject(method = "connectsTo(Lnet/minecraft/block/BlockState;Lnet/minecraft/util/math/Direction;)Z", at = @At("HEAD"), cancellable = true)
    private static void connectsToMixin(BlockState state, @Nullable Direction dir, CallbackInfoReturnable<Boolean> clr)
    {
        if (state.isOf(Registration.REDSTONE_INVERTER_BLOCK) ||
            state.isOf(Registration.REDSTONE_TICKER_BLOCK) ||
            state.isOf(Registration.REDSTONE_TIMER_BLOCK))
        {
            Direction direction = state.get(Properties.FACING);
            clr.setReturnValue(direction == dir || direction.getOpposite() == dir);
        }
    }
}
