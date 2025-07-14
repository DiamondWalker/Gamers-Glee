package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BrickUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    private short brick;
    private byte hitsLeft;

    public BrickUpdatePacket(int i, int hitsLeft) {
        this.brick = (short) i;
        this.hitsLeft = (byte) hitsLeft;
    }

    public BrickUpdatePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeShort(brick);
        buffer.writeByte(hitsLeft);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        brick = buffer.readShort();
        hitsLeft = buffer.readByte();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        BlockBreakGame.Brick brick = game.bricks.get(this.brick);
        if (brick != null) {
            brick.hitsLeft = hitsLeft;
            if (brick.hitsLeft <= 0) game.bricks.set(this.brick, null);
        }
    }
}
