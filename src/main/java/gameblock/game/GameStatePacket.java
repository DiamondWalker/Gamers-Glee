package gameblock.game;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.GameState;
import net.minecraft.network.FriendlyByteBuf;

public class GameStatePacket extends UpdateGamePacket<GameInstance> {
    private GameState state;

    public GameStatePacket(GameState state) { this.state = state; }

    public GameStatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(state);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        state = buffer.readEnum(GameState.class);
    }

    @Override
    public void handleGameUpdate(GameInstance game) {
        game.setGameState(state);
    }
}
