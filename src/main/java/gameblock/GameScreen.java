package gameblock;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

public class GameScreen extends Screen {
    public static final ResourceLocation BACKGROUND_LOCATION = new ResourceLocation("textures/gui/options_background.png");

    protected GameScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public void render(GuiGraphics graphics, int p_281550_, int p_282878_, float partialTicks) {
        this.renderBackground(graphics);
        graphics.enableScissor(this.width / 4, this.height / 4, 3 * this.width / 4, 3 * this.height / 4);
        graphics.fill(0, 0, width, height, FastColor.ARGB32.color(255, 0, 0, 0));
        super.render(graphics, p_281550_, p_282878_, partialTicks);
        graphics.disableScissor();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
