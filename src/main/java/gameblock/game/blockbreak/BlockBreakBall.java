package gameblock.game.blockbreak;

import gameblock.game.GameInstance;
import gameblock.game.blockbreak.packets.BallUpdatePacket;
import gameblock.game.blockbreak.packets.BrickUpdatePacket;
import gameblock.gui.GameScreen;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import gameblock.util.GameState;
import gameblock.util.MathHelper;
import gameblock.util.datastructure.CircularStack;
import org.joml.Vector2f;

public class BlockBreakBall {
    private final BlockBreakGame game;

    public static final float SIZE = 2.0f;
    public static final float INITIAL_SPEED = 3.0f;
    public static final float SPEED_INCREASE = 6.0f;

    private static final int UPDATES_PER_TICK = 30;
    private static final float START_Y = BlockBreakGame.PLATFORM_Y + BlockBreakGame.PLATFORM_HEIGHT / 2 + BlockBreakBall.SIZE / 2;

    public float x = 0.0f, y = START_Y;
    public float oldX = x, oldY = y;
    public float moveX, moveY;

    public boolean launched = false;

    private boolean moveUpdate = false;
    private boolean sendBounceNoise = false;

    // CLIENT-ONLY
    CircularStack<Vector2f> path = null;

    public BlockBreakBall(BlockBreakGame game) {
        this.game = game;
        if (game.isClientSide()) path = new CircularStack<>(5);
    }

    private float calculateBallSpeed() {
        return BlockBreakBall.INITIAL_SPEED + game.calculateProgress() * BlockBreakBall.SPEED_INCREASE;
    }

    public void launch(float xMotion) {
        xMotion /= UPDATES_PER_TICK;

        moveX = xMotion / 2;
        moveY = BlockBreakBall.INITIAL_SPEED / UPDATES_PER_TICK;

        double speed = Math.sqrt(moveX * moveX + moveY * moveY);
        moveX *= (calculateBallSpeed() / speed);
        moveY *= (calculateBallSpeed() / speed);
        launched = true;
    }

    public void tick() {
        oldX = x;
        oldY = y;
        if (path != null && launched) path.enqueue(new Vector2f(oldX, oldY));

        moveUpdate = false;
        sendBounceNoise = false;

        for (int ticks = 0; ticks < UPDATES_PER_TICK; ticks++) {
            //platformPos += (PLATFORM_SPEED / UPDATES_PER_TICK) * moveDir.getComponent();

            if (!launched) {
                x = game.platformPos;
                y = START_Y;
            } else {
                x += moveX / UPDATES_PER_TICK;
                y += moveY / UPDATES_PER_TICK;

                if (Math.abs(x) >= GameInstance.MAX_X - BlockBreakBall.SIZE / 2 && MathHelper.hasSameSign(x, moveX)) {
                    moveX = -moveX;
                    moveUpdate = true;
                    sendBounceNoise = true;
                }

                if (y >= GameInstance.MAX_Y - BlockBreakBall.SIZE / 2 && moveY > 0) {
                    moveY = -moveY;
                    moveUpdate = true;
                    sendBounceNoise = true;
                } else if (y <= GameInstance.MIN_Y - BlockBreakBall.SIZE / 2) {
                    if (!game.isClientSide()) game.setGameState(GameState.LOSS);
                    return;
                }

                // platform collision (this is handled on the client)
                if (game.isClientSide() && moveY < 0.0f) {
                    float platformCurrentPos = game.oldPlatformPos + ((float) (ticks + 1) / UPDATES_PER_TICK) * (game.platformPos - game.oldPlatformPos);
                    if (x >= platformCurrentPos - (BlockBreakGame.PLATFORM_WIDTH + BlockBreakBall.SIZE) / 2 && x <= platformCurrentPos + (BlockBreakGame.PLATFORM_WIDTH + BlockBreakBall.SIZE) / 2) {
                        if (y <= BlockBreakGame.PLATFORM_Y + (BlockBreakGame.PLATFORM_HEIGHT + BlockBreakBall.SIZE) / 2 && y >= BlockBreakGame.PLATFORM_Y - (BlockBreakGame.PLATFORM_HEIGHT + BlockBreakBall.SIZE) / 2) {
                            //moveX += PLATFORM_SPEED / UPDATES_PER_TICK / 4 * moveDir.getComponent();
                            moveX += (game.platformPos - game.oldPlatformPos) / 2;
                            moveY = Math.abs(moveY);
                            double speed = Math.sqrt(moveX * moveX + moveY * moveY);
                            moveX *= (calculateBallSpeed() / speed);
                            moveY *= (calculateBallSpeed() / speed);

                            game.playSound(GameblockSounds.BALL_BOUNCE.get());
                            GameblockPackets.sendToServer(new BallUpdatePacket(x, y, moveX, moveY, false));
                            game.clientToPacketBallUpdateTime = game.getGameTime();
                            //ballMoveUpdate = true;
                        }
                    }
                }

                for (int i = 0; i < game.blocks.size(); i++) {
                    BlockBreakBrick block = game.blocks.get(i);
                    if (block == null) continue;
                    if (block.breaking > 0) {
                        block.breaking--;
                        continue;
                    }

                    float brickX = block.x * 5;
                    float brickY = block.y * 5;
                    if (x >= brickX - (BlockBreakBall.SIZE + BlockBreakBrick.WIDTH) / 2 && x <= brickX + (BlockBreakBall.SIZE + BlockBreakBrick.WIDTH) / 2) {
                        if (y >= brickY - (BlockBreakBall.SIZE + BlockBreakBrick.HEIGHT) / 2 && y <= brickY + (BlockBreakBall.SIZE + BlockBreakBrick.HEIGHT) / 2) {
                            float xComponent = (x - brickX) / BlockBreakBrick.WIDTH;
                            float yComponent = (y - brickY) / BlockBreakBrick.HEIGHT;
                            if (Math.abs(xComponent) > Math.abs(yComponent)) {
                                moveX = Math.abs(moveX) * Math.signum(xComponent);
                            } else {
                                moveY = Math.abs(moveY) * Math.signum(yComponent);
                            }
                            double speed = Math.sqrt(moveX * moveX + moveY * moveY);
                            moveX *= (calculateBallSpeed() / speed);
                            moveY *= (calculateBallSpeed() / speed);

                            moveUpdate = true;
                            if (!game.isClientSide()) {
                                game.blocksBroken++;
                                game.blocks.set(i, null);//blocks.set(i, null);
                                if (game.blocksBroken >= game.blocks.size()) game.setGameState(GameState.WIN);
                                game.sendToAllPlayers(new BrickUpdatePacket(i), null);
                            } else {
                                game.blocks.get(i).breaking = BlockBreakGame.BRICK_BREAK_CLIENT_REAPPEAR_TIME;
                                game.spawnBrickBreakParticles(game.blocks.get(i));
                                //playSound(GameblockSounds.BRICK_BREAK.get());
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean needsToSyncMovement() {
        return moveUpdate;
    }

    public boolean shouldPlayBounceSound() {
        return sendBounceNoise;
    }
}
