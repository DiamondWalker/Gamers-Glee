package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction1D;
import net.minecraft.network.FriendlyByteBuf;

public class ServerToClientPaddleUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    Direction1D changeDir;
    float pos;

    public ServerToClientPaddleUpdatePacket(Direction1D changeDirection, float pos) {
        this.changeDir = changeDirection;
        this.pos = pos;
    }

    public ServerToClientPaddleUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(changeDir);
        buffer.writeFloat(pos);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        changeDir = buffer.readEnum(Direction1D.class);
        pos = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.getPaddleFromDirection(changeDir).pos = pos;
    }
}
