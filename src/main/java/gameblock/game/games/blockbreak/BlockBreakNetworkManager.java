package gameblock.game.games.blockbreak;

import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockSounds;

import java.util.UUID;

public class BlockBreakNetworkManager extends NetworkManager<BlockBreakGame> {
    private final GameUpdateType<BlockBreakGame> ballLaunch = registerMessage(this::receiveBallLaunch, true, false);
    private final GameUpdateType<BlockBreakGame> ballUpdate = registerMessage(this::receiveBallUpdate, true, true);
    private final GameUpdateType<BlockBreakGame> blockUpdate = registerMessage(this::receiveBlockUpdate, false, true);
    private final GameUpdateType<BlockBreakGame> scoreUpdate = registerMessage(this::receiveScoreUpdate, false, true);
    private final GameUpdateType<BlockBreakGame> highScoreUpdate = registerMessage(this::receiveHighScore, false, true);

    public BlockBreakNetworkManager(BlockBreakGame game) {
        super(game);
    }

    public void sendBallLaunch(float xPos, float xMotion) {
        ballLaunch.sendToServer().add(xPos).add(xMotion);
    }

    public void receiveBallLaunch(GameUpdate.Incoming packet, UUID sender) {
        game.lastPacketTime = game.getGameTime();
        game.ballX = packet.get(Float.class);
        game.launchBall(packet.get(Float.class));
    }

    public void sendBallUpdateToServer(float x, float y, float xMotion, float yMotion, boolean playSound) {
        ballUpdate.sendToServer().add(x).add(y).add(xMotion).add(yMotion).add(playSound);
    }

    public void sendBallUpdateToAll(float x, float y, float xMotion, float yMotion, boolean playSound) {
        ballUpdate.sendToAllClients().add(x).add(y).add(xMotion).add(yMotion).add(playSound);
    }

    public void receiveBallUpdate(GameUpdate.Incoming packet, UUID sender) {
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
        blockUpdate.sendToAllClients().add(brick);
    }

    public void receiveBlockUpdate(GameUpdate.Incoming packet, UUID sender) {
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
        scoreUpdate.sendToAllClients().add(score).add(seconds);
    }

    public void receiveScoreUpdate(GameUpdate.Incoming packet, UUID sender) {
        game.score = packet.get(Integer.class);
        game.timeSinceLaunch = packet.get(Long.class) * 20;
    }

    public void sendHighScore(int highScore) {
        highScoreUpdate.sendToAllClients().add(highScore);
    }

    public void receiveHighScore(GameUpdate.Incoming packet, UUID sender) {
        game.highScore = packet.get(Integer.class);
    }
}
