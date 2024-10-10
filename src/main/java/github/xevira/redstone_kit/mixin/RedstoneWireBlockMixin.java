package github.xevira.redstone_kit.mixin;

import github.xevira.redstone_kit.Registration;
import github.xevira.redstone_kit.util.RedstoneConnect;
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
        if (state.getBlock() instanceof RedstoneConnect connecter)
        {
            switch(connecter.getRedstoneConnect())
            {
                case ALWAYS -> clr.setReturnValue(true);
                case AXIS -> {
                    // Only allow along the axis of the FACING direction
                    Direction direction = state.get(Properties.HORIZONTAL_FACING);
                    clr.setReturnValue(direction == dir || direction.getOpposite() == dir);
                }
                case NOT_FACING -> {
                    // Any direction other than FACING.
                    Direction direction = state.get(Properties.HORIZONTAL_FACING);
                    clr.setReturnValue(direction.getOpposite() != dir);   // Back side should not connect
                }
            }
        }
    }
}
