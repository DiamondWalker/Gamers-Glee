package gameblock.game;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

public class GameblockOS extends Game {
    public GameblockOS(Player player) {
        super(player);
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawRectangle(graphics, 0, 0, 100, 100, 0, 0, 0, 255, 0);
        drawText(graphics, 0, 0, "Cartridge not found.");
    }
}
