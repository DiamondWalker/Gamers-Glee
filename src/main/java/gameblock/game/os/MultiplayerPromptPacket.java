package gameblock.game.os;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class MultiplayerPromptPacket extends UpdateGamePacket<GameblockOS> {
    public MultiplayerPromptPacket() {
        super();
    }

    public MultiplayerPromptPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {}

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {}

    @Override
    public void gameUpdateReceivedOnClient(GameblockOS game) {
        game.prompt = new MultiplayerGamePrompt(game);
    }
}
