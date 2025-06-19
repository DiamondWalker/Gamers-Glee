package gameblock.game.evasion;

import com.mojang.blaze3d.platform.InputConstants;
import gameblock.game.Game;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.FastColor;

import java.util.ArrayList;

public class BlockBreakGame extends Game {
    private static final int UPDATES_PER_TICK = 3;

    private static final float PLATFORM_Y = -40.0f;
    private static final float PLATFORM_WIDTH = 20.0f;
    private static final float PLATFORM_HEIGHT = 3.0f;
    private static final float PLATFORM_SPEED = 2.1f;

    private static final float BALL_WIDTH = 2.0f;

    private static final float BALL_START_Y = PLATFORM_Y + PLATFORM_HEIGHT / 2 + BALL_WIDTH / 2;
    private static final float BALL_SPEED = 3.0f;

    private static final float BRICK_WIDTH = 9.7f;
    private static final float BRICK_HEIGHT = 4.7f;

    float platformPos = 0.0f;
    float oldPlatformPos = platformPos;

    float ballX = 0.0f, ballY = BALL_START_Y;
    float oldBallX = ballX, oldBallY = ballY;
    float ballMoveX, ballMoveY;

    boolean ballLaunched = false;

    ArrayList<Brick> bricks = new ArrayList<>();

    final KeyBinding left = registerKey(InputConstants.KEY_LEFT);
    final KeyBinding right = registerKey(InputConstants.KEY_RIGHT);
    final KeyBinding launch = registerKey(InputConstants.KEY_SPACE);

    public BlockBreakGame() {
        for (int y = 1; y <= 4; y++) {
            if (y < 4) {
                for (int x = -10; x <= 10; x += 2) {
                    bricks.add(new Brick(x, y * 2 - 1 + 1, 3));
                }
            }
            for (int x = -11; x <= 11; x += 2) {
                bricks.add(new Brick(x, y * 2 - 1, 3));
            }
        }
    }

    @Override
    public void tick() {
        oldPlatformPos = platformPos;
        oldBallX = ballX;
        oldBallY = ballY;

        for (int ticks = 0; ticks < UPDATES_PER_TICK; ticks++) {
            if (left.pressed) platformPos -= PLATFORM_SPEED / UPDATES_PER_TICK;
            if (right.pressed) platformPos += PLATFORM_SPEED / UPDATES_PER_TICK;
            platformPos = Math.max(Math.min(100.0f - PLATFORM_WIDTH / 2, platformPos), -100.0f + PLATFORM_WIDTH / 2);

            if (!ballLaunched) {
                ballX = platformPos;
                ballY = BALL_START_Y;
                if (launch.pressed) {
                    ballMoveY = BALL_SPEED / UPDATES_PER_TICK;
                    ballLaunched = true;
                }
            } else {
                ballX += ballMoveX;
                ballY += ballMoveY;

                if (ballX >= 100.0f - BALL_WIDTH / 2) {
                    ballMoveX = -Math.abs(ballMoveX);
                } else if (ballX <= -100.0f + BALL_WIDTH / 2) {
                    ballMoveX = Math.abs(ballMoveX);
                }
                if (ballY >= 50.0f) {
                    ballMoveY = -Math.abs(ballMoveY);
                }
                if (ballX >= platformPos - (PLATFORM_WIDTH + BALL_WIDTH) / 2 && ballX <= platformPos + (PLATFORM_WIDTH + BALL_WIDTH) / 2) {
                    if (ballY <= PLATFORM_Y + (PLATFORM_HEIGHT + BALL_WIDTH) / 2 && ballY >= PLATFORM_Y - (PLATFORM_HEIGHT + BALL_WIDTH) / 2) {
                        if (left.pressed) {
                            ballMoveX -= PLATFORM_SPEED / UPDATES_PER_TICK / 2;
                            ballMoveX = Math.max(-PLATFORM_SPEED / UPDATES_PER_TICK, ballMoveX);
                        }
                        if (right.pressed) {
                            ballMoveX += PLATFORM_SPEED / UPDATES_PER_TICK / 2;
                            ballMoveX = Math.min(PLATFORM_SPEED / UPDATES_PER_TICK, ballMoveX);
                        }
                        ballMoveY = Math.abs(ballMoveY);
                        double speed = Math.sqrt(ballMoveX * ballMoveX + ballMoveY * ballMoveY);
                        ballMoveX *= (BALL_SPEED / UPDATES_PER_TICK / speed);
                        ballMoveY *= (BALL_SPEED / UPDATES_PER_TICK / speed);
                    }
                }

                for (int i = 0; i < bricks.size();) {
                    Brick brick = bricks.get(i);
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
                            brick.hitsLeft--;
                            if (brick.hitsLeft <= 0) {
                                bricks.remove(i);
                                continue;
                            }
                        }
                    }
                    i++;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        drawRectangle(graphics,
                oldPlatformPos + (platformPos - oldPlatformPos) * partialTicks,
                PLATFORM_Y,
                PLATFORM_WIDTH, PLATFORM_HEIGHT, 255, 255, 255, 255, 0);
        drawRectangle(graphics,
                oldBallX + (ballX - oldBallX) * partialTicks,
                oldBallY + (ballY - oldBallY) * partialTicks, BALL_WIDTH, BALL_WIDTH, 100, 100, 255, 255, 0);

        for (Brick brick : bricks) {
            int red = 0, green = 0, blue = 0;
            switch (brick.hitsLeft) {
                case 1: {
                    red = 255;
                    break;
                }
                case 2: {
                    red = 255;
                    green = 255;
                    break;
                }
                case 3: {
                    green = 255;
                    break;
                }
                default: {
                    red = 255;
                    blue = 255;
                }
            }
            drawRectangle(graphics, brick.x * 5, brick.y * 5, BRICK_WIDTH, BRICK_HEIGHT, red, green, blue, 255, 0);
        }
    }

    private class Brick {
        protected int x, y;
        protected int hitsLeft;

        public Brick(int x, int y, int hits) {
            this.x = x;
            this.y = y;
            this.hitsLeft = hits;
        }
    }
}
