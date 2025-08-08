package gameblock.game;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.GameState;
import net.minecraft.network.FriendlyByteBuf;

public class GameRestartPacket extends UpdateGamePacket<GameInstance> {
    public GameRestartPacket() {
    }

    public GameRestartPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
    }

    @Override
    public void handleGameUpdate(GameInstance game) {
        game.restart();
    }
}
