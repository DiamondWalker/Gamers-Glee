package gameblock.game.blockbreak;

import gameblock.game.Game;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BallLaunchPacket extends UpdateGamePacket<BlockBreakGame> {
    public BallLaunchPacket() {
        super();
    }

    public BallLaunchPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {

    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {

    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        game.launchBall();
    }
}
