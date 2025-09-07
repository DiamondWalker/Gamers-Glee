package gameblock.game.blockbreak;

import gameblock.util.datastructure.CircularStack;
import org.joml.Vector2f;

public class BlockBreakBall {
    private final BlockBreakGame game;

    public static final float SIZE = 2.0f;
    public static final float INITIAL_SPEED = 3.0f;
    public static final float SPEED_INCREASE = 6.0f;

    public float x = 0.0f, y = BlockBreakGame.BALL_START_Y;
    public float oldX = x, oldY = y;
    public float moveX, moveY;

    public boolean launched = false;

    // CLIENT-ONLY
    CircularStack<Vector2f> path = null;

    public BlockBreakBall(BlockBreakGame game) {
        this.game = game;
        if (game.isClientSide()) path = new CircularStack<>(5);
    }
}
