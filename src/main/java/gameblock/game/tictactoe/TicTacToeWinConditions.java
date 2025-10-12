package gameblock.game.tictactoe;

import gameblock.util.physics.Vec2i;
import net.minecraft.world.phys.Vec2;

public enum TicTacToeWinConditions {
    HORIZONTAL_TOP(new Vec2(-70.0f, 40.0f), new Vec2(70.0f, 40.0f), new Vec2i(-1, 1), new Vec2i(0, 1), new Vec2i(1, 1)),
    HORIZONTAL_CENTER(new Vec2(-70.0f, 0.0f), new Vec2(70.0f, 0.0f), new Vec2i(-1, 0), new Vec2i(0, 0), new Vec2i(1, 0)),
    HORIZONTAL_BOTTOM(new Vec2(-70.0f, -40.0f), new Vec2(70.0f, -40.0f), new Vec2i(-1, 1), new Vec2i(0, 1), new Vec2i(1, 1)),

    VERTICAL_LEFT(new Vec2(-40.0f, 70.0f), new Vec2(-40.0f, -70.0f), new Vec2i(-1, 1), new Vec2i(-1, 0), new Vec2i(-1, -1)),
    VERTICAL_CENTER(new Vec2(0.0f, 70.0f), new Vec2(0.0f, -70.0f), new Vec2i(0, 1), new Vec2i(0, 0), new Vec2i(0, -1)),
    VERTICAL_RIGHT(new Vec2(40.0f, 70.0f), new Vec2(40.0f, -70.0f), new Vec2i(1, 1), new Vec2i(1, 0), new Vec2i(1, -1)),

    DIAGONAL_LEFT_RIGHT(new Vec2(-70.0f, 70.0f), new Vec2(70.0f, -70.0f), new Vec2i(-1, 1), new Vec2i(0, 0), new Vec2i(1, -1)),
    DIAGONAL_RIGHT_LEFT(new Vec2(70.0f, 70.0f), new Vec2(-70.0f, -70.0f), new Vec2i(1, 1), new Vec2i(0, 0), new Vec2i(-1, -1));



    public final Vec2 startLine;
    public final Vec2 endLine;
    public final Vec2i pos1;
    public final Vec2i pos2;
    public final Vec2i pos3;

    TicTacToeWinConditions(Vec2 start, Vec2 end, Vec2i pos1, Vec2i pos2, Vec2i pos3) {
        startLine = start;
        endLine = end;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.pos3 = pos3;
    }
}
