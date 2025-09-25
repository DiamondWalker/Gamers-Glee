package gameblock.game.tictactoe;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public class TicTacToeGame extends GameInstance<TicTacToeGame> {
    TicTacToeShape temp = new TicTacToeShape(this, TicTacToeShapeType.X);

    public TicTacToeGame(Player player) {
        super(player, GameblockGames.TIC_TAC_TOE_GAME);
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render() {
        drawLine(-60, 20, 60, 20, 3, true, ColorF.WHITE);
        drawLine(-60, -20, 60, -20, 3, true, ColorF.WHITE);
        drawLine(20, -60, 20, 60, 3, true, ColorF.WHITE);
        drawLine(-20, -60, -20, 60, 3, true, ColorF.WHITE);

        temp.render(Vec2.ZERO);
    }
}
