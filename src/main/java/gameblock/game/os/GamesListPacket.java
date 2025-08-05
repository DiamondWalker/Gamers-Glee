package gameblock.game.os;

import gameblock.game.GameInstance;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockGames;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class GamesListPacket extends UpdateGamePacket<GameblockOS> {
    private GameblockGames.Game[] games;

    public GamesListPacket(GameblockGames.Game[] games) {
        this.games = games;
    }

    public GamesListPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(games.length);
        for (GameblockGames.Game<?> game : games) {
            buffer.writeUtf(game.gameID);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        games = new GameblockGames.Game[buffer.readInt()];
        for (int i = 0; i < games.length; i++) {
            games[i] = GameblockGames.getGame(buffer.readUtf());
        }
    }

    @Override
    public void handleGameUpdate(GameblockOS os) {
        os.gameIcons = new HashSet<>();
        for (int i = 0; i < games.length; i++) {
            os.gameIcons.add(new OSIcon<>(os, games[i], i));
        }
    }
}
