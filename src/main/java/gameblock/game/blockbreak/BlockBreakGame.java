package gameblock.game.blockbreak;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.GameblockMod;
import gameblock.game.Game;
import gameblock.registry.GameblockPackets;
import gameblock.util.CircularStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.units.qual.A;
import org.joml.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class BlockBreakGame extends Game {
    public static ResourceLocation SPRITE = new ResourceLocation(GameblockMod.MODID, "textures/gui/game/block_break.png");

    private static final int UPDATES_PER_TICK = 15;

    private static final float PLATFORM_Y = -50.0f;
    private static final float PLATFORM_WIDTH = 20.0f;
    private static final float PLATFORM_HEIGHT = 3.0f;
    private static final float PLATFORM_SPEED = 2.1f;

    private static final float BALL_WIDTH = 2.0f;
    private static final float INITIAL_BALL_SPEED = 3.0f;

    private static final float BALL_START_Y = PLATFORM_Y + PLATFORM_HEIGHT / 2 + BALL_WIDTH / 2;

    private static final float BRICK_WIDTH = 10.0f;
    private static final float BRICK_HEIGHT = 5.0f;

    protected static final int BRICK_BREAK_FLASH_TIME = 30;

    float platformPos = 0.0f;
    float oldPlatformPos = platformPos;
    byte oldMoveDir = 0;
    byte moveDir = 0;

    float ballX = 0.0f, ballY = BALL_START_Y;
    float oldBallX = ballX, oldBallY = ballY;
    float ballMoveX, ballMoveY;

    CircularStack<Vector2f> ballPath = null;

    boolean ballLaunched = false;

    ArrayList<Brick> bricks = new ArrayList<>();
    protected int bricksBroken = 0;

    ArrayList<Particle> particles = null;

    final KeyBinding left = registerKey(InputConstants.KEY_LEFT);
    final KeyBinding right = registerKey(InputConstants.KEY_RIGHT);
    final KeyBinding launch = registerKey(InputConstants.KEY_SPACE);

    public BlockBreakGame(Player player) {
        super(player);
        int col = 0;
        for (int y = 6; y >= 3; y--) {
            if (y < 6) {
                for (int x = -14; x <= 14; x += 2) {
                    bricks.add(new Brick(x, y * 2 - 2 + 1, col));
                }
                col++;
            }
            for (int x = -15; x <= 15; x += 2) {
                bricks.add(new Brick(x, y * 2 - 2, col));
            }
            col++;
        }

        if (isClientSide()) {
            ballPath = new CircularStack<>(5);
            particles = new ArrayList<>();
        }
    }

    private float calculateBallSpeed() {
        return INITIAL_BALL_SPEED + ((float) bricksBroken / bricks.size()) * INITIAL_BALL_SPEED;
    }

    protected void launchBall(float xMotion) {
        ballMoveX = xMotion / 2;
        ballMoveY = INITIAL_BALL_SPEED / UPDATES_PER_TICK;

        double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
        ballMoveX *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
        ballMoveY *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
        ballLaunched = true;
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            oldPlatformPos = platformPos;
            oldBallX = ballX;
            oldBallY = ballY;
            if (ballPath != null && ballLaunched) ballPath.enqueue(new Vector2f(oldBallX, oldBallY));

            boolean ballMoveUpdate = false;

            for (int ticks = 0; ticks < UPDATES_PER_TICK; ticks++) {
                if (isClientSide()) {
                    oldMoveDir = moveDir;
                    moveDir = 0;
                    if (left.pressed) moveDir--;
                    if (right.pressed) moveDir++;
                }
                platformPos += (PLATFORM_SPEED / UPDATES_PER_TICK) * moveDir;
                platformPos = Math.max(Math.min(100.0f - PLATFORM_WIDTH / 2, platformPos), -100.0f + PLATFORM_WIDTH / 2);

                if (moveDir != oldMoveDir) GameblockPackets.sendToServer(new PlatformMovePacket(platformPos, moveDir));

                if (!ballLaunched) {
                    ballX = platformPos;
                    ballY = BALL_START_Y;
                    if (launch.pressed) {
                        float motion = moveDir * PLATFORM_SPEED / UPDATES_PER_TICK;
                        launchBall(motion);
                        GameblockPackets.sendToServer(new BallLaunchPacket(motion));
                    }
                } else {
                    ballX += ballMoveX;
                    ballY += ballMoveY;

                    if (ballX >= 100.0f - BALL_WIDTH / 2) {
                        ballMoveX = -Math.abs(ballMoveX);
                        ballMoveUpdate = true;
                    } else if (ballX <= -100.0f + BALL_WIDTH / 2) {
                        ballMoveX = Math.abs(ballMoveX);
                        ballMoveUpdate = true;
                    }
                    if (ballY >= 75.0f - BALL_WIDTH / 2) {
                        ballMoveY = -Math.abs(ballMoveY);
                        ballMoveUpdate = true;
                    } else if (ballY <= -75.0f - BALL_WIDTH / 2) {
                        if (!isClientSide()) gameOver();
                        return;
                    }
                    if (ballX >= platformPos - (PLATFORM_WIDTH + BALL_WIDTH) / 2 && ballX <= platformPos + (PLATFORM_WIDTH + BALL_WIDTH) / 2) {
                        if (ballY <= PLATFORM_Y + (PLATFORM_HEIGHT + BALL_WIDTH) / 2 && ballY >= PLATFORM_Y - (PLATFORM_HEIGHT + BALL_WIDTH) / 2) {
                            if (moveDir < 0) {
                                ballMoveX -= PLATFORM_SPEED / UPDATES_PER_TICK / 2;
                                //ballMoveX = Math.max(-PLATFORM_SPEED / UPDATES_PER_TICK, ballMoveX);
                            } else if (moveDir > 0) {
                                ballMoveX += PLATFORM_SPEED / UPDATES_PER_TICK / 2;
                                //ballMoveX = Math.min(PLATFORM_SPEED / UPDATES_PER_TICK, ballMoveX);
                            }
                            ballMoveY = Math.abs(ballMoveY);
                            double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
                            ballMoveX *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);
                            ballMoveY *= (calculateBallSpeed() / UPDATES_PER_TICK / speed);

                            ballMoveUpdate = true;
                        }
                    }

                    for (int i = 0; i < bricks.size(); i++) {
                        Brick brick = bricks.get(i);
                        if (brick == null) continue;
                        if (brick.breaking > 0) {
                            brick.breaking--;
                            continue;
                        }

                        float brickX = brick.x * 5;
                        float brickY = brick.y * 5;
                        if (ballX >= brickX - (BALL_WIDTH + BRICK_WIDTH) / 2 && ballX <= brickX + (BALL_WIDTH + BRICK_WIDTH) / 2) {
                            if (ballY >= brickY - (BALL_WIDTH + BRICK_HEIGHT) / 2 && ballY <= brickY + (BALL_WIDTH + BRICK_HEIGHT) / 2) {
                                float xComponent = (ballX - brickX) / BRICK_WIDTH;
                                float yComponent = (ballY - brickY) / BRICK_HEIGHT;
                                if (Math.abs(xComponent) > Math.abs(yComponent)) {
                                    ballMoveX = Math.abs(ballMoveX) * Math.signum(xComponent);
                                } else {
                                    ballMoveY = Math.abs(ballMoveY) * Math.signum(yComponent);
                                }

                                ballMoveUpdate = true;
                                if (!isClientSide()) {
                                    bricks.set(i, null);
                                    bricksBroken++;
                                    GameblockPackets.sendToPlayer((ServerPlayer) player, new BrickUpdatePacket(i));
                                } else {
                                    bricks.get(i).breaking = BRICK_BREAK_FLASH_TIME;
                                    spawnBrickBreakParticles(bricks.get(i));
                                }
                            }
                        }
                    }
                }
            }

            if (ballMoveUpdate && !isClientSide()) {
                GameblockPackets.sendToPlayer((ServerPlayer) player, new BallUpdatePacket(ballX, ballY, ballMoveX, ballMoveY));
            }
        }

        if (particles != null) {
            synchronized (particles) {
                for (int i = 0; i < particles.size();) {
                    if (particles.get(i).update()) {
                        i++;
                    } else {
                        particles.remove(i);
                    }
                }
            }
        }
    }

    protected void spawnBrickBreakParticles(Brick brick) {
        Random random = new Random();
        float x = brick.x * 5;
        float y = brick.y * 5;
        int count = 5 + random.nextInt(4);
        synchronized (particles) {
            for (int i = 0; i < count; i++) {
                float angle = random.nextFloat(Mth.TWO_PI);
                float magnitude = 0.9f + random.nextFloat(0.5f);
                particles.add(new Particle(x, y, magnitude * Mth.cos(angle), magnitude * Mth.sin(angle), 20, brick.getColor()));
            }
        }
    }

    @Override
    protected void gameOver() {
        super.gameOver();
        if (particles != null) {
            Random random = new Random();
            for (int i = 0; i < 25; i++) {
                float x = random.nextFloat(10.0f) - 5.0f;
                particles.add(new Particle(ballX + x, ballY,
                        0.0f, 1.2f + random.nextFloat(3.0f),
                        40,
                        new Color(random.nextInt(256), 255, 255)));
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawTexture(graphics, SPRITE,
                oldPlatformPos + (platformPos - oldPlatformPos) * partialTicks,
                PLATFORM_Y,
                PLATFORM_WIDTH, PLATFORM_HEIGHT, 0, 0, 0, 20, 3);
        /*ballPath.forEach((Vector2f vect)-> drawTexture(graphics, SPRITE,
                vect.x,
                vect.y,
                BALL_WIDTH, BALL_WIDTH, 0, 20, 4, 4, 4));*/
        if (!isGameOver()) {
            AtomicInteger i = new AtomicInteger();
            ballPath.forEach((Vector2f vect) -> {
                float f = (i.getAndIncrement() + partialTicks) / 5;
                f = 1.0f - f;
                drawRectangle(graphics,
                        vect.x,
                        vect.y, BALL_WIDTH * f, BALL_WIDTH * f, 100, 100, 100, (int) ((1.0f - f) * 255), 0);
            });
        /*drawTexture(graphics, SPRITE,
                drawX,
                drawY,
                BALL_WIDTH, BALL_WIDTH, 0, 20, 0, 4, 4);*/
            drawRectangle(graphics,
                    oldBallX + (ballX - oldBallX) * partialTicks,
                    oldBallY + (ballY - oldBallY) * partialTicks, BALL_WIDTH, BALL_WIDTH, 100, 100, 255, 255, 0);
        }

        for (Brick brick : bricks) {
            if (brick == null) continue;

            if (brick.breaking == 0) drawTexture(graphics, SPRITE, brick.x * 5, brick.y * 5, BRICK_WIDTH, BRICK_HEIGHT,
                    0,
                    246,
                    brick.color * 5,
                    10,
                    5);
        }

        synchronized (particles) {
            for (Particle particle : particles) {
                float f = particle.time < 10 ? (float) particle.time / 10 : 1.0f;
                drawRectangle(graphics,
                        particle.oldX + (particle.x - particle.oldX) * partialTicks,
                        particle.oldY + (particle.y - particle.oldY) * partialTicks, 1.0f, 1.0f,
                        particle.color.getRed(), particle.color.getGreen(), particle.color.getBlue(), (int) (f * 255), 0);
            }
        }
    }

    protected class Brick {
        protected int x, y;
        protected int breaking = 0;
        private final int color;

        public Brick(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color % 7;
        }

        private Color getColor() {
            return switch (color) {
                case 0 -> new Color(0, 255, 0);
                case 1 -> new Color(0, 0, 255);
                case 2 -> new Color(0, 255, 255);
                case 3 -> new Color(255, 255, 0);
                case 4 -> new Color(255, 0, 0);
                case 5 -> new Color(255, 0, 255);
                case 6 -> new Color(255, 168, 0);
                default -> null;
            };
        }
    }

    private class Particle {
        private float x, y;
        private float oldX, oldY;
        private float motionX, motionY;
        private Color color;
        private int time;

        private Particle(float x, float y, float motionX, float motionY, int time, Color color) {
            this.x = this.oldX = x;
            this.y = this.oldY = y;
            this.motionX = motionX;
            this.motionY = motionY;
            this.time = time;
            this.color = color;
        }

        private boolean update() {
            this.oldX = x;
            this.oldY = y;

            x += motionX;
            y += motionY;

            motionX *= 0.95f;
            motionY *= 0.95f;

            return time-- > 0;
        }
    }
}
