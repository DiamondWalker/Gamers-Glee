package gameblock.game.tictactoe;

import gameblock.util.MathHelper;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public enum TicTacToeShapeType {
    X,
    O;

    public void render(TicTacToeGame game, Vec2 origin, float progress, ColorF color) {
        if (this == X) {
            float line1Progress = Mth.clamp(progress / 0.4f, 0.0f, 1.0f);
            float line2Progress = Mth.clamp((progress - 0.6f) / 0.4f, 0.0f, 1.0f);

            Vec2 topLeft = new Vec2(origin.x - 15, origin.y + 15);
            Vec2 topRight = new Vec2(origin.x + 15, origin.y + 15);
            Vec2 bottomLeft = new Vec2(origin.x - 15, origin.y - 15);
            Vec2 bottomRight = new Vec2(origin.x + 15, origin.y - 15);
            if (line1Progress > 0) {
                game.drawLine(
                        topLeft.x, topLeft.y,
                        Mth.lerp(line1Progress, topLeft.x, bottomRight.x), Mth.lerp(line1Progress, topLeft.y, bottomRight.y),
                        3, true, color
                );
            }

            if (line2Progress > 0) {
                game.drawLine(
                        topRight.x, topRight.y,
                        Mth.lerp(line2Progress, topRight.x, bottomLeft.x), Mth.lerp(line2Progress, topRight.y, bottomLeft.y),
                        3, true, color
                );
            }
        } else {
            float startAngle = 0.65f;
            float endAngle = startAngle + Mth.TWO_PI * progress;
            game.drawArc(origin, 15.0f - 1.5f, 15.0f + 1.5f, startAngle, endAngle, color);
            game.drawCircle(origin.add(MathHelper.getUnitVectorFromAngle(startAngle).scale(15)), 1.5f, color);
            game.drawCircle(origin.add(MathHelper.getUnitVectorFromAngle(endAngle).scale(15)), 1.5f, color);
        }
    }
}
