package gameblock.game.tictactoe;

import gameblock.util.MathHelper;
import gameblock.util.TickTimer;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public final class TicTacToeShape {
    protected final TicTacToeShapeType type;
    protected final TickTimer timer;

    public TicTacToeShape(TicTacToeGame game, TicTacToeShapeType type) {
        this.type = type;
        timer = new TickTimer(game);
        timer.start(30);
    }
}
