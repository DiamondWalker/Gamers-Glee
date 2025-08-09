package gameblock.game.flyingchicken;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class FlyingChickenHighScorePacket extends UpdateGamePacket<FlyingChickenGame> {
    int highScore;

    public FlyingChickenHighScorePacket(int highScore) {
        this.highScore = highScore;
    }

    public FlyingChickenHighScorePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(highScore);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        highScore = buffer.readInt();
    }

    @Override
    public void handleGameUpdate(FlyingChickenGame game) {
        game.highScore = highScore;
    }
}