package gameblock.game.flyingchicken;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class WingFlapPacket extends UpdateGamePacket<FlyingChickenGame> {
    long time;
    float y;

    public WingFlapPacket(long time, float y) {
        this.time = time;
        this.y = y;
    }

    public WingFlapPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeLong(time);
        buffer.writeFloat(y);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        time = buffer.readLong();
        y = buffer.readFloat();
    }

    @Override
    public void handleGameUpdate(FlyingChickenGame game) {
        game.chickenY = y;
    }
}
