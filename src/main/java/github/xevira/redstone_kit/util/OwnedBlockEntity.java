package github.xevira.redstone_kit.util;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface OwnedBlockEntity {
    @Nullable
    UUID getOwner();

    String getOwnerName();

    void setOwner(@Nullable PlayerEntity player);

    boolean isOwner(PlayerEntity player);

    boolean canConfigure(PlayerEntity player);
}
