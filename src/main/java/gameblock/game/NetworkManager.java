package gameblock.game;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockPackets;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class NetworkManager <T extends GameInstance<?>> {
    private final ArrayList<BiConsumer<UpdateGamePacket, UUID>> messages = new ArrayList<>();
    protected final T game;

    public NetworkManager(T game) {
        this.game = game;
    }

    protected final int registerMessage(BiConsumer<UpdateGamePacket, UUID> receiver) {
        int index = messages.size();
        messages.add(receiver);
        return index;
    }

    protected final void sendToClient(int type, Consumer<UpdateGamePacket> data, ServerPlayer player) {
        UpdateGamePacket packet = new UpdateGamePacket(type);
        data.accept(packet);
        GameblockPackets.sendToPlayer(player, packet);
    }

    protected final void sendToAll(int type, Consumer<UpdateGamePacket> data) {
        UpdateGamePacket packet = new UpdateGamePacket(type);
        data.accept(packet);
        game.players.forEachPlayerEntity((player) -> GameblockPackets.sendToPlayer((ServerPlayer) player, packet));
    }

    protected final void sendToServer(int type, Consumer<UpdateGamePacket> data) {
        UpdateGamePacket packet = new UpdateGamePacket(type);
        data.accept(packet);;
        GameblockPackets.sendToServer(packet);
    }

    public final void receive(UpdateGamePacket packet, UUID sender) {
        messages.get(packet.messageType).accept(packet, sender);
    }
}
