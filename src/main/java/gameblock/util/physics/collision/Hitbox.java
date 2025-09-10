package gameblock.util.physics.collision;

import gameblock.game.GameInstance;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.NotImplementedException;

public abstract class Hitbox {
    protected Vec2 origin;

    public Hitbox(Vec2 origin) {
        this.origin = origin;
    }

    public void setPosition(Vec2 pos) {
        this.origin = pos;
    }

    public abstract boolean containsPoint(Vec2 point);

    public abstract void render(GameInstance<?> game);

    public static boolean areColliding(Hitbox hitbox1, Hitbox hitbox2) {
        if (hitbox1 instanceof CircleHitbox circle1 && hitbox2 instanceof CircleHitbox circle2) {
            return circle1.origin.add(circle2.origin.negated()).length() <= circle1.radius + circle2.radius;
        }
        if (hitbox1 instanceof RectangleHitbox rectangle1 && hitbox2 instanceof RectangleHitbox rectangle2) {
            return Math.abs(rectangle1.origin.x - rectangle2.origin.x) <= rectangle1.width / 2 + rectangle2.width / 2
            && Math.abs(rectangle1.origin.y - rectangle2.origin.y) <= rectangle1.height / 2 + rectangle2.height / 2;
        }

        if (hitbox1 instanceof RectangleHitbox rectangle) hitbox1 = new RotatableRectangleHitbox(rectangle.origin, rectangle.width, rectangle.height, 0);
        if (hitbox2 instanceof RectangleHitbox rectangle) hitbox2 = new RotatableRectangleHitbox(rectangle.origin, rectangle.width, rectangle.height, 0);

        if (hitbox1 instanceof RotatableRectangleHitbox rectangle1) {
            for (Vec2 point : rectangle1.getCornerPoints()) {
                if (hitbox2.containsPoint(point)) return true;
            }
        } else {
            throw new NotImplementedException("The case where one hitbox is circular and the other is rectangular is not yet implemented");
        }

        if (hitbox2 instanceof RotatableRectangleHitbox rectangle2) {
            for (Vec2 point : rectangle2.getCornerPoints()) {
                if (hitbox1.containsPoint(point)) return true;
            }
        } else {
            throw new NotImplementedException("The case where one hitbox is circular and the other is rectangular is not yet implemented");
        }

        throw new  IllegalStateException("Somehow, the hitbox check did not return a result. This shouldn't happen!");
    }
}
