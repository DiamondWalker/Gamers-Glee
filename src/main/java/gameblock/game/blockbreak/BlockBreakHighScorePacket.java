package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BlockBreakHighScorePacket extends UpdateGamePacket<BlockBreakGame> {
    int highScore;

    public BlockBreakHighScorePacket(int highScore) {
        this.highScore = highScore;
    }

    public BlockBreakHighScorePacket(FriendlyByteBuf buf) {
        super(buf);
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
    public void handleGameUpdate(BlockBreakGame game) {
        game.highScore = highScore;
    }
}
