package gameblock.util.physics;

import gameblock.util.MathHelper;

public enum Direction1D {
    LEFT(-1),
    CENTER(0),
    RIGHT(1);

    private final int component;

    Direction1D(int dir) {
        this.component = dir;
    }

    public int getComponent() {
        return component;
    }

    public Direction1D getOpposite() {
        if (this == LEFT) return RIGHT;
        if (this == RIGHT) return LEFT;
        return CENTER;
    }

    public static Direction1D getFromCoordinate(int x) {
        byte component = MathHelper.getSign(x);
        if (component < 0) return LEFT;
        if (component > 0) return RIGHT;
        return CENTER;
    }

    public static Direction1D getFromCoordinate(float x) {
        byte component = MathHelper.getSign(x);
        if (component < 0) return LEFT;
        if (component > 0) return RIGHT;
        return CENTER;
    }
}
