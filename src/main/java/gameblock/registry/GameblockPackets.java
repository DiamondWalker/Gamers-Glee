package gameblock.registry;

import gameblock.game.GameOverPacket;
import gameblock.game.blockbreak.BallLaunchPacket;
import gameblock.game.blockbreak.BallUpdatePacket;
import gameblock.game.blockbreak.BrickUpdatePacket;
import gameblock.game.blockbreak.PlatformMovePacket;
import gameblock.game.flyingchicken.PipeSpawnPacket;
import gameblock.game.flyingchicken.WingFlapPacket;
import gameblock.game.serpent.EatFoodPacket;
import gameblock.game.serpent.SnakeUpdatePacket;
import gameblock.packet.EndGamePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import org.lwjgl.system.Platform;

public class GameblockPackets {
    private static int id = 0;

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("gameblock", "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void registerPackets() {
        INSTANCE.registerMessage(id++, EndGamePacket.class, EndGamePacket::writeToBuffer, EndGamePacket::new, EndGamePacket::handle);
        INSTANCE.registerMessage(id++, GameOverPacket.class, GameOverPacket::writeToBuffer, GameOverPacket::new, GameOverPacket::handle);

        // block break
        INSTANCE.registerMessage(id++, BallUpdatePacket.class, BallUpdatePacket::writeToBuffer, BallUpdatePacket::new, BallUpdatePacket::handle);
        INSTANCE.registerMessage(id++, BallLaunchPacket.class, BallLaunchPacket::writeToBuffer, BallLaunchPacket::new, BallLaunchPacket::handle);
        INSTANCE.registerMessage(id++, PlatformMovePacket.class, PlatformMovePacket::writeToBuffer, PlatformMovePacket::new, PlatformMovePacket::handle);
        INSTANCE.registerMessage(id++, BrickUpdatePacket.class, BrickUpdatePacket::writeToBuffer, BrickUpdatePacket::new, BrickUpdatePacket::handle);

        // serpent
        INSTANCE.registerMessage(id++, SnakeUpdatePacket.class, SnakeUpdatePacket::writeToBuffer, SnakeUpdatePacket::new, SnakeUpdatePacket::handle);
        INSTANCE.registerMessage(id++, EatFoodPacket.class, EatFoodPacket::writeToBuffer, EatFoodPacket::new, EatFoodPacket::handle);

        // flying chicken
        INSTANCE.registerMessage(id++, WingFlapPacket.class, WingFlapPacket::writeToBuffer, WingFlapPacket::new, WingFlapPacket::handle);
        INSTANCE.registerMessage(id++, PipeSpawnPacket.class, PipeSpawnPacket::writeToBuffer, PipeSpawnPacket::new, PipeSpawnPacket::handle);
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
