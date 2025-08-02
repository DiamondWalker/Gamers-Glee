package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
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
            if (game.bricks.get(this.brick).breaking == 0) {
                game.spawnBrickBreakParticles(game.bricks.get(this.brick));
                //game.playSound(GameblockSounds.BRICK_BREAK.get());
            }
            game.bricks.set(this.brick, null);
            game.bricksBroken++;
        }
    }
}
