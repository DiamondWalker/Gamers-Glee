package gameblock.game;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import gameblock.GameblockMod;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockPackets;
import gameblock.util.GameState;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.function.BiConsumer;

public abstract class NetworkManager <T extends GameInstance<?>> {
    private final ArrayList<GameUpdateType<?>> messages = new ArrayList<>();
    protected final T game;

    private final Multimap<UUID, GameUpdate.Outgoing> pendingUpdates = LinkedHashMultimap.create();

    private final GameUpdateType<T> gameRestart = registerMessage(this::receiveGameRestart, true, false);
    private final GameUpdateType<T> gameState = registerMessage(this::receiveGameState, false, true);

    public NetworkManager(T game) {
        this.game = game;
    }

    public final void sendGameRestart() {
        gameRestart.sendToServer();
    }

    public final void receiveGameRestart(GameUpdate.Incoming packet, UUID sender) {
        game.restart();
    }

    public final void sendGameState(GameState state) {
        gameState.sendToAllClients().add(state.ordinal());
    }

    public final void receiveGameState(GameUpdate.Incoming packet, UUID sender) {
        game.setGameState(GameState.values()[packet.get(Integer.class)]);
    }

    /**
     * Registers a network update type
     *
     * @param receiver The code that will read and handle this type of update when received
     * @param sentFromClient Whether this update can be sent from the client side
     * @param sentFromServer Whether this update can be sent from the server side
     *
     * @return A {@link GameUpdateType} instance that can be used to send updates of this type
     */
    protected final GameUpdateType<T> registerMessage(BiConsumer<GameUpdate.Incoming, UUID> receiver, boolean sentFromClient, boolean sentFromServer) {
        int index = messages.size();
        GameUpdateType<T> updateType = new GameUpdateType<>(index, receiver, this, sentFromClient, sentFromServer);
        messages.add(updateType);
        return updateType;
    }

    /**
     * Queues an outgoing update to be sent
     *
     * @param update The update
     * @param player The player to send the update to. Should be null for client-to-server updates
     * @throws IllegalStateException If {@code player} is nonnull on the client side
     */
    final void queueUpdate(GameUpdate.Outgoing update, UUID player) {
        if (game.isClientSide() && player != null) throw new IllegalStateException("Updates sent to the server cannot target a player");

        pendingUpdates.put(player, update);
    }

    /**
     * Receives an incoming update and dispatches it to the appropriate handler
     *
     * @param update The update
     * @param sender The sender. Null for server-to-client updates
     */
    public final void receive(GameUpdate.Incoming update, UUID sender) {
        GameUpdateType<?> updateType = messages.get(update.messageType);
        if (game.isClientSide() ? updateType.acceptOnClient() : updateType.acceptOnServer()) {
            updateType.handler.accept(update, sender);
        } else {
            GameblockMod.LOGGER.warn("Ignoring game update with ID {} as it is not accepted on the {} side", update.messageType, game.isClientSide() ? "client" : "server");
        }
    }

    /**
     * Retrieves and clears the list of updates that need to be sent to this player
     *
     * @param player The player to retrieve updates for. Must be null on the client side
     * @return The collection of updates to be sent
     * @throws IllegalStateException If {@code player} is nonnull on the client side
     */
    public final Collection<GameUpdate.Outgoing> getPendingUpdatesFor(UUID player) {
        if (game.isClientSide() && player != null) throw new IllegalStateException("Cannot retrieve pending updates for a non-null player");
        return pendingUpdates.removeAll(player);
    }
}
