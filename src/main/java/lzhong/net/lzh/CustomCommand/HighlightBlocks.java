package lzhong.net.lzh.CustomCommand;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lzhong.net.ClientInitialiser;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class HighlightBlocks {
    public static void registerRenderEvent() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            BlockPos highlightPos = ClientInitialiser.getHighlightBlockPos(); // Assume you're tracking the block to highlight
            //MainInitialiser.LOGGER.info("WorldRenderEvents.BEFORE_DEBUG_RENDER triggered");
            if (highlightPos != null) {
                renderBlockOutline(context.matrixStack(), highlightPos, context.camera().getPos());

            }
        });
    }

    private static void renderBlockOutline(MatrixStack matrixStack, BlockPos pos, Vec3d cameraPos) {
        // Create a bounding box for the block and offset it based on the camera position
        Box box = new Box(pos).expand(0.002).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        float red = ClientInitialiser.getRed();
        float green = ClientInitialiser.getGreen();
        float blue = ClientInitialiser.getBlue();
        float alpha = ClientInitialiser.getAlpha();


        // Disable depth testing to ensure the outline is always visible, even behind other blocks
        // Setup rendering
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);

        // Enable blending for transparency
        RenderSystem.defaultBlendFunc();

        // Set the shader and line width
        RenderSystem.setShader(GameRenderer::getRenderTypeLinesProgram);
        RenderSystem.lineWidth(1.0F);

        // Render the outline using a VertexConsumerProvider
        VertexConsumerProvider.Immediate buffer = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderLayer.getLines());

        // Render the block outline (red color, fully opaque)
        DebugRenderer.drawBox(matrixStack, buffer, box, red , green, blue, alpha);

        //DebugRenderer.drawBlockBox(matrixStack,buffer,pos,0.5f,0.5f,0.5f,0.5f);



        // Draw the buffer to finish rendering
        buffer.draw();

        // Re-enable depth mask and depth test after rendering
        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}