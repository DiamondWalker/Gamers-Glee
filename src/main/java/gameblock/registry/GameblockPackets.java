package gameblock.registry;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class GameblockPackets {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("gameblock", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {

    }

    public static void sendToServer(Packet<? extends ServerGamePacketListener> packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static void sendToPlayer(ServerPlayer player, Packet<? extends ClientGamePacketListener> packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
