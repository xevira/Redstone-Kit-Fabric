package github.xevira.redstone_kit.mixin;

import github.xevira.redstone_kit.util.IItemEntityMixin;
import net.minecraft.entity.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemEntity.class)
public class ItemEntityMixin implements IItemEntityMixin {

    @Shadow
    private int itemAge;


    @Override
    public void setItemAge(int age) {
        this.itemAge = age;
    }
}
