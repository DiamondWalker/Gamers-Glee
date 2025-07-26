package gameblock.game.blockbreak;

import gameblock.game.Game;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BallLaunchPacket extends UpdateGamePacket<BlockBreakGame> {
    float xMotion;

    public BallLaunchPacket(float initialXMotion) {
        super();
        this.xMotion = initialXMotion;
    }

    public BallLaunchPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(xMotion);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        xMotion = buffer.readFloat();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        game.launchBall(xMotion);
    }
}
