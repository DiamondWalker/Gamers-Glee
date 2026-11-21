package gameblock.game.games.flyingchicken;

import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import gameblock.registry.GameblockSounds;

import java.util.UUID;

public class FlyingChickenNetworkManager extends NetworkManager<FlyingChickenGame> {
    private final GameUpdateType<FlyingChickenGame> highScoreUpdate = registerMessage(this::receiveHighScore, false, true);
    private final GameUpdateType<FlyingChickenGame> pipeSpawn = registerMessage(this::receivePipeSpawn, false, true);
    private final GameUpdateType<FlyingChickenGame> scoreUpdate = registerMessage(this::receiveScoreUpdate, false, true);
    private final GameUpdateType<FlyingChickenGame> wingFlap = registerMessage(this::receiveWingFlap, true, false);

    public FlyingChickenNetworkManager(FlyingChickenGame game) {
        super(game);
    }

    public void sendHighScore(int highScore) {
        highScoreUpdate.sendToAllClients().add(highScore);
    }

    public void receiveHighScore(GameUpdate.Incoming update, UUID sender) {
        game.highScore = update.get(Integer.class);
    }

    public void sendPipeSpawn(float x, float y) {
        pipeSpawn.sendToAllClients().add(x).add(y);
    }

    public void receivePipeSpawn(GameUpdate.Incoming update, UUID sender) {
        float x = update.get(Float.class);
        float y = update.get(Float.class);

        game.pipes.enqueue(new FlyingChickenGame.Pipe(x, y));
    }

    public void sendScoreUpdate(int score) {
        scoreUpdate.sendToAllClients().add(score);
    }

    public void receiveScoreUpdate(GameUpdate.Incoming update, UUID sender) {
        int score = update.get(Integer.class);

        game.score = score;
        game.lastScoreTime = game.getGameTime();
        game.playSound(GameblockSounds.FLYING_CHICKEN_SCORE.get());
    }

    public void sendWingFlap(long time, float y) {
        wingFlap.sendToServer().add(time).add(y);
    }

    public void receiveWingFlap(GameUpdate.Incoming update, UUID sender) {
        long time = update.get(Long.class);
        float y = update.get(Float.class);

        game.chickenY = y;
        game.time = time;
        game.flap();
    }
}
