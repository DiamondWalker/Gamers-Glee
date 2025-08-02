package gameblock.game.blockbreak;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import net.minecraft.network.FriendlyByteBuf;

public class BallUpdatePacket extends UpdateGamePacket<BlockBreakGame> {
    float xPos, yPos, xMotion, yMotion;
    boolean playSound;

    public BallUpdatePacket(float x, float y, float xMotion, float yMotion, boolean playSound) {
        super();
        this.xPos = x; this.yPos = y;
        this.xMotion = xMotion; this.yMotion = yMotion;
        this.playSound = playSound;
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
        buffer.writeBoolean(playSound);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        xPos = buffer.readFloat();
        yPos = buffer.readFloat();
        xMotion = buffer.readFloat();
        yMotion = buffer.readFloat();
        playSound = buffer.readBoolean();
    }

    @Override
    public void handleGameUpdate(BlockBreakGame game) {
        if (!game.isClientSide()) game.lastPacketTime = game.getGameTime();
        game.ballX = xPos;
        game.ballY = yPos;
        game.ballMoveX = xMotion;
        game.ballMoveY = yMotion;
        if (playSound && game.isClientSide()) game.playSound(GameblockSounds.BALL_BOUNCE.get());
    }
}
