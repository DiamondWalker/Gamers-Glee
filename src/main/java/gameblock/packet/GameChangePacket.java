package gameblock.packet;

import gameblock.game.Game;
import gameblock.game.GameUpdate;
import gameblock.registry.GameblockCustomRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

public class GameChangePacket extends GameUpdatePacket {
    public Game<?> game;

    public GameChangePacket(Game<?> game, Collection<GameUpdate.Outgoing> updates) {
        super(updates);
        this.game = game;
    }

    public GameChangePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        ResourceLocation rl = GameblockCustomRegistries.GAMES.get().getKey(game);
        buffer.writeUtf(rl != null ? rl.toString() : "");
        super.writeToBuffer(buffer);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        String id = buffer.readUtf();
        game = !id.isEmpty() ? GameblockCustomRegistries.GAMES.get().getValue(ResourceLocation.parse(id)) : null;
        super.readFromBuffer(buffer);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleGameChangePacket(this));
        }
        context.get().setPacketHandled(true);
    }

    /*public Game<?> game;

    public GameChangePacket(Game<?> game) {
        this.game = game;
    }

    public GameChangePacket(FriendlyByteBuf buffer) {
        readFromBuffer(buffer);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        ResourceLocation rl = GameblockCustomRegistries.GAMES.get().getKey(game);
        buffer.writeUtf(rl != null ? rl.toString() : "");
    }

    public void readFromBuffer(FriendlyByteBuf buffer) {
        String id = buffer.readUtf();
        game = !id.isEmpty() ? GameblockCustomRegistries.GAMES.get().getValue(ResourceLocation.parse(id)) : null;
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        if (context.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
            context.get().enqueueWork(() -> ClientPacketHandler.handleGameChangePacket(this));
        }
        context.get().setPacketHandled(true);
    }*/
}
