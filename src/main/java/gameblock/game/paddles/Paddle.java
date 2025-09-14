package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.game.paddles.packets.ClientToServerPaddleUpdatePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.collision.Hitbox;
import gameblock.util.physics.collision.RectangleHitbox;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

public class Paddle {

    public static final float POSITION = 80.0f;
    public static final float WIDTH = 10.0f;
    public static final float DEPTH = 4.0f;

    private final PaddlesGame game;
    public final Direction1D direction;

    public float pos = 0.0f;
    public float oldPos = 0.0f;

    public Paddle(PaddlesGame game, Direction1D direction) {
        this.game = game;
        this.direction = direction;
    }

    public void tick() {
        if (game.isClientSide()) {
            oldPos = pos;
            if (direction == game.whichPaddleAmI) {
                pos = game.getMouseCoordinates().y;
                pos = Mth.clamp(pos, GameInstance.MIN_Y + WIDTH / 2, GameInstance.MAX_Y - WIDTH / 2);
                if (pos != oldPos) GameblockPackets.sendToServer(new ClientToServerPaddleUpdatePacket(pos));
            } else {
                pos = game.otherPaddleUpdatePos;
            }
        }
    }

    public Hitbox getHitbox() {
        return new RectangleHitbox(POSITION * direction.getComponent(), pos, DEPTH, WIDTH);
    }
}
