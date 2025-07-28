package gameblock.game.blockbreak;

import gameblock.game.Game;
import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class BallUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    float xPos, yPos, xMotion, yMotion;

    public BallUpdatePacket(float x, float y, float xMotion, float yMotion) {
        super();
        this.xPos = x; this.yPos = y;
        this.xMotion = xMotion; this.yMotion = yMotion;
    }

    public BallUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(xPos);
        buffer.writeFloat(yPos);
        buffer.writeFloat(xMotion);
        buffer.writeFloat(yMotion);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        xPos = buffer.readFloat();
        yPos = buffer.readFloat();
        xMotion = buffer.readFloat();
        yMotion = buffer.readFloat();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        if (!game.isClientSide()) game.lastPacketTime = game.getGameTime();
        game.ballX = xPos;
        game.ballY = yPos;
        game.ballMoveX = xMotion;
        game.ballMoveY = yMotion;
    }
}
