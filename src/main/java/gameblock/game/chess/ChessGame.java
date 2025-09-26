package gameblock.game.chess;

import gameblock.game.GameInstance;
import gameblock.game.GamePlayer;
import gameblock.registry.GameblockGames;
import gameblock.util.rendering.ColorF;
import net.minecraft.world.entity.player.Player;

public class ChessGame extends GameInstance<ChessGame, GamePlayer.GamePlayerData> {
    public ChessGame(Player player) {
        super(player, GameblockGames.CHESS_GAME, GamePlayer.GamePlayerData::new);
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render() {
        drawRectangle(0, 0, GameInstance.SCREEN_WIDTH, GameInstance.SCREEN_HEIGHT, new ColorF(1.0f, 1.0f, 0.8f), 0);

        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                if (x % 2 != y % 2) {
                    drawRectangle((-3.5f + x) * 15, (-3.5f + y) * 15, 15, 15, ColorF.WHITE, 0);
                } else {
                    drawRectangle((-3.5f + x) * 15, (-3.5f + y) * 15, 15, 15, ColorF.BLACK, 0);
                }
            }
        }
    }
}
