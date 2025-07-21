package gameblock.game;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class GameOverPacket extends UpdateGamePacket<Game> {
    public GameOverPacket() { super(); }

    public GameOverPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {}

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {}

    @Override
    public void handleGameUpdate(Game game) {
        game.gameOver();
    }
}
