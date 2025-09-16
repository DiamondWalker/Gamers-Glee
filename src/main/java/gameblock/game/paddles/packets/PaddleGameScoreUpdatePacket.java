package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesBall;
import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec2;

public class PaddleGameScoreUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    public byte leftScore;
    public byte rightScore;

    public PaddleGameScoreUpdatePacket(byte left, byte right) {
        this.leftScore = left;
        this.rightScore = right;
    }

    public PaddleGameScoreUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeByte(leftScore);
        buffer.writeByte(rightScore);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        leftScore = buffer.readByte();
        rightScore = buffer.readByte();
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.leftScore = leftScore;
        game.rightScore = rightScore;
        game.ball.resetBall();
        game.scoreTimer.reset();
        game.winSide = Direction1D.CENTER;
    }
}
