package gameblock.game.paddles;

import net.minecraft.world.phys.Vec2;

public class PaddlesBall {
    public static final float SIZE = 3.0f;

    public Vec2 pos = Vec2.ZERO;
    public Vec2 oldPos = Vec2.ZERO;
    public Vec2 motion = Vec2.ZERO;
    public float speed = 1.5f;
}
