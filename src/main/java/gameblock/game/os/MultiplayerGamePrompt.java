package gameblock.game.os;

import gameblock.game.GamePrompt;
import gameblock.util.ColorF;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class MultiplayerGamePrompt extends GamePrompt<GameblockOS> {
    public MultiplayerGamePrompt(GameblockOS game) {
        super(game);
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        game.drawRectangle(graphics, 0, 0, 100, 25, new ColorF(0.0f), 0);
        game.drawText(graphics, 0, 0, 1.0f, new ColorF(1.0f), Component.literal(get()));
    }
}
