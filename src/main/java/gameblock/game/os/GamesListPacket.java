package gameblock.game.os;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameRegistry;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;

public class GamesListPacket extends UpdateGamePacket<GameblockOS> {
    private GameRegistry.Game[] games;

    public GamesListPacket(GameRegistry.Game[] games) {
        this.games = games;
    }

    public GamesListPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(games.length);
        for (int i = 0; i < games.length; i++) {
            buffer.writeResourceLocation(games[i].gameID);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        games = new GameRegistry.Game[buffer.readInt()];
        for (int i = 0; i < games.length; i++) {
            games[i] = GameRegistry.getGame(buffer.readResourceLocation());
        }
    }

    @Override
    public void handleGameUpdate(GameblockOS os) {
        os.gamesFound = new HashSet<>();
        for (GameRegistry.Game game : games) os.gamesFound.add(game);
    }
}
