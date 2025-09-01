package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.Direction1D;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ClientToServerPaddleUpdatePacket extends UpdateGamePacket<PaddlesGame> {
    float pos;

    public ClientToServerPaddleUpdatePacket(float pos) {
        this.pos = pos;
    }

    public ClientToServerPaddleUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeFloat(pos);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        pos = buffer.readFloat();
    }

    @Override
    public void gameUpdateReceivedOnServer(PaddlesGame game, ServerPlayer sender) {
        Direction1D changeDirection = game.getDirectionFromPlayer(sender);
        game.getPaddleFromDirection(changeDirection).pos = pos;
        game.forEachPlayerExcluding(
                (Player player) -> GameblockPackets.sendToPlayer((ServerPlayer) player, new ServerToClientPaddleUpdatePacket(changeDirection, pos)),
                sender
        );
    }
}
