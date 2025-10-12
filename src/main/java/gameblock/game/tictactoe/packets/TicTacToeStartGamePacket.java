package gameblock.game.tictactoe.packets;

import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.game.tictactoe.TicTacToeShapeType;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class TicTacToeStartGamePacket extends UpdateGamePacket<TicTacToeGame> {
    public TicTacToeShapeType shape;

    public TicTacToeStartGamePacket(TicTacToeShapeType shape) {
        this.shape = shape;
    }

    public TicTacToeStartGamePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(shape);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        shape = buffer.readEnum(TicTacToeShapeType.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(TicTacToeGame game) {
        game.myType = shape;
        game.shapes.setAll((shape) -> null);
    }
}
