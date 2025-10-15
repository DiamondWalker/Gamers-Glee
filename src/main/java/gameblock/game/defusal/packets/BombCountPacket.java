package gameblock.game.defusal.packets;

import gameblock.game.defusal.DefusalGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BombCountPacket extends UpdateGamePacket<DefusalGame> {
    private int bombCount;

    public BombCountPacket(int bombCount) {
        this.bombCount = bombCount;
    }

    public BombCountPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeByte(bombCount);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        bombCount = buffer.readByte();
    }

    @Override
    public void gameUpdateReceivedOnClient(DefusalGame game) {
        game.bombCount = bombCount;
    }
}
