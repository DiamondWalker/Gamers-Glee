package gameblock.game.games.paddles;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockPackets;
import gameblock.util.Direction1D;
import gameblock.util.Hitbox;
import gameblock.util.RectangleHitbox;
import net.minecraft.util.Mth;

import java.util.UUID;

public class Paddle {
    public static final float POSITION = 80.0f;
    public static final float WIDTH = 10.0f;
    public static final float DEPTH = 4.0f;

    private final PaddlesGame game;
    public final Direction1D direction;
    public final UUID owner;

    public float pos = 0.0f;
    public float oldPos = 0.0f;

    public Paddle(PaddlesGame game, Direction1D direction, UUID owner) {
        this.game = game;
        this.direction = direction;
        this.owner = owner;
    }

    public void tick() {
        if (game.isClientSide()) {
            oldPos = pos;
            if (owner.equals(game.players.getLocalPlayer().getUUID())) {
                pos = game.controls.getMouseCoordinates().y;
                pos = Mth.clamp(pos, GameInstance.MIN_Y + WIDTH / 2, GameInstance.MAX_Y - WIDTH / 2);
                if (pos != oldPos) game.networkManager.sendClientToServerPaddleUpdate(pos);
            }
        }
    }

    public Hitbox getHitbox(float f) {
        return new RectangleHitbox(POSITION * direction.getComponent(), oldPos + (pos - oldPos) * f, DEPTH, WIDTH);
    }

    /**
     * On the client side, determine whether this paddle is the one belonging to the local player
     *
     * @return true if the paddle belongs to the local player, false otherwise
     */
    public boolean isMyPaddle() {
        return owner.equals(game.players.getLocalPlayer().getUUID());
    }
}
