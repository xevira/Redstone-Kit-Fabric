package github.xevira.redstone_kit.renderer;

import github.xevira.redstone_kit.block.entity.TeleportInhibitorBlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class TeleportInhibitorBlockEntityRenderer implements BlockEntityRenderer<TeleportInhibitorBlockEntity> {
    private final BlockEntityRendererFactory.Context context;
    private float eyeYaw;

    public TeleportInhibitorBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.context = context;

        Random random = Random.create();
        this.eyeYaw = random.nextFloat() * 10000.0f;
    }

    @Override
    public void render(TeleportInhibitorBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.hasCharge()) return;

        matrices.push();
        matrices.translate(0.5, 1.0, 0.5);
        matrices.scale(0.325f, 0.325f, 0.325f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(eyeYaw));

        ItemStack stack = new ItemStack(Items.ENDER_EYE);
        this.context.getItemRenderer().renderItem(stack, ModelTransformationMode.FIXED,
                light, overlay,
                matrices, vertexConsumers,
                entity.getWorld(), 0);

        matrices.pop();

        eyeYaw += tickDelta;
    }
}
