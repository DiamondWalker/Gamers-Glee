package gameblock.game.games.os;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.NotImplementedException;

import java.util.UUID;

public class OSNetworkManager extends NetworkManager<GameblockOS> {
    private final GameUpdateType<GameblockOS> joinGame = registerMessage(this::receiveJoinGame, true, false);
    private final GameUpdateType<GameblockOS> multiplayerPrompt = registerMessage(this::receiveMultiplayerPrompt, false, true);
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

    public void sendMultiplayerPrompt(UUID player) {
        multiplayerPrompt.sendToClient(player);
    }

    public void receiveMultiplayerPrompt(GameUpdate.Incoming packet, UUID sender) {
        game.prompt = new MultiplayerGamePrompt(game);
    }

    public void sendSelectGame(int index) {
        selectGame.sendToServer().add(index);
    }

    public void receiveSelectGame(GameUpdate.Incoming packet, UUID sender) {
        int index = packet.get(Integer.class);

        for (OSIcon icon : game.gameIcons) {
            if (icon.index == index) {
                icon.clickAction.run();
            }
        }
    }
}
