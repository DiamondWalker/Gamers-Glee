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
        BlockBreakGame.Brick brick = game.bricks.get(this.brick);
        if (brick != null) {
            if (brick.breaking == 0) brick.breaking = BlockBreakGame.BRICK_BREAK_FLASH_TIME;
            brick.confirmBreak = true;
            game.bricksBroken++;
        }
    }
}
