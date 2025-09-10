package gameblock.util.physics.collision;

import gameblock.game.GameInstance;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.lang3.NotImplementedException;

public class CircleHitbox extends Hitbox {
    float radius;

    public CircleHitbox(Vec2 origin, float radius) {
        super(origin);
        this.radius = radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean containsPoint(Vec2 point) {
        return point.add(origin.negated()).length() <= radius;
    }

    @Override
    public void render(GameInstance<?> game) {
        throw new NotImplementedException();
    }
}
