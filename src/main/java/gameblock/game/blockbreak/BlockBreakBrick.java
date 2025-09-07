package gameblock.game.blockbreak;

import gameblock.util.rendering.ColorF;

public class BlockBreakBrick {
    public int x, y;
    public int breaking = 0;
    public final int color;

    public static final float WIDTH = 10.0f;
    public static final float HEIGHT = 5.0f;

    public BlockBreakBrick(int x, int y, int color) {
        this.x = x;
        this.y = y;
        this.color = color % 7;
    }

    public ColorF getColor() {
        return switch (color) {
            case 0 -> new ColorF(0, 255, 0);
            case 1 -> new ColorF(0, 0, 255);
            case 2 -> new ColorF(0, 255, 255);
            case 3 -> new ColorF(255, 255, 0);
            case 4 -> new ColorF(255, 0, 0);
            case 5 -> new ColorF(255, 0, 255);
            case 6 -> new ColorF(255, 168, 0);
            default -> null;
        };
    }
}
