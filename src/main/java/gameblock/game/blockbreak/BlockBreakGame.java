package gameblock.game.blockbreak;

import com.mojang.blaze3d.vertex.VertexConsumer;
import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.blockbreak.packets.BallLaunchPacket;
import gameblock.game.blockbreak.packets.BallUpdatePacket;
import gameblock.game.blockbreak.packets.BlockUpdatePacket;
import gameblock.game.blockbreak.packets.ScoreUpdatePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockMusic;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.*;
import gameblock.util.datastructure.CircularStack;
import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Direction1D;
import gameblock.util.rendering.TextUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockBreakGame extends GameInstance<BlockBreakGame> {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/block_break.png");

    private static final int UPDATES_PER_TICK = 30;

    private static final float PLATFORM_Y = -50.0f;
    private static final float PLATFORM_WIDTH = 20.0f;
    private static final float PLATFORM_HEIGHT = 3.0f;

    private static final float BALL_WIDTH = 2.0f;
    private static final float INITIAL_BALL_SPEED = 3.0f;
    private static final float BALL_SPEED_INCREASE = 6.0f;

    private static final float BALL_START_Y = PLATFORM_Y + PLATFORM_HEIGHT / 2 + BALL_WIDTH / 2;

    private static final float BRICK_WIDTH = 10.0f;
    private static final float BRICK_HEIGHT = 5.0f;

    protected static final int BRICK_BREAK_FLASH_TIME = 30;

    private static final int MAX_PACKET_INTERVAL = 10;

    float platformPos = 0.0f;
    float oldPlatformPos = platformPos;

    final BlockBreakParticleManager particleManager = isClientSide() ? new BlockBreakParticleManager(this) : null;

    public float ballX = 0.0f, ballY = BALL_START_Y;
    public float oldBallX = ballX, oldBallY = ballY;
    public float ballMoveX, ballMoveY;

    CircularStack<Vector2f> ballPath = null;

    boolean ballLaunched = false;
    public long timeSinceLaunch = 0;

    public final ArrayList<Block> blocks = new ArrayList<>();
    public int blocksBroken;

    public long lastPacketTime = 0; // every so often packets should be sent to ensure everything is synced

    public int score = 0;
    public int highScore = 0;

    public long clientToPacketBallUpdateTime = -1;

    public BlockBreakGame(Player player) {
        super(player, GameblockGames.BLOCK_BREAK_GAME);
        int col = 0;
        if (!isClientSide()) {
            for (int y = 6; y >= 3; y--) {
                if (y < 6) {
                    for (int x = -14; x <= 14; x += 2) {
                        blocks.add(new Block(x, y * 2 - 1 + 1, col));
                    }
                    col++;
                }
                for (int x = -15; x <= 15; x += 2) {
                    blocks.add(new Block(x, y * 2 - 1, col));
                }
                col++;
            }
        } else {
            ballPath = new CircularStack<>(5);
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeShort(blocks.size());
        for (Block block : blocks) {
            buffer.writeByte(block.x);
            buffer.writeByte(block.y);
            buffer.writeByte(block.color);
        }

        buffer.writeShort(highScore);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        super.readFromBuffer(buffer);
        int size = buffer.readShort();
        for (int i = 0; i < size; i++) {
            blocks.add(new Block(buffer.readByte(), buffer.readByte(), buffer.readByte()));
        }

        highScore = buffer.readShort();
    }

    private float calculateProgress() {
        return (float) blocksBroken/ blocks.size();
    }

    private float calculateBallSpeed() {
        return INITIAL_BALL_SPEED + calculateProgress() * BALL_SPEED_INCREASE;
    }

    private void recalculateScore() {
        if (!isClientSide()) {
            int oldScore = score;
            score = (int) (blocksBroken - timeSinceLaunch / 100);
            if (getGameState() == GameState.WIN) score += 50;
            score = Math.max(score, 0);
            if (score != oldScore || timeSinceLaunch % 20 == 0) {
                sendToAllPlayers(new ScoreUpdatePacket(score, timeSinceLaunch / 20), null);
            }
        }
    }

    public void launchBall(float xMotion) {
        ballMoveX = xMotion / 2;
        ballMoveY = INITIAL_BALL_SPEED / UPDATES_PER_TICK;

        double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
        ballMoveX *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
        ballMoveY *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
        ballLaunched = true;
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT) {
            if (!ballLaunched) {
                float motion = (platformPos - oldPlatformPos) / UPDATES_PER_TICK;
                launchBall(motion);
                GameblockPackets.sendToServer(new BallLaunchPacket(ballX, motion));
            }

            if (isGameOver() && getGameTime() - endTime > (score > highScore ? 120 : 100)) restart();
        }
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            oldPlatformPos = platformPos;
            oldBallX = ballX;
            oldBallY = ballY;
            if (ballPath != null && ballLaunched) ballPath.enqueue(new Vector2f(oldBallX, oldBallY));

            boolean ballMoveUpdate = false;
            boolean sendBounceNoise = false;

            for (int ticks = 0; ticks < UPDATES_PER_TICK; ticks++) {
                //platformPos += (PLATFORM_SPEED / UPDATES_PER_TICK) * moveDir.getComponent();
                platformPos = getMouseCoordinates().x;
                platformPos = Math.max(Math.min(100.0f - PLATFORM_WIDTH / 2, platformPos), -100.0f + PLATFORM_WIDTH / 2);

                if (!ballLaunched) {
                    ballX = platformPos;
                    ballY = BALL_START_Y;
                } else {
                    ballX += ballMoveX;
                    ballY += ballMoveY;

                    if (ballX >= 100.0f - BALL_WIDTH / 2) {
                        ballMoveX = -Math.abs(ballMoveX);
                        ballMoveUpdate = true;
                        sendBounceNoise = true;
                    } else if (ballX <= -100.0f + BALL_WIDTH / 2) {
                        ballMoveX = Math.abs(ballMoveX);
                        ballMoveUpdate = true;
                        sendBounceNoise = true;
                    }
                    if (ballY >= 75.0f - BALL_WIDTH / 2) {
                        ballMoveY = -Math.abs(ballMoveY);
                        ballMoveUpdate = true;
                        sendBounceNoise = true;
                    } else if (ballY <= -75.0f - BALL_WIDTH / 2) {
                        if (!isClientSide()) setGameState(GameState.LOSS);
                        return;
                    }

                    // platform collision (this is handled on the client)
                    if (isClientSide() && ballMoveY < 0.0f) {
                        float platformCurrentPos = oldPlatformPos + ((float) (ticks + 1) / UPDATES_PER_TICK) * (platformPos - oldPlatformPos);
                        if (ballX >= platformCurrentPos - (PLATFORM_WIDTH + BALL_WIDTH) / 2 && ballX <= platformCurrentPos + (PLATFORM_WIDTH + BALL_WIDTH) / 2) {
                            if (ballY <= PLATFORM_Y + (PLATFORM_HEIGHT + BALL_WIDTH) / 2 && ballY >= PLATFORM_Y - (PLATFORM_HEIGHT + BALL_WIDTH) / 2) {
                                //ballMoveX += PLATFORM_SPEED / UPDATES_PER_TICK / 4 * moveDir.getComponent();
                                ballMoveX += (platformPos - oldPlatformPos) / UPDATES_PER_TICK / 2;
                                ballMoveY = Math.abs(ballMoveY);
                                double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
                                ballMoveX *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
                                ballMoveY *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);

                                playSound(GameblockSounds.BALL_BOUNCE.get());
                                GameblockPackets.sendToServer(new BallUpdatePacket(ballX, ballY, ballMoveX, ballMoveY, false));
                                clientToPacketBallUpdateTime = getGameTime();
                                //ballMoveUpdate = true;
                            }
                        }
                    }

                    for (int i = 0; i < blocks.size(); i++) {
                        Block block = blocks.get(i);
                        if (block == null) continue;
                        if (block.breaking > 0) {
                            block.breaking--;
                            continue;
                        }

                        float brickX = block.x * 5;
                        float brickY = block.y * 5;
                        if (ballX >= brickX - (BALL_WIDTH + BRICK_WIDTH) / 2 && ballX <= brickX + (BALL_WIDTH + BRICK_WIDTH) / 2) {
                            if (ballY >= brickY - (BALL_WIDTH + BRICK_HEIGHT) / 2 && ballY <= brickY + (BALL_WIDTH + BRICK_HEIGHT) / 2) {
                                float xComponent = (ballX - brickX) / BRICK_WIDTH;
                                float yComponent = (ballY - brickY) / BRICK_HEIGHT;
                                if (Math.abs(xComponent) > Math.abs(yComponent)) {
                                    ballMoveX = Math.abs(ballMoveX) * Math.signum(xComponent);
                                } else {
                                    ballMoveY = Math.abs(ballMoveY) * Math.signum(yComponent);
                                }
                                double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
                                ballMoveX *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
                                ballMoveY *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);

                                ballMoveUpdate = true;
                                if (!isClientSide()) {
                                    blocksBroken++;
                                    blocks.set(i, null);//blocks.set(i, null);
                                    if (blocksBroken >= blocks.size()) setGameState(GameState.WIN);
                                    sendToAllPlayers(new BlockUpdatePacket(i), null);
                                } else {
                                    blocks.get(i).breaking = BRICK_BREAK_FLASH_TIME;
                                    spawnBrickBreakParticles(blocks.get(i));
                                    //playSound(GameblockSounds.BRICK_BREAK.get());
                                }
                            }
                        }
                    }
                }
            }

            if (ballLaunched && !isClientSide()) { // server dictates the ball position
                if (ballMoveUpdate || getGameTime() - lastPacketTime > MAX_PACKET_INTERVAL) {
                    boolean finalSendBounceNoise = sendBounceNoise;
                    sendToAllPlayers(new BallUpdatePacket(ballX, ballY, ballMoveX, ballMoveY, finalSendBounceNoise), null);
                    lastPacketTime = getGameTime();
                }
                timeSinceLaunch++;
                recalculateScore();
            }
        }

        if (particleManager != null) particleManager.tick();
    }

    @Override
    public Music getMusic() {
        return (ballLaunched && !isGameOver()) ? GameblockMusic.BLOCK_BREAK : null;
    }

    public void spawnBrickBreakParticles(Block block) {
        Random random = new Random();
        float x = block.x * 5;
        float y = block.y * 5;
        int count = 10 + random.nextInt(8);
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat(Mth.TWO_PI);
            float magnitude = 0.9f + random.nextFloat(0.5f);
            particleManager.addParticle(x, y, magnitude * Mth.cos(angle), magnitude * Mth.sin(angle), 20, block.getColor());
        }

        playSound(GameblockSounds.BLOCK_BROKEN.get());
    }

    private long endTime = -1;

    @Override
    protected void onGameWin() {
        if (!isClientSide()) {
            score += 50;
            sendToAllPlayers(new ScoreUpdatePacket(score, timeSinceLaunch / 20), null);
        }
        endTime = getGameTime();
    }

    @Override
    protected void onGameLoss() {
        if (particleManager != null) {
            Random random = new Random();
            for (int i = 0; i < 25; i++) {
                float x = random.nextFloat(10.0f) - 5.0f;
                particleManager.addParticle(ballX + x, -75.0f,
                        0.0f, 1.2f + random.nextFloat(3.0f),
                        40,
                        new ColorF(random.nextInt(256), 255, 255));
            }

            playSound(GameblockSounds.BALL_BROKEN.get());
        }
        endTime = getGameTime();
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
    }

    @Override
    public void render() {
        float progress = calculateProgress();

        GuiGraphics graphics = getGraphicsInstance();
        Matrix4f matrix = graphics.pose().last().pose();
        VertexConsumer vertexconsumer = graphics.bufferSource().getBuffer(RenderType.gui());

        // gradient
        ColorF col1 = new ColorF(0.0f, 1.0f, 1.0f, 0.3f)
                .fadeTo(new ColorF(1.0f, 0.0f, 0.0f, 0.6f), progress);
        ColorF col2 = col1.withAlpha(0.0f);
        float offset = Mth.sin((getPartialTicks() + getGameTime()) / 10) * 10.0f;
        vertexconsumer.vertex(matrix, -100.0f, -30.0f + offset, 0.0f).color(col2.getRed(), col2.getGreen(), col2.getBlue(), col2.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, -100.0f, 75.0f, 0.0f).color(col1.getRed(), col1.getGreen(), col1.getBlue(), col1.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, 100.0f, 75.0f, 0.0f).color(col1.getRed(), col1.getGreen(), col1.getBlue(), col1.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, 100.0f, -30.0f + offset, 0.0f).color(col2.getRed(), col2.getGreen(), col2.getBlue(), col2.getAlpha()).endVertex();

        // progress bar
        /*ColorF barCol = new ColorF(1.0f);
        float minX = -70.0f;
        float maxX = -70.0f + progress * (70.0f * 2);
        vertexconsumer.vertex(matrix, minX, 63.0f, 0.0f).color(barCol.getRed(), barCol.getGreen(), barCol.getBlue(), barCol.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, minX, 65.0f, 0.0f).color(barCol.getRed(), barCol.getGreen(), barCol.getBlue(), barCol.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, maxX, 65.0f, 0.0f).color(barCol.getRed(), barCol.getGreen(), barCol.getBlue(), barCol.getAlpha()).endVertex();
        vertexconsumer.vertex(matrix, maxX, 63.0f, 0.0f).color(barCol.getRed(), barCol.getGreen(), barCol.getBlue(), barCol.getAlpha()).endVertex();*/

        graphics.flush();

        String timeString = "XX:XX";
        if (ballLaunched) {
            timeString = TextUtil.getTimeString(timeSinceLaunch, false, true);
        }

        float partialTicks = getPartialTicks();
        if (!isGameOver()) {
            drawText(80.0f, 67.5f, 0.5f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.score", score));
            drawText(80.0f, 62.5f, 0.5f, new ColorF(1.0f), Component.literal(timeString));
        } else {
            long gameOverTime = getGameTime() - endTime;
            if (gameOverTime > 20) {
                if (getGameState() == GameState.WIN) {
                    drawText(0.0f, 16.0f, 0.7f, new ColorF(0.0f, 1.0f, 0.0f), Component.translatable("gui.gameblock.block_break.win"));
                } else {
                    drawText(0.0f, 16.0f, 0.7f, new ColorF(1.0f, 0.0f, 0.0f), Component.translatable("gui.gameblock.block_break.lose"));
                }

                if (gameOverTime > 40) {
                    int percent = (int)(progress * 100);
                    drawText(0.0f, 8.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.blocks_broken", blocksBroken, percent));

                    if (gameOverTime > 60) {
                        drawText(0.0f, 0.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.time", timeString));

                        if (gameOverTime > 80) {
                            drawText(0.0f, -8.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.score", score));

                            if (gameOverTime > 100) {
                                if (score > highScore) { // new high score!
                                    drawText(40.0f, -8.0f, 0.4f, new ColorF(1.0f, 1.0f, 0.0f), Component.translatable("gui.gameblock.block_break.highscore"));

                                    if (gameOverTime > 120) {
                                        drawText(0.0f, -16.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.restart"));
                                    }
                                } else {
                                    drawText(0.0f, -16.0f, 0.7f, new ColorF(1.0f), Component.translatable("gui.gameblock.block_break.restart"));
                                }
                            }
                        }
                    }
                }
            }

            partialTicks = 0.0f; // fix vibration when game ends
        }

        drawTexture(SPRITE,
                oldPlatformPos + (platformPos - oldPlatformPos) * partialTicks,
                PLATFORM_Y,
                PLATFORM_WIDTH, PLATFORM_HEIGHT, 0, 0, 0, 20, 3);
        /*ballPath.forEach((Vector2f vect)-> drawTexture(graphics, SPRITE,
                vect.x,
                vect.y,
                BALL_WIDTH, BALL_WIDTH, 0, 20, 4, 4, 4));*/
        if (!isGameOver()) {
            AtomicInteger i = new AtomicInteger();
            float finalPartialTicks = partialTicks;
            ballPath.forEach((Vector2f vect) -> {
                float f = (i.getAndIncrement() + finalPartialTicks) / 5;
                f = 1.0f - f;
                drawRectangle(
                        vect.x,
                        vect.y, BALL_WIDTH * f, BALL_WIDTH * f, new ColorF(100).withAlpha(1.0f - f), 0);
            });
        /*drawTexture(graphics, SPRITE,
                drawX,
                drawY,
                BALL_WIDTH, BALL_WIDTH, 0, 20, 0, 4, 4);*/
            drawRectangle(
                    oldBallX + (ballX - oldBallX) * partialTicks,
                    oldBallY + (ballY - oldBallY) * partialTicks, BALL_WIDTH, BALL_WIDTH, new ColorF(100, 100, 255), 0);
        }

        for (Block block : blocks) {
            if (block == null) continue;

            if (block.breaking == 0) drawTexture(SPRITE, block.x * 5, block.y * 5, BRICK_WIDTH, BRICK_HEIGHT,
                    0,
                    246,
                    block.color * 5,
                    10,
                    5);
        }

        particleManager.render();
    }

    public class Block {
        public int x, y;
        public int breaking = 0;
        public final int color;

        public Block(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color % 7;
        }

        public ColorF getColor() {
            return switch (color) {
                case 0 -> new ColorF(0, 255, 0);
                case 1 -> new ColorF(0, 0, 255);
                case 2 -> new ColorF(0, 255, 255);
                case 3 -> new ColorF(255, 255, 0);
                case 4 -> new ColorF(255, 0, 0);
                case 5 -> new ColorF(255, 0, 255);
                case 6 -> new ColorF(255, 168, 0);
                default -> null;
            };
        }
    }
}
