package gameblock.registry;

import gameblock.game.GameRestartPacket;
import gameblock.game.GameStatePacket;
import gameblock.game.blockbreak.*;
import gameblock.game.defusal.*;
import gameblock.game.flyingchicken.FlyingChickenHighScorePacket;
import gameblock.game.flyingchicken.PipeSpawnPacket;
import gameblock.game.flyingchicken.ScorePacket;
import gameblock.game.flyingchicken.WingFlapPacket;
import gameblock.game.os.JoinGamePacket;
import gameblock.game.os.MultiplayerPromptPacket;
import gameblock.game.os.SelectGamePacket;
import gameblock.game.paddles.ClientToServerPaddleUpdatePacket;
import gameblock.game.paddles.GameStartPacket;
import gameblock.game.paddles.PaddleGameCodeSelectionPacket;
import gameblock.game.paddles.ServerToClientPaddleUpdatePacket;
import gameblock.game.serpent.EatFoodPacket;
import gameblock.game.serpent.SnakeUpdatePacket;
import gameblock.packet.GameChangePacket;
import gameblock.packet.GameClosePacket;
import gameblock.packet.IPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

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
        register(GameChangePacket.class, GameChangePacket::new);
        register(GameStatePacket.class, GameStatePacket::new);
        register(GameClosePacket.class, GameClosePacket::new);
        register(GameRestartPacket.class, GameRestartPacket::new);

        // OS
        register(SelectGamePacket.class, SelectGamePacket::new);
        register(MultiplayerPromptPacket.class, MultiplayerPromptPacket::new);
        register(JoinGamePacket.class, JoinGamePacket::new);

        // block break
        register(BallUpdatePacket.class, BallUpdatePacket::new);
        register(BallLaunchPacket.class, BallLaunchPacket::new);
        register(BlockUpdatePacket.class, BlockUpdatePacket::new);
        register(ScoreUpdatePacket.class, ScoreUpdatePacket::new);
        register(BlockBreakHighScorePacket.class, BlockBreakHighScorePacket::new);

        // serpent
        register(SnakeUpdatePacket.class, SnakeUpdatePacket::new);
        register(EatFoodPacket.class, EatFoodPacket::new);

        // flying chicken
        register(WingFlapPacket.class, WingFlapPacket::new);
        register(PipeSpawnPacket.class, PipeSpawnPacket::new);
        register(ScorePacket.class, ScorePacket::new);
        register(FlyingChickenHighScorePacket.class, FlyingChickenHighScorePacket::new);

        // defusal
        register(TileClickPacket.class, TileClickPacket::new);
        register(TileRevealPacket.class, TileRevealPacket::new);
        register(BombRevealPacket.class, BombRevealPacket::new);
        register(TileStatePacket.class, TileStatePacket::new);
        register(BombCountPacket.class, BombCountPacket::new);
        register(TimePacket.class, TimePacket::new);

        // paddles
        register(GameStartPacket.class, GameStartPacket::new);
        register(ClientToServerPaddleUpdatePacket.class, ClientToServerPaddleUpdatePacket::new);
        register(ServerToClientPaddleUpdatePacket.class, ServerToClientPaddleUpdatePacket::new);
        register(PaddleGameCodeSelectionPacket.class, PaddleGameCodeSelectionPacket::new);
    }

    private static <MSG extends IPacket> void register(Class<MSG> clazz, Function<FriendlyByteBuf, MSG> constructor) {
        INSTANCE.registerMessage(
                id++,
                clazz,
                MSG::writeToBuffer,
                constructor,
                MSG::handle
        );
    }

    public static <MSG> void sendToServer(MSG packet) {
        INSTANCE.send(PacketDistributor.SERVER.noArg(), packet);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
