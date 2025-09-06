package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction1D;
import net.minecraft.network.FriendlyByteBuf;

public class ServerToClientPaddleUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    float pos;

    public ServerToClientPaddleUpdatePacket(float pos) {
        this.pos = pos;
    }

    public ServerToClientPaddleUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(pos);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        pos = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.otherPaddleUpdatePos = pos;
    }
}
