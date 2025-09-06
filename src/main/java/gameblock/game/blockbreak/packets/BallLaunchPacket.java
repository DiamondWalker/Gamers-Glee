package gameblock.game.blockbreak.packets;

import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class BallLaunchPacket extends UpdateGamePacket<BlockBreakGame> {
    float xPos;
    float xMotion;

    public BallLaunchPacket(float launchX, float initialXMotion) {
        super();
        this.xPos = launchX;
        this.xMotion = initialXMotion;
    }

    public BallLaunchPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(xPos);
        buffer.writeFloat(xMotion);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        xPos = buffer.readFloat();
        xMotion = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnServer(BlockBreakGame game, ServerPlayer sender) {
        game.lastPacketTime = game.getGameTime();
        game.ballX = xPos;
        game.launchBall(xMotion);
    }
}
