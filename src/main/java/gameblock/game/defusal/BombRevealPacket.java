package gameblock.game.defusal;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

public class BombRevealPacket extends UpdateGamePacket<DefusalGame> {
    private Vec2i[] bombs;

    public BombRevealPacket(Vec2i[] bombs) {
        this.bombs = bombs;
    }

    public BombRevealPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(bombs.length);
        for (Vec2i bomb : bombs) {
            buffer.writeShort(bomb.getX());
            buffer.writeShort(bomb.getY());
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        bombs = new Vec2i[buffer.readInt()];
        for (int i = 0; i < bombs.length; i++) {
            bombs[i] = new Vec2i(buffer.readShort(), buffer.readShort());
        }
    }

    @Override
    public void handleGameUpdate(DefusalGame game) {
        for (Vec2i bomb : bombs) {
            game.tiles.get(bomb.getX(), bomb.getY()).setBomb();
        }
    }
}
