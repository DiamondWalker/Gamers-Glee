package gameblock.game.evasion;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

import java.util.ArrayList;

public class BlockBreakGame extends Game {
    float platformPos = 0.0f;
    float ballX, ballY;
    ArrayList<Brick> bricks = new ArrayList<>();

    final KeyBinding up = registerKey(InputConstants.KEY_UP);
    final KeyBinding down = registerKey(InputConstants.KEY_DOWN);
    final KeyBinding left = registerKey(InputConstants.KEY_LEFT);
    final KeyBinding right = registerKey(InputConstants.KEY_RIGHT);

    @Override
    public void tick() {
        if (left.pressed) platformPos -= 4.0f;
        if (right.pressed) platformPos += 4.0f;
        platformPos = Math.max(Math.min(90.0f, platformPos), -90.0f);
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        float renderX = platformPos;
        if (left.pressed) renderX -= (partialTicks * 5.0f);
        if (right.pressed) renderX += (partialTicks * 5.0f);
        drawRectangle(graphics, renderX, -40.0f, 20.0f, 3.0f, 255, 255, 255, 255, 0);
    }

    private class Brick {
        protected int x, y;
        protected int color;

        public Brick(int x, int y, int red, int green, int blue) {
            this.x = x;
            this.y = y;
            this.color = FastColor.ARGB32.color(255, red, green, blue);
        }
    }
}
