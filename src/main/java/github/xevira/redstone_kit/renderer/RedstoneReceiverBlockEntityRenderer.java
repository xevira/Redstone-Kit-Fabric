package github.xevira.redstone_kit.renderer;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.RedstoneReceiverBlock;
import github.xevira.redstone_kit.block.entity.RedstoneReceiverBlockEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

import java.util.Set;

public class RedstoneReceiverBlockEntityRenderer implements BlockEntityRenderer<RedstoneReceiverBlockEntity> {
    private static final Identifier NODE_TEXTURE = RedstoneKit.id("textures/block/redstone_transmitter_node.png");
    private static final Identifier NODE_UNSET_TEXTURE = RedstoneKit.id("textures/block/redstone_transmitter_node_unset.png");

    private static final ModelPart.Cuboid[] NODE_CUBOIDS;

    static {
        NODE_CUBOIDS = new ModelPart.Cuboid[4];

        NODE_CUBOIDS[0] = new ModelPart.Cuboid(0,0, 4.0f, 2.0f, -6.0f, 2.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, 2.0f, 2.0f, Set.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        NODE_CUBOIDS[1] = new ModelPart.Cuboid(0,0, 1.0f, 2.0f, -6.0f, 2.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, 2.0f, 2.0f, Set.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        NODE_CUBOIDS[2] = new ModelPart.Cuboid(0,0, -3.0f, 2.0f, -6.0f, 2.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, 2.0f, 2.0f, Set.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
        NODE_CUBOIDS[3] = new ModelPart.Cuboid(0,0, -6.0f, 2.0f, -6.0f, 2.0f, 1.0f, 2.0f, 0.0f, 0.0f, 0.0f, false, 2.0f, 2.0f, Set.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST));
    }

    private static final float Y = 2.0f / 16.0f;
    private static final float SIZE = 2.0f / 16.0f;

    private static final int UNSET_COLOR = 0x707070;

    private final BlockEntityRendererFactory.Context context;

    private static boolean first = true;

    public RedstoneReceiverBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

        this.context = context;
    }

    @Override
    public void render(RedstoneReceiverBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        if (first) {
            RedstoneKit.LOGGER.info("RedstoneReceiverBlockEntityRenderer.renderer called");
            first = false;
        }

        matrices.push();
        matrices.translate(0.5, 0.0, 0.5);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(switch (entity.getCachedState().get(RedstoneReceiverBlock.FACING)) {
            case EAST -> 270;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        }));


        for(int i = 0; i < RedstoneReceiverBlockEntity.SLOTS; i++)
        {
            DyeColor dye = entity.getNodeColor(i);

            int color;
            VertexConsumer vertexConsumer;
            int l;

            if (dye != null)
            {
                color = dye.getSignColor();
                vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntitySolid(NODE_TEXTURE));
                l = 0xFFFFFF;
            }
            else
            {
                color = 0xFFFFFF;
                vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(NODE_UNSET_TEXTURE));
                l = light;
            }

            NODE_CUBOIDS[i].renderCuboid(matrices.peek(), vertexConsumer, l, overlay, color);
        }

        matrices.pop();
    }

    private void renderSide(
            Matrix4f model, VertexConsumer vertices, float x1, float x2, float y1, float y2, float z1, float z2, int color
    ) {
        vertices.vertex(model, x1, y1, z1).color(color);
        vertices.vertex(model, x2, y1, z1).color(color);
        vertices.vertex(model, x2, y2, z2).color(color);
        vertices.vertex(model, x1, y2, z2).color(color);
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
