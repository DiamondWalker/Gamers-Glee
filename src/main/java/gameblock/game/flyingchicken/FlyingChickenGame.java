package gameblock.game.flyingchicken;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import gameblock.registry.GameblockPackets;
import gameblock.util.CircularStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class FlyingChickenGame extends Game {
    private static final float HORIZONTAL_MOVEMENT_PER_TICK = 1.5f;
    private static final float SPACE_BETWEEN_PIPES = 30.0f;

    protected float chickenY = 0.0f;
    private float chickenMotion = 0.0f;
    protected long time = 0;

    final Game.KeyBinding jump = registerKey(InputConstants.KEY_SPACE, this::flap);
    protected final CircularStack<Pipe> pipes = new CircularStack<>(5);

    public FlyingChickenGame(Player player) {
        super(player);
    }

    protected void flap() {
        if (isClientSide()) GameblockPackets.sendToServer(new WingFlapPacket(time, chickenY));
        chickenMotion = 2.56f;
    }

    @Override
    public void tick() {
        chickenY += chickenMotion;
        chickenMotion -= 0.32f;
        if (time % 60 == 0 && !isClientSide()) {
            float x = calculatePipeOffset(0.0f) + 140;
            float y = (-80.0f + SPACE_BETWEEN_PIPES) + new Random().nextFloat(160.0f - 2f * SPACE_BETWEEN_PIPES);
            pipes.enqueue(new Pipe(x, y));
            GameblockPackets.sendToPlayer((ServerPlayer) player, new PipeSpawnPacket(x, y));
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

    protected static class Pipe {
        float x;
        float y;

        Pipe(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
