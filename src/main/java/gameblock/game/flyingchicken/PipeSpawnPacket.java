package gameblock.game.flyingchicken;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class PipeSpawnPacket extends UpdateGamePacket<FlyingChickenGame> {
    float x;
    float y;

    public PipeSpawnPacket(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public PipeSpawnPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(x);
        buffer.writeFloat(y);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        x = buffer.readFloat();
        y = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnClient(FlyingChickenGame game) {
        game.pipes.enqueue(new FlyingChickenGame.Pipe(x, y));
    }
}
