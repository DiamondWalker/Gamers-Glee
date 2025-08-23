package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class ServerToClientPaddleUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    float leftPaddlePos;
    float rightPaddlePos;

    public ServerToClientPaddleUpdatePacket(float left, float right) {
        leftPaddlePos = left;
        rightPaddlePos = right;
    }

    public ServerToClientPaddleUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(leftPaddlePos);
        buffer.writeFloat(rightPaddlePos);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        leftPaddlePos = buffer.readFloat();
        rightPaddlePos = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.leftPaddle.pos = leftPaddlePos;
        game.rightPaddle.pos = rightPaddlePos;
    }
}
