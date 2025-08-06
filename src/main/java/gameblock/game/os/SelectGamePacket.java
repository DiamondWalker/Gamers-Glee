package gameblock.game.os;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockGames;
import net.minecraft.network.FriendlyByteBuf;

public class SelectGamePacket extends UpdateGamePacket<GameblockOS> {
    private GameblockGames.Game<?> game;

    public SelectGamePacket(GameblockGames.Game<?> game) {
        this.game = game;
    }

    public SelectGamePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(game.gameID);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        game = GameblockGames.getGame(buffer.readUtf());
    }

    @Override
    public void handleGameUpdate(GameblockOS game) {
        game.selectGameAndSentToClient(this.game);
    }
}
