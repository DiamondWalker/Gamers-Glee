package gameblock.game.blockbreak;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.GameblockMod;
import gameblock.game.Game;
import gameblock.registry.GameblockPackets;
import gameblock.util.CircularStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2f;

import java.util.ArrayList;
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

        if (isClientSide()) ballPath = new CircularStack<>(5);
    }

    private float calculateBallSpeed() {
        return INITIAL_BALL_SPEED + ((float) bricksBroken / bricks.size()) * INITIAL_BALL_SPEED;
    }

    protected void launchBall() {
        ballMoveY = calculateBallSpeed() / UPDATES_PER_TICK;
        ballLaunched = true;
    }

    @Override
    public void tick() {
        if (!isGameOver()) {
            oldPlatformPos = platformPos;
            oldBallX = ballX;
            oldBallY = ballY;
            if (ballPath != null) ballPath.enqueue(new Vector2f(oldBallX, oldBallY));

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
                        launchBall();
                        GameblockPackets.sendToServer(new BallLaunchPacket());
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

        for (Brick brick : bricks) {
            if (brick == null) continue;

            if (brick.breaking == 0) drawTexture(graphics, SPRITE, brick.x * 5, brick.y * 5, BRICK_WIDTH, BRICK_HEIGHT,
                    0,
                    246,
                    brick.color * 5,
                    10,
                    5);
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
    }
}
