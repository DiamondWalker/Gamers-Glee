package gameblock.game.tictactoe.packets;

import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class TicTacToeCanMakeMovePacket extends UpdateGamePacket<TicTacToeGame> {
    public boolean canMakeMove;

    public TicTacToeCanMakeMovePacket(boolean canMakeMove) {
        this.canMakeMove = canMakeMove;
    }

    public TicTacToeCanMakeMovePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(canMakeMove);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        canMakeMove = buffer.readBoolean();
    }

    @Override
    public void gameUpdateReceivedOnClient(TicTacToeGame game) {
        game.canMakeMove = canMakeMove;
    }
}
