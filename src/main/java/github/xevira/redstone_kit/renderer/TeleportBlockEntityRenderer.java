package github.xevira.redstone_kit.renderer;

import github.xevira.redstone_kit.block.entity.TeleporterBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;

public class TeleportBlockEntityRenderer implements BlockEntityRenderer<TeleporterBlockEntity> {

    public TeleportBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(TeleporterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (!entity.isLinked() || !entity.isActive()) return;

        float y = 2.0f / 16.0f;

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(this.getLayer());

        this.renderSide(entity, matrices.peek().getPositionMatrix(), vertexConsumer, 0.25F, 0.75F, y, y, 0.75F, 0.75F, 0.25F, 0.25F, Direction.UP);
        //drawQuad(vertexConsumer, matrices.peek(), 0.25f, 0.25f, 0.25f, 0.75f, 0.25f, 0.75f, 0.25f, 0.25f, 0.75f, 0.75f, 0xFFFFFF, light, overlay);
    }

    protected RenderLayer getLayer() {
        return RenderLayer.getEndPortal();
    }

    private void renderSide(
            TeleporterBlockEntity entity, Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, float z3, float z4, Direction side
    ) {
        vertices.vertex(model, x1, y1, z1);
        vertices.vertex(model, x2, y1, z2);
        vertices.vertex(model, x2, y2, z3);
        vertices.vertex(model, x1, y2, z4);
    }

    private static void drawQuad(VertexConsumer vertexConsumer,
                                 MatrixStack.Entry entry,
                                 float x1, float y1, float z1,
                                 float x2, float y2, float z2,
                                 float minU, float minV,
                                 float maxU, float maxV,
                                 int color,
                                 int light, int overlay) {
        vertexConsumer.vertex(entry, x1, y1, z1)
                .color(color)
                .texture(minU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x1, y2, z1)
                .color(color)
                .texture(minU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x2, y2, z2)
                .color(color)
                .texture(maxU, maxV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);

        vertexConsumer.vertex(entry, x2, y1, z2)
                .color(color)
                .texture(maxU, minV)
                .light(light)
                .overlay(overlay)
                .normal(0.0F, 1.0F, 0.0F);
    }
}
