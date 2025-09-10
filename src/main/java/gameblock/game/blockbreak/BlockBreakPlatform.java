package gameblock.game.blockbreak;

import gameblock.game.GameInstance;

public class BlockBreakPlatform {
    public static final float Y_POSITION = -50.0f;
    public static final float WIDTH = 20.0f;
    public static final float HEIGHT = 3.0f;

    private final BlockBreakGame game;
    public float pos = 0.0f;
    public float oldPos = pos;

    public BlockBreakPlatform(BlockBreakGame game) {
        this.game = game;
    }

    public void tick() {
        oldPos = pos;
        if (!game.isGameOver()) {
            pos = game.getMouseCoordinates().x;
            pos = Math.max(Math.min(GameInstance.MAX_X - WIDTH / 2, pos), GameInstance.MIN_X + WIDTH / 2);
        }
    }

    public void render() {
        game.drawTexture(BlockBreakGame.SPRITE,
                oldPos + (pos - oldPos) * (game.isGameOver() ? 0.0f : game.getPartialTicks()),
                Y_POSITION,
                WIDTH, HEIGHT, 0, 0, 0, 20, 3);
    }
}
