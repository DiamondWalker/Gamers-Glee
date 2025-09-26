package gameblock.game.tictactoe;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.MathHelper;
import gameblock.util.datastructure.TileGrid2D;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.Vec2i;
import gameblock.util.rendering.ColorF;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public class TicTacToeGame extends GameInstance<TicTacToeGame, TicTacToePlayerData> {
    // COMMON DATA
    public String gameCode = null;
    TileGrid2D<TicTacToeShape> shapes = new TileGrid2D<>(-1, 1, -1, 1);

    // CLIENT DATA
    TicTacToeShapeType myType = TicTacToeShapeType.O;

    public TicTacToeGame(Player player) {
        super(player, GameblockGames.TIC_TAC_TOE_GAME, TicTacToePlayerData::new);
        if (isClientSide()) prompt = new TicTacToeGameCodePrompt(this);
    }

    @Override
    protected void tick() {

    }

    @Override
    public String getGameCode() {
        return gameCode;
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT) {
            Vec2i mouseOver = getSlotAtCoordinates(clickCoordinates);
            if (shapes.get(mouseOver.getX(), mouseOver.getY()) == null) {
                shapes.set(mouseOver.getX(), mouseOver.getY(), new TicTacToeShape(this, myType));
            }
        }
    }

    @Override
    public void render() {
        if (getPlayerCount() == 2) {
            drawLine(-60 + 3, 20, 60 - 3, 20, 3, true, ColorF.WHITE);
            drawLine(-60 + 3, -20, 60 - 3, -20, 3, true, ColorF.WHITE);
            drawLine(20, -60 + 3, 20, 60 - 3, 3, true, ColorF.WHITE);
            drawLine(-20, -60 + 3, -20, 60 - 3, 3, true, ColorF.WHITE);

            shapes.forEach((Vec2i vec, TicTacToeShape shape) -> {
                if (shape != null) {
                    shape.type.render(this, new Vec2(vec.getX(), vec.getY()).scale(40), shape.timer.getProgress(), ColorF.WHITE);
                }
            });

            Vec2i hoveringOver = getSlotAtCoordinates(getMouseCoordinates());
            if (Math.abs(hoveringOver.getX()) <= 1 && Math.abs(hoveringOver.getY()) <= 1) {
                if (shapes.get(hoveringOver.getX(), hoveringOver.getY()) == null) {
                    myType.render(this, new Vec2(hoveringOver.getX(), hoveringOver.getY()).scale(40), 1.0f, new ColorF(0.4f));
                }
            }
        }
    }

    private Vec2i getSlotAtCoordinates(Vec2 pos) {
        return new Vec2i(Math.round(pos.x / 40), Math.round(pos.y / 40));
    }
}
