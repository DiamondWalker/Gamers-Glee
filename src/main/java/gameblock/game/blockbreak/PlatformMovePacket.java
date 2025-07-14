package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction2D;
import net.minecraft.network.FriendlyByteBuf;

public class PlatformMovePacket extends UpdateGamePacket<BlockBreakGame> {
    private byte dir;
    private float x;

    public PlatformMovePacket(float x, byte dir) {
        this.x = x;
        this.dir = dir;
    }

    public PlatformMovePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeByte(dir);
        buffer.writeFloat(x);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        dir = buffer.readByte();
        x = buffer.readFloat();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        game.platformPos = x;
        game.moveDir = dir;
    }
}
