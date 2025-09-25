package gameblock.game.tictactoe;

import gameblock.util.MathHelper;
import gameblock.util.TickTimer;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class TicTacToeShape {
    protected final TicTacToeGame game;
    protected final TicTacToeShapeType type;
    protected final TickTimer timer;

    public TicTacToeShape(TicTacToeGame game, TicTacToeShapeType type) {
        this.game = game;
        this.type = type;
        timer = new TickTimer(game);
        timer.start(type.ticksToDraw);
    }

    protected void render(Vec2 origin) {
        float progress = timer.getProgress();
        if (type == TicTacToeShapeType.X) {
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
                        3, true, ColorF.WHITE
                );
            }

            if (line2Progress > 0) {
                game.drawLine(
                        topRight.x, topRight.y,
                        Mth.lerp(line2Progress, topRight.x, bottomLeft.x), Mth.lerp(line2Progress, topRight.y, bottomLeft.y),
                        3, true, ColorF.WHITE
                );
            }
        } else {
            float startAngle = 0.2f;
            float endAngle = startAngle + Mth.TWO_PI * progress;
            game.drawArc(origin, 15.0f - 1.5f, 15.0f + 1.5f, startAngle, endAngle, ColorF.WHITE);
            game.drawCircle(origin.add(MathHelper.getUnitVectorFromAngle(startAngle).scale(15)), 1.5f, ColorF.WHITE);
            game.drawCircle(origin.add(MathHelper.getUnitVectorFromAngle(endAngle).scale(15)), 1.5f, ColorF.WHITE);
        }
    }
}
