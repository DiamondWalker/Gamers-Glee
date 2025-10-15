package gameblock.game.tictactoe.packets;

import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class TicTacToeStopGamePacket extends UpdateGamePacket<TicTacToeGame> {
    public TicTacToeStopGamePacket() {

    }

    public TicTacToeStopGamePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {

    }

    @Override
    public void gameUpdateReceivedOnClient(TicTacToeGame game) {
        game.myType = null;
    }
}
