package github.xevira.redstone_kit.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BeaconBlockEntity.BeamSegment.class)
public class BeamSegmentMixin implements IBeamSegmentMixin {

    @Shadow
    private int height;

    @Override
    public void grow() {
        this.height++;
    }
}
