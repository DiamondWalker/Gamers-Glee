package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddleGameCodePrompt;
import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.CompletionStatus;
import net.minecraft.network.FriendlyByteBuf;

public class PaddleGameCodeConfirmationPacket extends UpdateGamePacket<PaddlesGame> {
    CompletionStatus result;

    public PaddleGameCodeConfirmationPacket(CompletionStatus result) {
        this.result = result;
    }

    public PaddleGameCodeConfirmationPacket(FriendlyByteBuf buffer) {
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
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        if (result == CompletionStatus.SUCCESS) {
            if (game.prompt instanceof PaddleGameCodePrompt) game.prompt.close();
        } else {
            PaddleGameCodePrompt prompt;
            if (game.prompt instanceof PaddleGameCodePrompt codePrompt) {
                prompt = codePrompt;
            } else {
                prompt = new PaddleGameCodePrompt(game);
                game.prompt = prompt;
            }

            prompt.setFailed();
        }
    }
}
