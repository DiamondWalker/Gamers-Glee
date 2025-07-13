package gameblock.util;

import net.minecraft.core.Vec3i;
import org.joml.Vector2i;

public enum Direction2D {
    UP(0, 1),
    DOWN(0, -1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    private final Vec2i normal;

    Direction2D(int x, int y) {
        normal = new Vec2i(x, y);
    }

    public Direction2D getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
        };
    }

    public Vec2i getNormal() {
        return normal;
    }
}
