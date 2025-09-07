package gameblock.game.blockbreak.packets;

import gameblock.game.blockbreak.BlockBreakGame;
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
    public void gameUpdateReceivedOnClient(BlockBreakGame game) {
        if (game.blocks.get(this.brick) != null) {
            if (game.blocks.get(this.brick).breaking == 0) {
                game.spawnBrickBreakParticles(game.blocks.get(this.brick));
            }
            game.blocksBroken++;
            game.blocks.set(this.brick, null);
        }
    }
}
