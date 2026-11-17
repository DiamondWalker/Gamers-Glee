package gameblock.game.games.blockbreak;

import gameblock.game.NetworkManager;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public class BlockBreakNetworkManager extends NetworkManager<BlockBreakGame> {
    private final int ballLaunch = registerMessage(this::receiveBallLaunch);
    private final int ballUpdate = registerMessage(this::receiveBallUpdate);
    private final int blockUpdate = registerMessage(this::receiveBlockUpdate);
    private final int scoreUpdate = registerMessage(this::receiveScoreUpdate);

    public BlockBreakNetworkManager(BlockBreakGame game) {
        super(game);
    }

    public void sendBallLaunch(float xPos, float xMotion) {
        sendToServer(ballLaunch, (packet) -> packet.add(xPos).add(xMotion));
    }

    public void receiveBallLaunch(UpdateGamePacket packet, UUID sender) {
        game.lastPacketTime = game.getGameTime();
        game.ballX = packet.get(Float.class);
        game.launchBall(packet.get(Float.class));
    }

    public void sendBallUpdateToServer(float x, float y, float xMotion, float yMotion, boolean playSound) {
        sendToServer(ballUpdate, (packet) -> packet.add(x).add(y).add(xMotion).add(yMotion).add(playSound));
    }

    public void sendBallUpdateToAll(float x, float y, float xMotion, float yMotion, boolean playSound) {
        sendToAll(ballUpdate, (packet) -> packet.add(x).add(y).add(xMotion).add(yMotion).add(playSound));
    }

    public void receiveBallUpdate(UpdateGamePacket packet, UUID sender) {
        if (!game.isClientSide()) {
            game.lastPacketTime = game.getGameTime();
        } else {
            if (game.getGameTime() - game.clientToPacketBallUpdateTime < 4) return;
        }
        game.ballX = packet.get(Float.class);
        game.ballY = packet.get(Float.class);
        game.ballMoveX = packet.get(Float.class);
        game.ballMoveY = packet.get(Float.class);
        if (packet.get(Boolean.class) && game.isClientSide()) game.playSound(GameblockSounds.BALL_BOUNCE.get());
    }

    public void sendBlockUpdate(short brick) {
        sendToAll(blockUpdate, (packet) -> packet.add(brick));
    }

    public void receiveBlockUpdate(UpdateGamePacket packet, UUID sender) {
        short block = packet.get(Short.class);
        if (game.blocks.get(block) != null) {
            if (game.blocks.get(block).breaking == 0) {
                game.spawnBrickBreakParticles(game.blocks.get(block));
                //game.playSound(GameblockSounds.BRICK_BREAK.get());
            }
            game.blocks.set(block, null);
            game.blocksBroken++;
        }
    }

    public void sendScoreUpdate(int score, long seconds) {
        sendToAll(scoreUpdate, (packet) -> packet.add(score).add(seconds));
    }

    public void receiveScoreUpdate(UpdateGamePacket packet, UUID sender) {
        game.score = packet.get(Integer.class);
        game.timeSinceLaunch = packet.get(Integer.class) * 20;
    }
}
