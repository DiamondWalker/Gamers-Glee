package gameblock.util;

import gameblock.game.GameInstance;
import net.minecraft.world.phys.Vec2;

public class RectangleHitbox extends Hitbox {
    float width, height;

    public RectangleHitbox(float x, float y, float sideLength) {
        this(new Vec2(x, y), sideLength);
    }

    public RectangleHitbox(float x, float y, float width, float height) {
        this(new Vec2(x, y), width, height);
    }

    public RectangleHitbox(Vec2 origin, float sideLength) {
        this(origin, sideLength, sideLength);
    }

    public RectangleHitbox(Vec2 origin, float width, float height) {
        super(origin);
        this.width = width;
        this.height = height;
    }

    public void setDimensions(float w, float h) {
        this.width = w;
        this.height = h;
    }

    @Override
    public boolean containsPoint(Vec2 point) {
        return Math.abs(point.x - origin.x) <= width / 2 && Math.abs(point.y - origin.y) <= height / 2;
    }

    @Override
    public void render(GameInstance<?> game) {
        game.renderer.drawRectangle(origin.x, origin.y, width, height, new ColorF(1.0f, 0.0f, 0.0f, 0.5f), 0);
    }
}
