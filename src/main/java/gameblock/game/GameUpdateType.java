package gameblock.game;

import gameblock.packet.GameUpdatePacket;

import java.util.UUID;
import java.util.function.BiConsumer;

public class GameUpdateType <T extends GameInstance<?>> {
    private final int id;
    public final BiConsumer<GameUpdate.Incoming, UUID> handler;
    private final NetworkManager<T> manager;

    GameUpdateType(int id, BiConsumer<GameUpdate.Incoming, UUID> handler, NetworkManager<T> manager) {
        this.id = id;
        this.handler = handler;
        this.manager = manager;
    }

    public GameUpdate.Outgoing sendToClient(UUID player) {
        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        manager.queueUpdate(player, update);
        return update;
    }

    public GameUpdate.Outgoing sendToAllClients() {
        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        manager.queueUpdate(null, update);
        return update;
    }

    public GameUpdate.Outgoing sendToServer() {
        GameUpdate.Outgoing update = new GameUpdate.Outgoing(id);
        manager.queueUpdate(null, update);
        return update;
    }
}
