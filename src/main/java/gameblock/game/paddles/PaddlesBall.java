package gameblock.game.paddles;

import gameblock.game.paddles.packets.PaddleGameBallUpdatePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.MathHelper;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.collision.Hitbox;
import gameblock.util.physics.collision.RectangleHitbox;
import net.minecraft.world.phys.Vec2;

public class PaddlesBall {
    private final PaddlesGame game;

    public static final float SIZE = 3.0f;

    public Vec2 pos = Vec2.ZERO;
    public Vec2 oldPos = Vec2.ZERO;
    public Vec2 motion = Vec2.ZERO;
    public float speed = 1.5f;

    public PaddlesBall(PaddlesGame game) {
        this.game = game;
    }

    public void tick() {
        oldPos = pos;
        pos = pos.add(motion);

        // bounce off the top and bottom of the screen
        if (Math.abs(pos.y + PaddlesBall.SIZE / 2) >= 75) {
            if (MathHelper.hasSameSign(pos.y, motion.y)) { // make sure it's still moving out of the screen
                motion = new Vec2(motion.x, -motion.y);
            }
        }

        // paddle collision
        if (MathHelper.hasSameSign(pos.x, motion.x)) { // make sure the ball is moving towards one of the paddles
            for (Paddle paddle : new Paddle[] {game.leftPaddle, game.rightPaddle}) {
                if (Hitbox.areColliding(this.getHitbox(), paddle.getHitbox())) {
                    float yComponent = (pos.y - paddle.pos) / ((PaddlesBall.SIZE + Paddle.WIDTH) / 2); // if the ball hits the very corner, this will be 1 or -1. If the ball hits the center, it'll be 0
                    speed *= 1.2f;
                    motion = new Vec2(-motion.x, yComponent).normalized().scale(speed);

                    if (paddle.direction == game.whichPaddleAmI) GameblockPackets.sendToServer(new PaddleGameBallUpdatePacket(pos, motion));
                }
            }
        }
    }

    public Hitbox getHitbox() {
        return new RectangleHitbox(pos, SIZE, SIZE);
    }
}
