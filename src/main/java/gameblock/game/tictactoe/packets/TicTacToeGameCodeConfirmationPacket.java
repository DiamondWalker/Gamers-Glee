package gameblock.game.tictactoe.packets;

import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.game.tictactoe.TicTacToeGameCodePrompt;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.CompletionStatus;
import net.minecraft.network.FriendlyByteBuf;

public class TicTacToeGameCodeConfirmationPacket extends UpdateGamePacket<TicTacToeGame> {
    CompletionStatus result;

    public TicTacToeGameCodeConfirmationPacket(CompletionStatus result) {
        this.result = result;
    }

    public TicTacToeGameCodeConfirmationPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(result);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        result = buffer.readEnum(CompletionStatus.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(TicTacToeGame game) {
        if (result == CompletionStatus.SUCCESS) {
            if (game.prompt instanceof TicTacToeGameCodePrompt) game.prompt.close();
        } else {
            game.gameCode = null;
            TicTacToeGameCodePrompt prompt;
            if (game.prompt instanceof TicTacToeGameCodePrompt codePrompt) {
                prompt = codePrompt;
            } else {
                prompt = new TicTacToeGameCodePrompt(game);
                game.prompt = prompt;
            }

            prompt.setFailed();
        }
    }
}
