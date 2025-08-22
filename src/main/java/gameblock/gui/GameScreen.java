package gameblock.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import gameblock.game.GameInstance;
import gameblock.game.os.GameblockOS;
import gameblock.packet.GameChangePacket;
import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.packet.GameClosePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.Direction1D;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.openal.AL10;

public class GameScreen extends Screen {
    public static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.fromNamespaceAndPath(GameblockMod.MODID, "textures/gui/gameblock.png");
    private static final int IMAGE_WIDTH = 206;
    private static final int IMAGE_HEIGHT = 208;

    private final GameInstance<?> game;

    public GameScreen(GameInstance game) {
        super(GameNarrator.NO_TITLE);
        this.game = game;
    }

    public GameInstance getGame() {
        return game;
    }

    @Override
    public void tick() {
        super.tick();
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                if (cap.getGame() != null) return;
            }
        }
        game.soundManager.update();
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float partialTicks) {
        this.renderBackground(graphics);
        RenderSystem.disableCull();
        int i = (this.width - IMAGE_WIDTH) / 2;
        int j = this.height - IMAGE_HEIGHT;
        int frameMinX = i + 3, frameMaxX = i + IMAGE_WIDTH - 3, frameMinY = j + 3, frameMaxY = j + IMAGE_WIDTH - 53;
        graphics.enableScissor(frameMinX, frameMinY, frameMaxX, frameMaxY);
        graphics.fill(0, 0, width, height, FastColor.ARGB32.color(255, 0, 0, 0));
        PoseStack stack = graphics.pose();
        stack.pushPose();
        int frameWidth = frameMaxX - frameMinX;
        int frameHeight = frameMaxY - frameMinY;
        float scale = (float)frameWidth / 200;
        stack.translate((frameMinX + frameMaxX) / 2, (frameMinY + frameMaxY) / 2, 0.0);
        stack.scale(scale, -scale, 1.0f);
        game.render(graphics, partialTicks);
        if (game.prompt != null) game.prompt.render(graphics, partialTicks);
        stack.popPose();
        super.render(graphics, p_281550_, p_282878_, partialTicks);
        graphics.disableScissor();
        RenderSystem.enableCull();
        graphics.blit(TEXTURE_LOCATION, i, j, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    private Vec2 convertMouseCoordinatesToGameCoordinates(double mouseX, double mouseY) {
        int i = (this.width - IMAGE_WIDTH) / 2;
        int j = this.height - IMAGE_HEIGHT;
        int frameMinX = i + 3, frameMaxX = i + IMAGE_WIDTH - 3, frameMinY = j + 3, frameMaxY = j + IMAGE_WIDTH - 53;
        int centerX = (frameMinX + frameMaxX) / 2, centerY = (frameMinY + frameMaxY) / 2;
        float scale = (float)(frameMaxX - frameMinX) / 200;

        mouseX -= centerX;
        mouseY -= centerY;
        mouseX *= scale;
        mouseY *= -scale;

        return new Vec2((float)mouseX, (float)mouseY);
    }

    @Override
    public void mouseMoved(double pMouseX, double pMouseY) {
        game.setMouseCoordinates(convertMouseCoordinatesToGameCoordinates(pMouseX, pMouseY));
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        Vec2 gameCoords = convertMouseCoordinatesToGameCoordinates(pMouseX, pMouseY);
        Direction1D button = null;
        switch (pButton) {
            case (InputConstants.MOUSE_BUTTON_LEFT) -> button = Direction1D.LEFT;
            case (InputConstants.MOUSE_BUTTON_RIGHT) -> button = Direction1D.RIGHT;
            case (InputConstants.MOUSE_BUTTON_MIDDLE) -> button = Direction1D.CENTER;
        }

        if (game.prompt != null && game.prompt.click(gameCoords)) return true;
        game.click(gameCoords, button);
        return true;
    }

    private Music currentMusic = null;

    @Nullable
    @Override
    public Music getBackgroundMusic() {
        Music newMusic = game.getMusic();
        MusicManager manager = minecraft.getMusicManager();
        if (newMusic == null || !manager.isPlayingMusic(newMusic)) {
            manager.stopPlaying();
        }
        currentMusic = newMusic;
        return newMusic;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (game.prompt != null && game.prompt.handleKeyPress(pKeyCode)) return true;
        if (game.pressKey(pKeyCode)) return true;
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (game.prompt != null) game.prompt.handleCharTyped(pCodePoint);
        return super.charTyped(pCodePoint, pModifiers);
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        if (game.releaseKey(pKeyCode)) return true;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        GameblockPackets.sendToServer(new GameClosePacket());
        game.soundManager.stopAll();
        if (currentMusic != null) minecraft.getMusicManager().stopPlaying(currentMusic);
    }
}
