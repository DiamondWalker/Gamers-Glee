package gameblock.game;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gameblock.util.ColorF;
import gameblock.util.TextRenderingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import org.joml.Matrix4f;

import java.util.function.Consumer;

public class RenderManager {
    private GuiGraphics graphics = null;
    private PoseStack pose = null;

    private void verifyRender() {
        if (graphics == null) throw new IllegalStateException("Frame is not being rendered");
    }

    public void prepareRender(GuiGraphics graphics) {
        if (this.graphics != null) throw new IllegalStateException("Frame is already being rendered");
        this.graphics = graphics;
        pose = graphics.pose();
    }

    public void endRender() {
        verifyRender();
        graphics = null;
        pose = null;
    }

    public void doDirectRendering(Consumer<GuiGraphics> rendering) {
        verifyRender();
        rendering.accept(graphics);
    }

    public void fill(ColorF color) {
        graphics.fill(-GameInstance.MAX_X, -GameInstance.MAX_Y, GameInstance.MAX_X, GameInstance.MAX_Y, FastColor.ARGB32.color(Math.round(color.getAlpha() * 255), Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255)));
    }

    public void push() {
        verifyRender();
        pose.pushPose();
    }

    public void pop() {
        verifyRender();
        pose.popPose();
    }

    public void translate(float x, float y) {
        verifyRender();
        pose.translate(x, y, 0);
    }

    public void scale(float scale) {
        scale(scale, scale);
    }

    public void scale(float x, float y) {
        verifyRender();
        pose.scale(x, y, 1.0f);
    }

    public void rotateRadians(float radians) {
        pose.mulPose(Axis.ZP.rotation(radians));
    }

    public void rotateDegrees(float degrees) {
        pose.mulPose(Axis.ZP.rotationDegrees(degrees));
    }

    private void drawText(float x, float y, float scale, Component txt, ColorF color) {
        verifyRender();

        Font font = Minecraft.getInstance().font;
        float width = font.width(txt);
        float height = font.lineHeight;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x - scale * (width / 2 - 0.5f), y + scale * (height / 2 - 1), 0);
        pose.scale(scale, -scale, 1.0f);

        graphics.drawString(font, txt, 0, 0, FastColor.ARGB32.color(Math.round(color.getAlpha() * 255), Math.round(color.getRed() * 255), Math.round(color.getGreen() * 255), Math.round(color.getBlue() * 255)), false);

        pose.popPose();
    }

    private void drawText(float x, float y, float scale, Component[] txt, ColorF colorF) {
        verifyRender();

        Font font = Minecraft.getInstance().font;
        float height = font.lineHeight * txt.length + 2 * (txt.length - 1);

        int alpha = Math.round(colorF.getAlpha() * 255);
        if (alpha > 3) { // Minecraft text rendering bugs out at low alpha levels
            int red = Math.round(colorF.getRed() * 255);
            int green = Math.round(colorF.getGreen() * 255);
            int blue = Math.round(colorF.getBlue() * 255);
            int color = FastColor.ARGB32.color(alpha, red, green, blue);

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate(x, y + scale * (height / 2 - 1), 0);
            pose.scale(scale, -scale, 1.0f);

            for (Component line : txt) {
                float width = font.width(line);
                pose.pushPose();
                pose.translate(-width / 2 + 0.5f, 0.0f, 0.0f);
                graphics.drawString(font, line, 0, 0, color, false);
                pose.popPose();
                pose.translate(0.0f, font.lineHeight + 2, 0.0f);
            }
            pose.popPose();
        }
    }

    public final void drawText(float x, float y, float scale, int maxWidth, int maxLines, ColorF color, Component txt) {
        verifyRender();

        TextRenderingRules rules = new TextRenderingRules().setMaxWidth(maxWidth).setMaxLines(maxLines);
        drawText(x, y, scale, rules.splitIntoLines(Minecraft.getInstance().font, txt), color);
    }

    public final void drawText(float x, float y, float scale, ColorF color, Component... lines) {
        drawText(x, y, scale, lines, color);
    }

    public final void drawRectangle(float x, float y, float width, float height, ColorF color, float angle) {
        drawRectangle(RenderType.gui(), x, y, width, height, color, angle);
    }

    public final void drawRectangle(RenderType type, float x, float y, float width, float height, ColorF color, float angle) {
        verifyRender();

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f matrix4f = pose.last().pose();

        float pMinX = -width / 2;
        float pMaxX = width / 2;
        float pMinY = -height / 2;
        float pMaxY = height / 2;

        if (pMinX < pMaxX) {
            float i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY) {
            float j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(type);
        vertexconsumer.vertex(matrix4f, pMinX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        graphics.flush();

        pose.popPose();
    }

    public final void drawHollowRectangle(float x, float y, float width, float height, float thickness, ColorF color, float angle) {
        drawHollowRectangle(RenderType.gui(), x, y, width, height, thickness, color, angle);
    }

    public final void drawHollowRectangle(RenderType type, float x, float y, float width, float height, float thickness, ColorF color,  float angle) {
        verifyRender();

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f matrix4f = pose.last().pose();

        float pMinOuterX = -width / 2;
        float pMaxOuterX = width / 2;
        float pMinOuterY = -height / 2;
        float pMaxOuterY = height / 2;

        if (pMinOuterX < pMaxOuterX) {
            float i = pMinOuterX;
            pMinOuterX = pMaxOuterX;
            pMaxOuterX = i;
        }

        if (pMinOuterY < pMaxOuterY) {
            float j = pMinOuterY;
            pMinOuterY = pMaxOuterY;
            pMaxOuterY = j;
        }

        float pMinInnerX = pMinOuterX + thickness;
        float pMaxInnerX = pMaxOuterX - thickness;
        float pMinInnerY = pMinOuterY + thickness;
        float pMaxInnerY = pMaxOuterY - thickness;

        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(type);

        // left
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // top
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // right
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMaxOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMaxInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        // bottom
        vertexconsumer.vertex(matrix4f, pMaxOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinOuterX, pMinOuterY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMinInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix4f, pMaxInnerX, pMinInnerY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).endVertex();

        graphics.flush();

        pose.popPose();
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight) {
        drawTexture(texture, x, y, width, height, angle, u, v, uWidth, vHeight, new ColorF(1.0f));
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle) {
        drawTexture(texture, x, y, width, height, angle, new ColorF(1.0f));
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight, ColorF color) {
        verifyRender();

        float minU = (float) u / 256;
        float minV = (float) v / 256;
        float maxU = (float) (u + uWidth) / 256;
        float maxV = (float) (v + vHeight) / 256;
        drawTexture(texture, x, y, width, height, angle, minU, maxU, minV, maxV, color);
    }

    public final void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, ColorF color) {
        drawTexture(texture, x, y, width, height, angle, 0.0f, 1.0f, 0.0f, 1.0f, color);
    }

    private void drawTexture(ResourceLocation texture, float x, float y, float width, float height, float angle, float minU, float maxU, float minV, float maxV, ColorF color) {
        if (graphics == null) throw new IllegalStateException("Frame is not being rendered");

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionColorTexShader);
        RenderSystem.enableBlend();
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0.0f);
        pose.mulPose(Axis.ZP.rotation(angle));
        Matrix4f matrix4f = pose.last().pose();

        float pMinX = -width / 2;
        float pMaxX = width / 2;
        float pMinY = -height / 2;
        float pMaxY = height / 2;

        if (pMinX < pMaxX) {
            float i = pMinX;
            pMinX = pMaxX;
            pMaxX = i;
        }

        if (pMinY < pMaxY) {
            float j = pMinY;
            pMinY = pMaxY;
            pMaxY = j;
        }

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
        bufferbuilder.vertex(matrix4f, pMinX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(maxU, minV).endVertex();
        bufferbuilder.vertex(matrix4f, pMinX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, pMaxX, pMaxY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(matrix4f, pMaxX, pMinY, 0.0f).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()).uv(minU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());

        pose.popPose();
    }
}
