package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BrickUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    private short brick;

    public BrickUpdatePacket(int i) {
        this.brick = (short) i;
    }

    public BrickUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeShort(brick);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        brick = buffer.readShort();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        if (game.bricks.get(this.brick) != null) {
            game.bricks.set(this.brick, null);
            game.bricksBroken++;
        }
    }
}
