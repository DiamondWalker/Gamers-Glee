package gameblock.registry;

import gameblock.game.GameStatePacket;
import gameblock.game.blockbreak.BallLaunchPacket;
import gameblock.game.blockbreak.BallUpdatePacket;
import gameblock.game.blockbreak.BrickUpdatePacket;
import gameblock.game.blockbreak.ScoreUpdatePacket;
import gameblock.game.flyingchicken.PipeSpawnPacket;
import gameblock.game.flyingchicken.WingFlapPacket;
import gameblock.game.os.SelectGamePacket;
import gameblock.game.serpent.EatFoodPacket;
import gameblock.game.serpent.SnakeUpdatePacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameClosePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

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
        INSTANCE.registerMessage(id++, GameChangePacket.class, GameChangePacket::writeToBuffer, GameChangePacket::new, GameChangePacket::handle);
        INSTANCE.registerMessage(id++, GameStatePacket.class, GameStatePacket::writeToBuffer, GameStatePacket::new, GameStatePacket::handle);
        INSTANCE.registerMessage(id++, GameClosePacket.class, GameClosePacket::writeToBuffer, GameClosePacket::new, GameClosePacket::handle);

        // OS
        INSTANCE.registerMessage(id++, SelectGamePacket.class, SelectGamePacket::writeToBuffer, SelectGamePacket::new, SelectGamePacket::handle);

        // block break
        INSTANCE.registerMessage(id++, BallUpdatePacket.class, BallUpdatePacket::writeToBuffer, BallUpdatePacket::new, BallUpdatePacket::handle);
        INSTANCE.registerMessage(id++, BallLaunchPacket.class, BallLaunchPacket::writeToBuffer, BallLaunchPacket::new, BallLaunchPacket::handle);
        INSTANCE.registerMessage(id++, BrickUpdatePacket.class, BrickUpdatePacket::writeToBuffer, BrickUpdatePacket::new, BrickUpdatePacket::handle);
        INSTANCE.registerMessage(id++, ScoreUpdatePacket.class, ScoreUpdatePacket::writeToBuffer, ScoreUpdatePacket::new, ScoreUpdatePacket::handle);

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
