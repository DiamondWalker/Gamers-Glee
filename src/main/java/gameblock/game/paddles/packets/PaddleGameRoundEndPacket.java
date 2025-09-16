package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.FriendlyByteBuf;

public class PaddleGameRoundEndPacket extends UpdateGamePacket<PaddlesGame> {
    public Direction1D winningSide;

    public PaddleGameRoundEndPacket(Direction1D dir) {
        winningSide = dir;
    }

    public PaddleGameRoundEndPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(winningSide);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        winningSide = buffer.readEnum(Direction1D.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.scoreTimer.start();
        game.winSide = winningSide;
    }
}
