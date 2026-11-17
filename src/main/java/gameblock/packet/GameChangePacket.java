package gameblock.packet;

import gameblock.game.Game;
import gameblock.registry.GameblockCustomRegistries;
import gameblock.registry.GameblockGames;
import io.netty.buffer.ByteBufUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.function.Supplier;

public class GameChangePacket {
    public Game<?> game;

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
    }
}
