package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class ScoreUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    int score;
    long seconds;

    public ScoreUpdatePacket(int score, long secondsElapsed) {
        super();
        this.score = score;
        this.seconds = secondsElapsed;
    }

    public ScoreUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(score);
        buffer.writeLong(seconds);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        score = buffer.readInt();
        seconds = buffer.readLong();
    }

    @Override
    public void gameUpdateReceivedOnClient(BlockBreakGame game) {
        game.score = score;
        game.timeSinceLaunch = seconds * 20;
    }
}
