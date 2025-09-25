package gameblock.game.tictactoe;

public enum TicTacToeShapeType {
    X(30),
    O(50);

    public final int ticksToDraw;

    TicTacToeShapeType(int time) {
        this.ticksToDraw = time;
    }
}
