package gameblock.game;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import gameblock.game.os.GameblockOS;
import gameblock.packet.EndGamePacket;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.gui.GameScreen;
import gameblock.item.CartridgeItem;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.ColorF;
import gameblock.util.Direction1D;
import gameblock.util.TextRenderingRules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.FastColor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class GameInstance {
    private final HashMap<Integer, KeyBinding> keyBindings = new HashMap<>();
    private Vec2 mouseCoordinates = new Vec2(Float.NaN, Float.NaN);

    protected final Player player;

    public static final int MAX_X = 100;
    public static final int MAX_Y = 75;

    private long gameTime = 0;
    private boolean gameOver = false;

    public final ArrayList<SimpleSoundInstance> sounds = new ArrayList<>();

    public GameInstance(Player player) {
        this.player = player;
    }

    public boolean isClientSide() {
        return player.level().isClientSide();
    }

    protected void gameOver() {
        gameOver = true;
        if (!isClientSide()) GameblockPackets.sendToPlayer((ServerPlayer) player, new GameOverPacket());
    }

    public long getGameTime() {
        return gameTime;
    }

    public void setMouseCoordinates(Vec2 coords) {
        this.mouseCoordinates = coords;
    }

    protected Vec2 getMouseCoordinates() {
        return mouseCoordinates;
    }

    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {}

    protected boolean isGameOver() {
        return gameOver;
    }

    public final void baseTick() {
        tick();
        gameTime++;

        if (
                player.getItemInHand(InteractionHand.MAIN_HAND).is(GameblockItems.GAMEBLOCK.get()) &&
                player.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CartridgeItem cartridge &&
                cartridge.isInstance(this)) {
            return;
        } else if (!(this instanceof GameblockOS)) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null && cap.isPlaying()) {
                cap.setGame(null, isClientSide());
                if (isClientSide()) {
                    if (Minecraft.getInstance().screen instanceof GameScreen screen) screen.onClose();
                    GameblockPackets.sendToServer(new EndGamePacket());
                } else {
                    GameblockPackets.sendToPlayer((ServerPlayer) player, new EndGamePacket());
                }
            }
        }
    }

    protected abstract void tick();

    public abstract void render(GuiGraphics graphics, float partialTicks);

    public Music getMusic() {
        return null;
    }

    public final void playSound(SoundEvent event, float pitch, float volume) {
        if (isClientSide()) {
            SimpleSoundInstance sound = SimpleSoundInstance.forUI(event, pitch, volume);
            Minecraft.getInstance().getSoundManager().play(sound);
            sounds.add(sound);
        }
    }

    public final void playSound(SoundEvent event) {
        playSound(event, 1.0f, 1.0f);
    }

    private void drawText(GuiGraphics graphics, float x, float y, float scale, String txt, ColorF color) {
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

    private void drawText(GuiGraphics graphics, float x, float y, float scale, String[] txt, ColorF colorF) {
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

            for (String line : txt) {
                MutableComponent translatedLine = Component.translatable(line);
                float width = font.width(translatedLine);
                pose.pushPose();
                pose.translate(-width / 2 + 0.5f, 0.0f, 0.0f);
                graphics.drawString(font, translatedLine, 0, 0, color, false);
                pose.popPose();
                pose.translate(0.0f, font.lineHeight + 2, 0.0f);
            }
            pose.popPose();
        }
    }

    public final void drawText(GuiGraphics graphics, float x, float y, float scale, int maxWidth, int maxLines, ColorF color, String txt) {
        TextRenderingRules rules = new TextRenderingRules().setMaxWidth(maxWidth).setMaxLines(maxLines);
        drawText(graphics, x, y, scale, rules.splitIntoLines(Minecraft.getInstance().font, txt), color);
    }

    public final void drawText(GuiGraphics graphics, float x, float y, float scale, ColorF color, String... lines) {
        drawText(graphics, x, y, scale, lines, color);
    }

    public final void drawRectangle(GuiGraphics graphics, float x, float y, float width, float height, ColorF color, float angle) {
        drawRectangle(graphics, RenderType.gui(), x, y, width, height, color, angle);
    }

    public final void drawRectangle(GuiGraphics graphics, RenderType type, float x, float y, float width, float height, ColorF color, float angle) {
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

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight) {
        drawTexture(graphics, texture, x, y, width, height, angle, u, v, uWidth, vHeight, new ColorF(1.0f));
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle) {
        drawTexture(graphics, texture, x, y, width, height, angle, new ColorF(1.0f));
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, int u, int v, int uWidth, int vHeight, ColorF color) {
        float minU = (float) u / 256;
        float minV = (float) v / 256;
        float maxU = (float) (u + uWidth) / 256;
        float maxV = (float) (v + vHeight) / 256;
        drawTexture(graphics, texture, x, y, width, height, angle, minU, maxU, minV, maxV, color);
    }

    public final void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, ColorF color) {
        drawTexture(graphics, texture, x, y, width, height, angle, 0.0f, 1.0f, 0.0f, 1.0f, color);
    }

    private void drawTexture(GuiGraphics graphics, ResourceLocation texture, float x, float y, float width, float height, float angle, float minU, float maxU, float minV, float maxV, ColorF color) {
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
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

    protected final KeyBinding registerKey(int key) {
        KeyBinding binding = new KeyBinding();
        keyBindings.put(key, binding);
        return binding;
    }

    protected final KeyBinding registerKey(int key, Runnable pressAction) {
        KeyBinding binding = new KeyBinding(pressAction);
        keyBindings.put(key, binding);
        return binding;
    }

    public final boolean pressKey(int key) {
        if (isGameOver()) return false;
        KeyBinding binding = keyBindings.get(key);
        if (binding != null) {
            if (binding.pressAction != null) binding.pressAction.run();
            binding.pressed = true;
            return true;
        }
        return false;
    }

    public final boolean releaseKey(int key) {
        if (isGameOver()) return false;
        if (keyBindings.containsKey(key)) {
            keyBindings.get(key).pressed = false;
            return true;
        }
        return false;
    }

    protected static class KeyBinding {
        public boolean pressed = false;
        public Runnable pressAction;

        protected KeyBinding() {
            this(null);
        }

        protected KeyBinding(Runnable action) {
            this.pressAction = action;
        }
    }
}
