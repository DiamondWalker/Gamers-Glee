package gameblock.game.blockbreak.packets;

import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

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
    public void gameUpdateReceivedOnClient(BlockBreakGame game) {
        if (game.getGameTime() - game.clientToPacketBallUpdateTime < 4) return;

        game.ball.x = xPos;
        game.ball.y = yPos;
        game.ball.moveX = xMotion;
        game.ball.moveY = yMotion;
        if (playSound && game.isClientSide()) game.playSound(GameblockSounds.BALL_BOUNCE.get());
    }

    @Override
    public void gameUpdateReceivedOnServer(BlockBreakGame game, ServerPlayer sender) {
        game.lastPacketTime = game.getGameTime();

        game.ball.x = xPos;
        game.ball.y = yPos;
        game.ball.moveX = xMotion;
        game.ball.moveY = yMotion;
        if (playSound && game.isClientSide()) game.playSound(GameblockSounds.BALL_BOUNCE.get());
    }
}
