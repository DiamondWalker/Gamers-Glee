package gameblock.game;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public class PlayerManager {
    private final GameInstance<?> game;
    private final HashMap<UUID, Player> players = new HashMap<>();
    private Player host;
    private Player localPlayer;

    public PlayerManager(GameInstance<?> game) {
        this.game = game;
    }

    public void setHost(Player player) {
        if (player != null && !players.containsKey(player.getUUID())) throw new IllegalStateException("Host player must be in the game!");

        host = player;
    }

    /**
     * Getter for the host Player entity
     * <p>
     * On the server side, this is the player hosting the game (or, for singleplayer games, just the player)
     * On the client side, the host player will be null
     *
     * @return The Player entity hosting this game
     */
    public Player getHostPlayer() {
        return host;
    }

    public void setLocalPlayer(Player player) {
        localPlayer = player;
    }

    /**
     * Getter for the local Player entity
     * <p>
     * On the client side, this is the player belonging to this client
     * On the server side, the local player will be null
     *
     * @return The local player
     */
    public Player getLocalPlayer() {
        return localPlayer;
    }

    public Player getPlayerEntity(UUID id) {
        return players.get(id);
    }

    public Set<UUID> getPlayers() {
        return players.keySet();
    }

    public boolean canAddPlayers() {
        return getPlayerCount() < game.getMaxPlayers() && game.canJoin();
    }

    public void addPlayer(Player player) {
        if (getPlayerCount() >= game.getMaxPlayers()) throw new IllegalStateException("No room for adding more players");

        UUID uuid = player.getUUID();
        players.put(uuid, player);
        game.onPlayerJoined(uuid);
    }

    public void removePlayer(UUID id) {
        if (!players.containsKey(id)) throw new IllegalStateException("Player with id " + id.toString() + " is not in game");

        game.onPlayerLeft(id);
        Player player = players.remove(id);
        if (player == host) {
            setHost(null);
        }

        if (!game.isClientSide()) game.networkManager.getPendingUpdatesFor(id); // flush pending updates for this player
    }

    public int getPlayerCount() {
        return players.size();
    }
}
