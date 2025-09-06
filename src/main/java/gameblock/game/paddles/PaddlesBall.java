package gameblock.game.paddles;

import gameblock.util.ColorF;
import net.minecraft.world.phys.Vec2;

public class PaddlesBall {
    public static final float SIZE = 3.0f;

    Vec2 pos = Vec2.ZERO;
    Vec2 oldPos = Vec2.ZERO;
    Vec2 motion = Vec2.ZERO;
    float speed = 1.5f;
}
