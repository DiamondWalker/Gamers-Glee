package gameblock.game.evasion;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

public class EvasionGame extends Game {
    float x = 0.0f;
    float y = 0.0f;
    @Override
    public void tick() {
        y += 0.5f;
        x += 0.5f;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        graphics.fill((int)x - 10, (int)y - 10, (int)x + 10, (int)y + 10, FastColor.ARGB32.color(255, 255, 0, 0));
    }
}
