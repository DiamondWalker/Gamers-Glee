package gameblock.game.flyingchicken;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import net.minecraft.network.FriendlyByteBuf;

public class ScorePacket extends UpdateGamePacket<FlyingChickenGame> {
    int score;

    public ScorePacket(int score) {
        this.score = score;
    }

    public ScorePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(score);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        score = buffer.readInt();
    }

    @Override
    public void handleGameUpdate(FlyingChickenGame game) {
        game.score = score;
        game.lastScoreTime = game.getGameTime();
        game.playSound(GameblockSounds.FLYING_CHICKEN_SCORE.get());
    }
}
