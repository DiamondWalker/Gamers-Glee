package gameblock.game.flyingchicken;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import java.util.Random;

public class FlyingChickenGame extends GameInstance<FlyingChickenGame> {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/flying_chicken.png");
    private static final float HORIZONTAL_MOVEMENT_PER_TICK = 1.5f;
    private static final float SPACE_BETWEEN_PIPES = 30.0f;

    protected float chickenY = 0.0f;
    private float chickenMotion = 0.0f;
    protected long time = 0;
    private int pipesSpawned = 0; // used to ensure the same pipe isn't spawned twice, since wing flap packets can sometimes move the bird back and make another pair spawn
    private long lastFlapTime = Integer.MIN_VALUE;
    private long gameOverTime = 0;
    private float gameOverFallDirection = 0.0f;

    protected int score = 0;
    protected int highScore = 0;
    protected long lastScoreTime = 0;

    final GameInstance.KeyBinding jump = registerKey(InputConstants.KEY_SPACE, this::flap);
    protected final CircularStack<Pipe> pipes = new CircularStack<>(5);

    public FlyingChickenGame(Player player) {
        super(player, GameblockGames.FLYING_CHICKEN_GAME);
    }

    protected void flap() {
        if (isClientSide()) {
            GameblockPackets.sendToServer(new WingFlapPacket(time, chickenY));
            lastFlapTime = time;
            playSound(GameblockSounds.FLYING_CHICKEN_FLAP.get(), 1.0f, 1.0f);
            //playSound(SoundEvents.PHANTOM_FLAP, 1.0f, 350.0f);
        }
        chickenMotion = 2.56f;
    }

    @Override
    protected void onGameLoss() {
        if (isClientSide()) {
            playSound(SoundEvents.CHICKEN_DEATH);
            gameOverFallDirection = new Random().nextFloat() * 2 - 1;
        }
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            chickenY += chickenMotion;
            float chickenX = calculatePipeOffset(0.0f) - 70;
            chickenMotion -= 0.32f;

            if (!isClientSide()) {
                if (Math.abs(chickenY) > 75 - 5) {
                    setGameState(GameState.LOSS);
                } else {
                    pipes.forEach((Pipe pipe) -> {
                        if (Math.abs(pipe.x - chickenX) - 6 < 12 && Math.abs(pipe.y - chickenY) + 5 >= SPACE_BETWEEN_PIPES / 2) {
                            setGameState(GameState.LOSS);
                        } else {
                            if (!pipe.passed && chickenX > (pipe.x - 12)) {
                                score++;
                                pipe.passed = true;
                                sendToAllPlayers(new ScorePacket(score), null);
                            }
                        }
                    });
                }
            }

            if (time / 60 >= pipesSpawned && !isClientSide()) {
                float x = pipesSpawned * 60 * HORIZONTAL_MOVEMENT_PER_TICK + 120;
                float y = (-80.0f + SPACE_BETWEEN_PIPES) + new Random().nextFloat(160.0f - 2f * SPACE_BETWEEN_PIPES);
                pipes.enqueue(new Pipe(x, y));
                sendToAllPlayers(new PipeSpawnPacket(x, y), null);

                pipesSpawned++;
            }

            time++;
        } else {
            gameOverTime++;
        }
    }

    @Override
    protected CompoundTag writeSaveData() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("highScore", Math.max(score, highScore));
        return tag;
    }

    @Override
    protected void readSaveData(CompoundTag tag) {
        highScore = tag.getInt("highScore");
        sendToAllPlayers(new FlyingChickenHighScorePacket(highScore), null);
    }

    @Override
    public Music getMusic() {
        return !isGameOver() ? GameblockMusic.FLYING_CHICKEN : null;
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

        if (!isGameOver()) {
            ColorF col = score > highScore ? new ColorF(1.0f, 1.0f, 0.0f) : new ColorF(1.0f);
            float time = (partialTicks + (getGameTime() - lastScoreTime)) / 15;
            String scoreString = TextUtil.formatWithUnits(score, 3);
            if (lastScoreTime > 0 && time < 1.0f) drawText(graphics, 0.0f, 55.0f, 1.5f + time * 0.65f, col.withAlpha(1.0f - time), Component.literal(scoreString));
            drawText(graphics, 0.0f, 55.0f, 1.5f, col, Component.literal(scoreString));
        } else {
            drawRectangle(graphics, 0.0f, 30.0f, 50.0f, 10.0f, new ColorF(1.0f, 0.5f, 0.0f), 0);
            drawHollowRectangle(graphics, 0.0f, 30.0f, 50.0f, 10.0f, 1.0f, new ColorF(1.0f), 0);
            drawText(graphics, 0.0f, 30.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.flying_chicken.game_over"));
            drawText(graphics, -40.0f, 10.0f, 0.9f, new ColorF(1.0f), Component.translatable("gui.gameblock.flying_chicken.score", score));
            drawText(graphics, 40.0f, 10.0f, 0.9f, new ColorF(1.0f), Component.translatable("gui.gameblock.flying_chicken.best", highScore));
            if (score > highScore) {
                float time = partialTicks + getGameTime();
                float expand = (time % 20) / 20;
                ColorF col = new ColorF(1.0f, 1.0f, 0.0f).fadeTo(new ColorF(1.0f), 1.0f - expand);
                drawText(graphics, 0.0f, 10.0f, 0.7f + expand * 0.2f, col.withAlpha(1.0f - expand), Component.translatable("gui.gameblock.flying_chicken.new_best"));
                drawText(graphics, 0.0f, 10.0f, 0.7f, col, Component.translatable("gui.gameblock.flying_chicken.new_best"));
            }
            ColorF col = overRetryButton(getMouseCoordinates()) ? new ColorF(1.0f, 1.0f, 0.0f) : new ColorF(1.0f);
            drawText(graphics, 0.0f, -20.0f, 0.9f, col, Component.translatable("gui.gameblock.flying_chicken.retry"));
        }


    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT && overRetryButton(clickCoordinates)) restart();
    }

    private boolean overRetryButton(Vec2 coordinates) {
        return Math.abs(coordinates.x) < 15 && Math.abs(coordinates.y - -20) < 5;
    }

    private float calculatePipeOffset(float partialTicks) {
        if (isGameOver()) partialTicks = 0.0f;
        return ((float) time + partialTicks) * HORIZONTAL_MOVEMENT_PER_TICK;
    }

    protected static class Pipe {
        float x;
        float y;
        boolean passed = false;

        Pipe(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
}
