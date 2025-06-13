package gameblock.game.evasion;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;
import org.lwjgl.glfw.GLFW;

public class EvasionGame extends Game {
    float x = 0.0f;
    float y = 0.0f;

    final KeyBinding up = registerKey(InputConstants.KEY_UP);
    final KeyBinding down = registerKey(InputConstants.KEY_DOWN);
    final KeyBinding left = registerKey(InputConstants.KEY_LEFT);
    final KeyBinding right = registerKey(InputConstants.KEY_RIGHT);

    @Override
    public void tick() {
        if (up.pressed) y += 1.0f;
        if (down.pressed) y -= 1.0f;
        if (left.pressed) x -= 1.0f;
        if (right.pressed) x += 1.0f;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        graphics.fill((int)x - 10, (int)y - 10, (int)x + 10, (int)y + 10, FastColor.ARGB32.color(255, 255, 0, 0));
    }
}
