package gameblock.game.flyingchicken;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.GameblockMod;
import gameblock.game.Game;
import gameblock.registry.GameblockPackets;
import gameblock.util.CircularStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;

import java.util.Random;

public class FlyingChickenGame extends Game {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/flying_chicken.png");
    private static final float HORIZONTAL_MOVEMENT_PER_TICK = 1.5f;
    private static final float SPACE_BETWEEN_PIPES = 30.0f;

    protected float chickenY = 0.0f;
    private float chickenMotion = 0.0f;
    protected long time = 0;
    private long lastFlapTime = Long.MIN_VALUE;
    private long gameOverTime = 0;
    private float gameOverFallDirection = 0.0f;

    final Game.KeyBinding jump = registerKey(InputConstants.KEY_SPACE, this::flap);
    protected final CircularStack<Pipe> pipes = new CircularStack<>(5);

    public FlyingChickenGame(Player player) {
        super(player);
    }

    protected void flap() {
        if (isClientSide()) {
            GameblockPackets.sendToServer(new WingFlapPacket(time, chickenY));
            lastFlapTime = time;
        }
        chickenMotion = 2.56f;
    }

    @Override
    protected void gameOver() {
        super.gameOver();
        gameOverFallDirection = new Random().nextFloat() * 2 - 1;
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            chickenY += chickenMotion;
            float chickenX = calculatePipeOffset(0.0f) - 70;
            chickenMotion -= 0.32f;

            if (!isClientSide()) pipes.forEach((Pipe pipe) -> {
                if (Math.abs(pipe.x - chickenX) - 6 < 12) {
                    if (Math.abs(pipe.y - chickenY) + 5 >= SPACE_BETWEEN_PIPES / 2) {
                        gameOver();
                    }
                }
            });

            if (time % 60 == 0 && !isClientSide()) {
                float x = chickenX + 140;
                float y = (-80.0f + SPACE_BETWEEN_PIPES) + new Random().nextFloat(160.0f - 2f * SPACE_BETWEEN_PIPES);
                pipes.enqueue(new Pipe(x, y));
                GameblockPackets.sendToPlayer((ServerPlayer) player, new PipeSpawnPacket(x, y));
            }

            time++;
        } else {
            gameOverTime++;
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        graphics.fill(-100, -80, 100, 80, FastColor.ARGB32.color(255, 76, 196, 230)); // sky

        final float pipeOffset = calculatePipeOffset(partialTicks);
        pipes.forEach((Pipe pipe) -> {
            drawTexture(graphics, SPRITE,
                    (pipe.x - pipeOffset), pipe.y - (80 + SPACE_BETWEEN_PIPES / 2), 23, 160, 0,
                    233, 0, 23, 160);

            drawTexture(graphics, SPRITE,
                    (pipe.x - pipeOffset), pipe.y + (80 + SPACE_BETWEEN_PIPES / 2), 23, 160, 0,
                    211, 0, 23, 160);
        });

        if (!isGameOver()) {
            drawTexture(graphics, SPRITE,
                    -70, (chickenY + partialTicks * chickenMotion), 12, 10, (float) Math.atan2(chickenMotion / 5, HORIZONTAL_MOVEMENT_PER_TICK),
                    0, time - lastFlapTime < 2 ? 0 : 10, 12, 10);
        } else {
            float deathTime = gameOverTime + partialTicks;
            float y = deathTime / 5 - 2;
            y = -(y * y) + 4;
            y *= 5;
            y += chickenY;

            float rotationOffset = deathTime / 5 * -gameOverFallDirection;
            drawTexture(graphics, SPRITE,
                    -70 + deathTime * gameOverFallDirection, y, 12, 10, (float) Math.atan2(chickenMotion / 5, HORIZONTAL_MOVEMENT_PER_TICK) + rotationOffset,
                    0, time - lastFlapTime < 2 ? 0 : 10, 12, 10);
        }
    }

    private float calculatePipeOffset(float partialTicks) {
        if (isGameOver()) partialTicks = 0.0f;
        return ((float) time + partialTicks) * HORIZONTAL_MOVEMENT_PER_TICK;
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
