package lzhong.net.lzh.CustomCommand;

import com.mojang.blaze3d.systems.RenderSystem;
import lzhong.net.ClientInitialiser;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class BlockOutline {
    public static void registerRenderEvent() {
        HudRenderCallback.EVENT.register((drawContext, tickDeltaManager)->{
            BlockPos highlightPos = ClientInitialiser.getHighlightBlockPos(); // Assume you're tracking the block to highlight
            if (highlightPos != null) {
                renderBlockOutline(drawContext,highlightPos,tickDeltaManager);
            }
        });
    }

    private static void renderBlockOutline(DrawContext drawContext, BlockPos highlightPos, RenderTickCounter tickDeltaManager) {
// Setup transformation matrix from draw context
        Matrix4f transformationMatrix = drawContext.getMatrices().peek().getPositionMatrix();

        // Use Tessellator and BufferBuilder to start rendering
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        // Define the vertices for the block corners
        Vec3d[] vertices = {
                new Vec3d(0, 0, 0), new Vec3d(1, 0, 0), new Vec3d(1, 1, 0), new Vec3d(0, 1, 0), // Bottom face
                new Vec3d(0, 0, 1), new Vec3d(1, 0, 1), new Vec3d(1, 1, 1), new Vec3d(0, 1, 1)  // Top face
        };

        // Add vertices to buffer with color
        for (Vec3d vertex : vertices) {
            Vector3f transformedVertex = new Vector3f((float) (vertex.x + highlightPos.getX()),
                    (float) (vertex.y + highlightPos.getY()),
                    (float) (vertex.z + highlightPos.getZ()));
            transformedVertex.mulPosition(transformationMatrix);
            buffer.vertex(transformationMatrix, transformedVertex.get(0), transformedVertex.get(1), transformedVertex.get(2))
                    .color(0xFF000000) // Set the color (green in this case)
                    ;
            System.out.println(Text.literal(transformedVertex.get(0) + " " + transformedVertex.get(1) + " " + transformedVertex.get(2)));
        }

        // Finish drawing the outline
        // We'll get to this bit in the next section.
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

}