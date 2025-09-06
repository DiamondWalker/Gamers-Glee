package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.CompletionStatus;
import gameblock.util.MultiplayerHelper;
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
        CompletionStatus result;
        if (MultiplayerHelper.findGameWithGameCode(sender.getServer(), gameCode) == null) {
            game.gameCode = gameCode;
            result = CompletionStatus.SUCCESS;
        } else {
            result = CompletionStatus.FAIL;
        }

        GameblockPackets.sendToPlayer(sender, new PaddleGameCreationResultPacket(result));
    }
}
