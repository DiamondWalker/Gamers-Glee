package gameblock.game;

import gameblock.packet.GameUpdatePacket;
import net.minecraftforge.event.TickEvent;

import java.util.UUID;
import java.util.function.BiConsumer;

public class GameUpdateType <T extends GameInstance<?>> {
    private final int id;
    public final BiConsumer<GameUpdate.Incoming, UUID> handler;
    private final NetworkManager<T> manager;

    private final boolean canBeSentFromClient;
    private final boolean canBeSentFromServer;

    GameUpdateType(int id, BiConsumer<GameUpdate.Incoming, UUID> handler, NetworkManager<T> manager, boolean canBeSentFromClient, boolean canBeSentFromServer) {
        this.id = id;
        this.handler = handler;
        this.manager = manager;
        this.canBeSentFromClient = canBeSentFromClient;
        this.canBeSentFromServer = canBeSentFromServer;
    }

    /**
     * Send an update of this type to a specific client
     * @param player The client to send the update to
     * @return An outgoing game update. Use {@link GameUpdate.Outgoing#add(Object)} to attach data to the update
     * @throws IllegalStateException if this update type cannot be sent from this side
     */
    public GameUpdate.Outgoing sendToClient(UUID player) {
        if (manager.game.isClientSide()) throw new IllegalStateException("sendToClient cannot be called on client side");
        if (!canBeSentFromServer) throw new IllegalStateException("Game update cannot be sent from server side");

        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        manager.queueUpdate(update, player);
        return update;
    }

    /**
     * Send an update of this type to all clients
     * @return An outgoing game update. Use {@link GameUpdate.Outgoing#add(Object)} to attack data to the update
     * @throws IllegalStateException if this update type cannot be sent from this side
     */
    public GameUpdate.Outgoing sendToAllClients() {
        if (manager.game.isClientSide()) throw new IllegalStateException("sendToAllClients cannot be called on client side");
        if (!canBeSentFromServer) throw new IllegalStateException("Game update cannot be sent from server side");

        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        for (UUID player : manager.game.players.getPlayers()) manager.queueUpdate(update, player);
        return update;
    }

    /**
     * Send an update of this type to the server
     * @return An outgoing game update. Use {@link GameUpdate.Outgoing#add(Object)} to attack data to the update
     * @throws IllegalStateException if this update type cannot be sent from this side
     */
    public GameUpdate.Outgoing sendToServer() {
        if (!manager.game.isClientSide()) throw new IllegalStateException("sendToServer cannot be called on server side");
        if (!canBeSentFromClient) throw new IllegalStateException("Game update cannot be sent from client side");

        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        manager.queueUpdate(update, null);
        return update;
    }

    /**
     * Whether this update type should be expected on the server side
     * @return {@code true} if updates of this type should be accepted on the server side; {@code false} if they should be ignored
     */
    public boolean acceptOnServer() {
        return canBeSentFromClient;
    }

    /**
     * Whether this update type should be expected on the client side
     * @return {@code true} if updates of this type should be accepted on the client side; {@code false} if they should be ignored
     */
    public boolean acceptOnClient() {
        return canBeSentFromServer;
    }
}
