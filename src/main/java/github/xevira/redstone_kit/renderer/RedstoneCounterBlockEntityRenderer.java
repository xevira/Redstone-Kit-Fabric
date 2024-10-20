package github.xevira.redstone_kit.renderer;

import github.xevira.redstone_kit.RedstoneKit;
import github.xevira.redstone_kit.block.RedstoneCounterBlock;
import github.xevira.redstone_kit.block.entity.RedstoneCounterBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

import java.util.Set;

public class RedstoneCounterBlockEntityRenderer implements BlockEntityRenderer<RedstoneCounterBlockEntity> {
    private static final Identifier BACKGROUND = RedstoneKit.id("textures/block/redstone_counter_grey.png");

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private final BlockEntityRendererFactory.Context context;
    private static final ModelPart.Cuboid CUBOID;

    static {
        CUBOID = new ModelPart.Cuboid(0,0, -8.0f, 0.0f, -8.0f, 16.0f, 0.0f, 16.0f, 0.0f, 0.0f, 0.0f, false, 16.0f, 16.0f, Set.of(Direction.UP));
    }

    public RedstoneCounterBlockEntityRenderer(BlockEntityRendererFactory.Context context)
    {
        this.context = context;
    }

    @Override
    public void render(RedstoneCounterBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {

        matrices.push();
        DyeColor dye = entity.getColor();
        int count = entity.getCount();

        matrices.translate(0.5D, 0.126D, 0.5D);

        VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityCutout(BACKGROUND));

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(switch (entity.getCachedState().get(RedstoneCounterBlock.FACING)) {
            case EAST -> 270;
            case SOUTH -> 180;
            case WEST -> 90;
            default -> 0;
        }));

        int color;
        int textColor;
        int textOutline;
        if (dye == DyeColor.BLACK) {
            color = 0x101010;
            textColor = 0x000000;
            textOutline = 0xFFFFFF;
        }
        else {
            color = dye.getSignColor();
            textColor = dye.getSignColor();
            textOutline = 0x000000;
        }

        //drawQuadPixel(vertexConsumer, matrices.peek(), 3, 2, 3, 13, 2, 13, 3, 3, 13, 13, 0xFFFFFF, color, overlay);
        CUBOID.renderCuboid(matrices.peek(), vertexConsumer, 0xFFFFFF, overlay, color);

        matrices.push();
        // Do text
        Text label = Text.literal(String.valueOf(count));
        int width = context.getTextRenderer().getWidth(label.asOrderedText());
        int height = context.getTextRenderer().fontHeight;
        float scale = Math.min(0.8f / width, 0.02f);
        matrices.scale(scale, scale, scale);
        matrices.translate(0.0D, 0.001D, 0.0D);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180F));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90F));

        context.getTextRenderer().drawWithOutline(label.asOrderedText(), -width * 0.5f, -height * 0.5f, textColor, textOutline, matrices.peek().getPositionMatrix(), vertexConsumers, 0xFFFFFF);

        matrices.pop();
        matrices.pop();
    }

    private static void drawQuadPixel(VertexConsumer vertexConsumer,
                                      MatrixStack.Entry entry,
                                      int x1, int y1, int z1,
                                      int x2, int y2, int z2,
                                      int minU, int minV,
                                      int maxU, int maxV,
                                      int color,
                                      int light, int overlay)
    {
        drawQuad(vertexConsumer, entry, x1 * 0.0625f, y1 * 0.0625f, z1 * 0.0625f, x2 * 0.0625f, y2 * 0.0625f, z2 * 0.0625f, minU * 0.0625f, minV * 0.0625f, maxU * 0.0625f, maxV * 0.0625f, color, light, overlay);
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
