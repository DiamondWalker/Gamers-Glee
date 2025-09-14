package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;

public class PaddleGameBallUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    public Vec2 pos;
    public Vec2 motion;

    public PaddleGameBallUpdatePacket(Vec2 pos, Vec2 motion) {
        this.pos = pos;
        this.motion = motion;
    }

    public PaddleGameBallUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(pos.x);
        buffer.writeFloat(pos.y);

        buffer.writeFloat(motion.x);
        buffer.writeFloat(motion.y);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        pos = new Vec2(buffer.readFloat(), buffer.readFloat());
        motion = new Vec2(buffer.readFloat(), buffer.readFloat());
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.ball.pos = pos;
        game.ball.motion = motion;
    }

    @Override
    public void gameUpdateReceivedOnServer(PaddlesGame game, ServerPlayer sender) {
        game.ball.pos = pos;
        game.ball.motion = motion;
        game.sendToAllPlayers(new PaddleGameBallUpdatePacket(pos, motion), null);
    }
}
