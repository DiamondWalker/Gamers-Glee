package gameblock.game.games.os;

import gameblock.game.*;
import gameblock.registry.GameblockCustomRegistries;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OSNetworkManager extends NetworkManager<GameblockOS> {
    private final GameUpdateType<GameblockOS> joinGame = registerMessage(this::receiveJoinGame, true, false);
    private final GameUpdateType<GameblockOS> multiplayerPrompt = registerMessage(this::receiveMultiplayerPrompt, false, true);
    private final GameUpdateType<GameblockOS> gamesList = registerMessage(this::receiveGamesList, false, true);
    private final GameUpdateType<GameblockOS> selectGame = registerMessage(this::receiveSelectGame, true, false);

    public OSNetworkManager(GameblockOS game) {
        super(game);
    }

    public void sendJoinGame(String code) {
        joinGame.sendToServer().add(code);
    }

    public void receiveJoinGame(GameUpdate.Incoming packet, UUID sender) {
        String gameCode = packet.get(String.class);

        throw new NotImplementedException();
    }

    public void sendMultiplayerPrompt() {
        multiplayerPrompt.sendToAllClients();
    }

    public void receiveMultiplayerPrompt(GameUpdate.Incoming packet, UUID sender) {
        game.prompt = new MultiplayerGamePrompt(game);
    }

    public void sendGamesList(List<Game<?>> games) {
        GameUpdate.Outgoing update = gamesList.sendToAllClients().add(games.size());

        for (Game<?> foundGame : games) {
            update.add(foundGame.getId());
        }
    }

    public void receiveGamesList(GameUpdate.Incoming update, UUID sender) {
        int gameCount = update.get(Integer.class);
        ArrayList<Game<?>> games = new ArrayList<>();

        for (int i = 0; i < gameCount; i++) {
            Game<?> foundGame = GameblockCustomRegistries.GAMES.get().getValue(update.get(ResourceLocation.class));
            games.add(foundGame);
        }

        game.populateIconsFromGamesList(games);
    }

    public void sendSelectGame(int index) {
        selectGame.sendToServer().add(index);
    }

    public void receiveSelectGame(GameUpdate.Incoming packet, UUID sender) {
        int index = packet.get(Integer.class);

        game.gameIcons.get(index).clickAction.run();
    }
}
