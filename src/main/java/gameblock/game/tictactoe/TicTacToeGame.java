package gameblock.game.tictactoe;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class TicTacToeGame extends GameInstance<TicTacToeGame> {
    public TicTacToeGame(Player player) {
        super(player, GameblockGames.TIC_TAC_TOE_GAME);
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render() {
        //drawCircle(0.0f, 0.0f, 100.0f, new ColorF(1.0f));
        //drawRing(0.0f, 0.0f, 5.0f, 10.0f, new ColorF(1.0f));
        //drawArc(0.0f, 0.0f, 5.0f, 10.0f, Mth.PI + 0.1f, Mth.HALF_PI * 3 - 0.1f, new ColorF(1.0f));
    }
}
