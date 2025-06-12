package gameblock.game;

import net.minecraft.client.gui.GuiGraphics;

public abstract class Game {
    public abstract void tick();

    public abstract void render(GuiGraphics graphics, float partialTicks);
}
