package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction1D;
import net.minecraft.network.FriendlyByteBuf;

public class GameStartPacket extends UpdateGamePacket<PaddlesGame> {
    Direction1D thisIsYourPaddle;

    public GameStartPacket(Direction1D playerPaddle) {
        thisIsYourPaddle = playerPaddle;
    }

    public GameStartPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(thisIsYourPaddle);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        thisIsYourPaddle = buffer.readEnum(Direction1D.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.gameStarted = true;
        game.whichPaddleAmI = thisIsYourPaddle;
    }
}
