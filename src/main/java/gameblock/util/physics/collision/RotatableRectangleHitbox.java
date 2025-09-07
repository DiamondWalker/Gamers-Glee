package gameblock.util.physics.collision;

import gameblock.game.GameInstance;
import gameblock.util.rendering.ColorF;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.NotImplementedException;

public class RotatableRectangleHitbox extends Hitbox {
    float width, height;
    float angle;

    public RotatableRectangleHitbox(Vec2 origin, float width, float height, float angle) {
        super(origin);
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    public void setDimensions(float w, float h) {
        this.width = w;
        this.height = h;
    }

    public void setRotation(float angle) {
        this.angle = angle;
    }

    @Override
    public boolean containsPoint(Vec2 point) {
        return false;
    }

    @Override
    public void render(GameInstance<?> game) {
        game.drawRectangle(origin.x, origin.y, width, height, new ColorF(1.0f, 0.0f, 0.0f, 0.5f), angle);
    }

    public Vec2[] getCornerPoints() {
        throw new NotImplementedException();
    }
}
