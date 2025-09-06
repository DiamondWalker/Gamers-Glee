package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction1D;
import net.minecraft.network.FriendlyByteBuf;

public class PaddleGameStatePacket extends UpdateGamePacket<PaddlesGame> {
    Direction1D thisIsYourPaddle;

    public PaddleGameStatePacket(Direction1D playerPaddle) { // a value of center means unassigned
        thisIsYourPaddle = playerPaddle;
    }

    public PaddleGameStatePacket(FriendlyByteBuf buffer) {
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
        if (thisIsYourPaddle != Direction1D.CENTER) {
            game.whichPaddleAmI = thisIsYourPaddle;
            game.initializeGame();
        } else {
            game.stopGame();
        }
    }
}
