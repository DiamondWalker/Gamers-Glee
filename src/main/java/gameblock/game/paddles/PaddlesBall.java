package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.game.paddles.packets.PaddleGameBallUpdatePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.MathHelper;
import gameblock.util.TickTimer;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.collision.Hitbox;
import gameblock.util.physics.collision.RectangleHitbox;
import net.minecraft.world.phys.Vec2;

public class PaddlesBall {
    private final PaddlesGame game;

    public static final float SIZE = 3.0f;
    public static final float DEFAULT_SPEED = 3.0f;
    private static final int PADDLE_COLLISION_CHECKS = 100;

    public Vec2 pos = Vec2.ZERO;
    public Vec2 oldPos = Vec2.ZERO;
    public Vec2 motion = Vec2.ZERO;
    public float speed = DEFAULT_SPEED;

    public PaddlesBall(PaddlesGame game) {
        this.game = game;
    }

    public void tick() {
        if (game.isGameOver()) {
            oldPos = pos;
            pos = pos.add(motion.scale(3.5f));

            // bounce off the top and bottom of the screen
            if (pos.y + PaddlesBall.SIZE / 2 >= GameInstance.MAX_Y || pos.y - PaddlesBall.SIZE / 2 <= GameInstance.MIN_Y) {
                if (MathHelper.hasSameSign(pos.y, motion.y)) { // make sure it's still moving out of the screen
                    motion = new Vec2(motion.x, -motion.y);
                }
            }
            // bounce off the sides for the game over animation thing
            if (pos.x + PaddlesBall.SIZE / 2 >= GameInstance.MAX_X || pos.x - PaddlesBall.SIZE / 2 <= GameInstance.MIN_X) {
                if (MathHelper.hasSameSign(pos.x, motion.x)) { // make sure it's still moving out of the screen
                    motion = new Vec2(-motion.x, motion.y);
                }
            }
        } else {
            oldPos = pos;
            pos = pos.add(motion.scale(speed));

            // bounce off the top and bottom of the screen
            if (pos.y + PaddlesBall.SIZE / 2 >= GameInstance.MAX_Y || pos.y - PaddlesBall.SIZE / 2 <= GameInstance.MIN_Y) {
                if (MathHelper.hasSameSign(pos.y, motion.y)) { // make sure it's still moving out of the screen
                    motion = new Vec2(motion.x, -motion.y);
                }
            }

            // paddle collision
            if (game.isClientSide()) {
                for (int i = 1; i <= PADDLE_COLLISION_CHECKS; i++) {
                    if (MathHelper.hasSameSign(pos.x, motion.x)) { // make sure the ball is moving towards one of the paddles
                        for (Paddle paddle : new Paddle[] {game.leftPaddle, game.rightPaddle}) {
                            float f = (float) i / PADDLE_COLLISION_CHECKS;
                            Hitbox ballHitbox = this.getHitbox(f);
                            Hitbox paddleHitbox = paddle.getHitbox(f);
                            if (Hitbox.areColliding(ballHitbox, paddleHitbox)) {
                                float yComponent = (ballHitbox.getOrigin().y - paddleHitbox.getOrigin().y) / ((PaddlesBall.SIZE + Paddle.WIDTH) / 2); // if the ball hits the very corner, this will be 1 or -1. If the ball hits the center, it'll be 0
                                speed += 0.2f;
                                motion = new Vec2(-paddle.direction.getComponent(), yComponent);

                                if (paddle.direction == game.whichPaddleAmI) {
                                    GameblockPackets.sendToServer(new PaddleGameBallUpdatePacket(pos, motion, speed));
                                }
                                pos = ballHitbox.getOrigin();
                                break;
                            }
                        }
                    }
                }
            }

            // win condition
            if (!game.isClientSide()) {
                if (pos.x - SIZE > GameInstance.MAX_X || pos.x + SIZE < GameInstance.MIN_X) {
                    game.score(Direction1D.getFromCoordinate(pos.x).getOpposite());
                }
            }
        }
    }

    public void resetBall() {
        pos = oldPos = Vec2.ZERO;
        motion = new Vec2(Math.signum(motion.x), Math.signum(motion.y));
        speed = DEFAULT_SPEED;
    }

    public Hitbox getHitbox(float f) {
        return new RectangleHitbox(pos.add(oldPos.negated()).scale(f).add(oldPos), SIZE, SIZE);
    }
}
