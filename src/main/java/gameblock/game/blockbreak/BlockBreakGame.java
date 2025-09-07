package gameblock.game.blockbreak;

import com.mojang.blaze3d.vertex.VertexConsumer;
import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.blockbreak.packets.BallLaunchPacket;
import gameblock.game.blockbreak.packets.BallUpdatePacket;
import gameblock.game.blockbreak.packets.BrickUpdatePacket;
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
import org.lwjgl.system.Platform;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockBreakGame extends GameInstance<BlockBreakGame> {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/block_break.png");

    public static final int BRICK_BREAK_CLIENT_REAPPEAR_TIME = 30; // when a brick is removed on the client side, we still aren't 100% sure it's actually getting removed, so after a bit we'll bring it back

    private static final int MAX_PACKET_INTERVAL = 10;


    final BlockBreakParticleManager particleManager = isClientSide() ? new BlockBreakParticleManager(this) : null;

    public BlockBreakBall ball;
    public BlockBreakPlatform platform;

    public long timeSinceLaunch = 0;

    public final ArrayList<BlockBreakBrick> blocks = new ArrayList<>();
    public int blocksBroken;

    public long lastPacketTime = 0; // every so often packets should be sent to ensure everything is synced

    public int score = 0;
    public int highScore = 0;

    public long clientToPacketBallUpdateTime = -1;

    public BlockBreakGame(Player player) {
        super(player, GameblockGames.BLOCK_BREAK_GAME);
        ball = new BlockBreakBall(this);
        platform = new BlockBreakPlatform(this);
        
        int col = 0;
        if (!isClientSide()) {
            for (int y = 6; y >= 3; y--) {
                if (y < 6) {
                    for (int x = -14; x <= 14; x += 2) {
                        blocks.add(new BlockBreakBrick(x, y * 2 - 1 + 1, col));
                    }
                    col++;
                }
                for (int x = -15; x <= 15; x += 2) {
                    blocks.add(new BlockBreakBrick(x, y * 2 - 1, col));
                }
                col++;
            }
        }
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeShort(blocks.size());
        for (BlockBreakBrick block : blocks) {
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
            blocks.add(new BlockBreakBrick(buffer.readByte(), buffer.readByte(), buffer.readByte()));
        }

        highScore = buffer.readShort();
    }

    public float calculateProgress() {
        return (float) blocksBroken/ blocks.size();
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

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT) {
            if (!ball.launched) {
                float motion = platform.pos - platform.oldPos;
                ball.launch(motion);
                GameblockPackets.sendToServer(new BallLaunchPacket(ball.x, motion));
            }

            if (isGameOver() && getGameTime() - endTime > (score > highScore ? 120 : 100)) restart();
        }
    }

    @Override
    public void tick() {
        platform.tick();
        ball.tick();
        if (ball.launched && !isClientSide()) { // server dictates the ball position
            if (ball.needsToSyncMovement() || getGameTime() - lastPacketTime > MAX_PACKET_INTERVAL) {
                boolean finalSendBounceNoise = ball.shouldPlayBounceSound();
                sendToAllPlayers(new BallUpdatePacket(ball.x, ball.y, ball.moveX, ball.moveY, finalSendBounceNoise), null);
                lastPacketTime = getGameTime();
            }
            if (!isGameOver()) {
                timeSinceLaunch++;
                recalculateScore();
            }
        }

        if (particleManager != null) particleManager.tick();
    }

    @Override
    public Music getMusic() {
        return (ball.launched && !isGameOver()) ? GameblockMusic.BLOCK_BREAK : null;
    }

    public void spawnBrickBreakParticles(BlockBreakBrick block) {
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
                particleManager.addParticle(ball.x + x, -75.0f,
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
        if (ball.launched) {
            timeString = TextUtil.getTimeString(timeSinceLaunch, false, true);
        }

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
        }

        platform.render();
        ball.render();

        for (BlockBreakBrick block : blocks) {
            if (block == null) continue;

            if (block.breaking == 0) drawTexture(SPRITE, block.x * 5, block.y * 5, BlockBreakBrick.WIDTH, BlockBreakBrick.HEIGHT,
                    0,
                    246,
                    block.color * 5,
                    10,
                    5);
        }

        particleManager.render();
    }
}
