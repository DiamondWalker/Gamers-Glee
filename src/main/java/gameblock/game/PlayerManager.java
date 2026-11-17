package gameblock.game;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerManager {
    private final GameInstance game;
    private final HashMap<UUID, Player> players = new HashMap<>();
    private final Player mainPlayer;

    public PlayerManager(GameInstance game, Player mainPlayer) {
        this.game = game;
        this.mainPlayer = mainPlayer;
        players.put(mainPlayer.getUUID(), mainPlayer);
    }

    public final Player mainPlayer() {
        return mainPlayer;
    }

    public final Player getPlayer(UUID id) {
        return players.get(id);
    }

    public Set<UUID> getPlayers() {
        return players.keySet();
    }

    public void forEachPlayerEntity(Consumer<Player> action) {
        for (Player player : players.values()) {
            if (player != null) action.accept(player);
        }
    }

    public final UUID addPlayer(ServerPlayer player) {
        if (players.size() < game.getMaxPlayers()) {
            GameCapability cap = player.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                UUID uuid = player.getUUID();
                players.put(uuid, player);
                cap.setGame(game.gameType, player);
                game.onPlayerJoined(uuid);
                return uuid;
            }
        }
        return null;
    }

    public final int getPlayerCount() {
        return players.size();
    }
}
