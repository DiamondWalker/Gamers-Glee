package gameblock.game.games.os;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.game.NetworkManager;
import gameblock.packet.UpdateGamePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class OSNetworkManager extends NetworkManager<GameblockOS> {
    private final int joinGame = registerMessage(this::receiveJoinGame);
    private final int multiplayerPrompt = registerMessage(this::receiveMultiplayerPrompt);
    private final int selectGame = registerMessage(this::receiveSelectGame);

    public OSNetworkManager(GameblockOS game) {
        super(game);
    }

    public void sendJoinGame(String code) {
        sendToServer(joinGame, (packet) -> packet.add(code));
    }

    public void receiveJoinGame(UpdateGamePacket packet, UUID sender) {
        String gameCode = packet.get(String.class);

        if (game.players.mainPlayer() instanceof ServerPlayer serverPlayer) { // TODO: eventually this handle method will be updated to receive a player instance as an argument. When it is use that instead of getHostPlayer()
            for (ServerPlayer player : serverPlayer.getServer().getPlayerList().getPlayers()) {
                GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                if (cap != null && cap.isPlaying()) {
                    GameInstance joinGame = cap.getGame();
                    if (joinGame.getGameCode().matches(gameCode)) {
                        if (joinGame.players.addPlayer(serverPlayer) != null) return;
                    }
                }
            }
        } else {
            throw new IllegalStateException("Host player should be an instance of ServerPlayer, but it isn't! Was the packet handled on the client?");
        }
    }

    public void sendMultiplayerPrompt(ServerPlayer player) {
        sendToClient(multiplayerPrompt, (packet) -> {}, player);
    }

    public void receiveMultiplayerPrompt(UpdateGamePacket packet, UUID sender) {
        game.prompt = new MultiplayerGamePrompt(game);
    }

    public void sendSelectGame(int index) {
        sendToServer(selectGame, (packet) -> packet.add(index));
    }

    public void receiveSelectGame(UpdateGamePacket packet, UUID sender) {
        int index = packet.get(Integer.class);

        for (OSIcon icon : game.gameIcons) {
            if (icon.index == index) {
                icon.clickAction.run();
            }
        }
    }
}
