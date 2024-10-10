package github.xevira.redstone_kit.mixin;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.poi.TeleportInhibitors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EndermanEntity.class)
public class EndermanEntityMixin {

    @Inject(method = "teleportTo(DDD)Z", at = @At("HEAD"), cancellable = true)
    void teleportToMixin(double x, double y, double z, CallbackInfoReturnable<Boolean> clr)
    {
        // Check current location
        if (!TeleportInhibitors.isValidEndermanTeleport(((EndermanEntity)(Object)this)))
        {
            clr.setReturnValue(false);
        }

        // Check the destination
        if (!TeleportInhibitors.isValidEndermanTeleport(((EndermanEntity)(Object)this).getWorld(), x, y, z))
        {
            clr.setReturnValue(false);
        }
    }
}
