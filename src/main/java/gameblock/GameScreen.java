package gameblock;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import gameblock.game.Game;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class GameScreen extends Screen {
    private final Game game;

    protected GameScreen(Game game) {
        super(GameNarrator.NO_TITLE);
        this.game = game;
    }

    @Override
    public void tick() {
        game.tick();
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float partialTicks) {
        this.renderBackground(graphics);
        RenderSystem.disableCull();
        graphics.enableScissor(this.width / 6, this.height / 6, 5 * this.width / 6, 5 * this.height / 6);
        graphics.fill(0, 0, width, height, FastColor.ARGB32.color(255, 0, 0, 0));
        PoseStack stack = graphics.pose();
        stack.pushPose();
        stack.translate(0.5 * width, 0.5 * height, 0.0);
        stack.scale(100.0f / Math.min(width, height), 100.0f / Math.min(width, height), 1.0f);
        stack.scale(1.0f, -1.0f, 1.0f);
        game.render(graphics, partialTicks);
        stack.popPose();
        super.render(graphics, p_281550_, p_282878_, partialTicks);
        graphics.disableScissor();
        RenderSystem.enableCull();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
