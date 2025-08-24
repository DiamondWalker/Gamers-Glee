package gameblock.game.paddles;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PaddleGameCodeSelectionPacket extends UpdateGamePacket<PaddlesGame> {
    String gameCode;

    public PaddleGameCodeSelectionPacket(String gameCode) {
        this.gameCode = gameCode;
    }

    public PaddleGameCodeSelectionPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(gameCode);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        gameCode = buffer.readUtf();
    }

    @Override
    public void gameUpdateReceivedOnServer(PaddlesGame game, ServerPlayer sender) {
        game.gameCode = gameCode;
    }
}
