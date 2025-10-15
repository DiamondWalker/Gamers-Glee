package gameblock.game.tictactoe.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.game.paddles.packets.PaddleGameCodeConfirmationPacket;
import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.CompletionStatus;
import gameblock.util.MultiplayerHelper;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class TicTacToeGameCodeSelectionPacket extends UpdateGamePacket<TicTacToeGame> {
    String gameCode;

    public TicTacToeGameCodeSelectionPacket(String gameCode) {
        this.gameCode = gameCode;
    }

    public TicTacToeGameCodeSelectionPacket(FriendlyByteBuf buffer) {
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
    public void gameUpdateReceivedOnServer(TicTacToeGame game, ServerPlayer sender) {
        CompletionStatus result;
        if (MultiplayerHelper.findGameWithGameCode(sender.getServer(), gameCode) == null) {
            game.gameCode = gameCode;
            result = CompletionStatus.SUCCESS;
        } else {
            result = CompletionStatus.FAIL;
        }

        GameblockPackets.sendToPlayer(sender, new TicTacToeGameCodeConfirmationPacket(result));
    }
}
