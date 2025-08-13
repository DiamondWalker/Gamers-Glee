package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BlockUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    private short brick;

    public BlockUpdatePacket(int i) {
        this.brick = (short) i;
    }

    public BlockUpdatePacket(FriendlyByteBuf buffer) {
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
        if (game.blocks.get(this.brick) != null) {
            if (game.blocks.get(this.brick).breaking == 0) {
                game.spawnBrickBreakParticles(game.blocks.get(this.brick));
                //game.playSound(GameblockSounds.BRICK_BREAK.get());
            }
            game.blocks.set(this.brick, null);
            game.blocksBroken++;
        }
    }
}
