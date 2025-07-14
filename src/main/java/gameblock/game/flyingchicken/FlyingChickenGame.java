package gameblock.game.flyingchicken;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import gameblock.util.CircularStack;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Random;

public class FlyingChickenGame extends Game {
    private static final float HORIZONTAL_MOVEMENT_PER_TICK = 1.5f;
    private static final float SPACE_BETWEEN_PIPES = 30.0f;

    private float chickenY = 0.0f;
    private float chickenMotion = 0.0f;
    private long time = 0;

    final Game.KeyBinding jump = registerKey(InputConstants.KEY_SPACE, () -> chickenMotion = 2.56f);
    final CircularStack<Pipe> pipes = new CircularStack<>(5);

    @Override
    public void tick() {
        chickenY += chickenMotion;
        chickenMotion -= 0.32f;
        if (time % 60 == 0) {
            pipes.enqueue(new Pipe(calculatePipeOffset(0.0f) + 100, (-80.0f + SPACE_BETWEEN_PIPES) + new Random().nextFloat(160.0f - 2f * SPACE_BETWEEN_PIPES)));
        }

        time++;
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawRectangle(graphics,
                -70, (chickenY + partialTicks * chickenMotion) - 40, 10, 8,
                255, 255, 0, 255, 0);

        final float pipeOffset = calculatePipeOffset(partialTicks);
        pipes.forEach((Pipe pipe) -> {
            drawRectangle(graphics,
                    (pipe.x - pipeOffset), pipe.y - (80 + SPACE_BETWEEN_PIPES / 2), 23, 160,
                    0, 255, 0, 255, 0);

            drawRectangle(graphics,
                    (pipe.x - pipeOffset), pipe.y + (80 + SPACE_BETWEEN_PIPES / 2), 23, 160,
                    0, 255, 0, 255, 0);
        });
    }

    private float calculatePipeOffset(float partialTicks) {
        return ((float) time + partialTicks)  * HORIZONTAL_MOVEMENT_PER_TICK;
    }

    private class Pipe {
        float x;
        float y;

        private Pipe(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
