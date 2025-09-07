package gameblock.util.physics;

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
}
