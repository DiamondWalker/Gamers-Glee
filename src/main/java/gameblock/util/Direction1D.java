package gameblock.util;

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
}
