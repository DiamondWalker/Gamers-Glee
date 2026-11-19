package gameblock.game;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
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

    private final GameUpdateType<T> gameRestart = registerMessage(this::receiveGameRestart);
    private final GameUpdateType<T> gameState = registerMessage(this::receiveGameState);

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

    protected final GameUpdateType<T> registerMessage(BiConsumer<GameUpdate.Incoming, UUID> receiver) {
        int index = messages.size();
        GameUpdateType<T> updateType = new GameUpdateType<>(index, receiver, this);
        messages.add(updateType);
        return updateType;
    }

    public final void queueUpdate(UUID player, GameUpdate.Outgoing update) {
        if (!game.isClientSide()) {
            if (player == null) {
                for (UUID aPlayer : game.players.getPlayers()) {
                    queueUpdate(aPlayer, update);
                }
            } else {
                pendingUpdates.put(player, update);
            }
        } else {
            if (player != null) throw new IllegalStateException("Updates sent to the server cannot target a player");
            pendingUpdates.put(null, update);
        }
    }

    public final void receive(GameUpdate.Incoming update, UUID sender) {
        messages.get(update.messageType).handler.accept(update, sender);
    }

    public final Collection<GameUpdate.Outgoing> getPendingUpdatesFor(UUID player) {
        return pendingUpdates.removeAll(player);
    }
}
