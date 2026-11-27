package gameblock.game.games.paddles;

import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import gameblock.registry.GameblockSounds;
import gameblock.util.CompletionStatus;
import gameblock.util.Direction1D;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class PaddlesNetworkManager extends NetworkManager<PaddlesGame> {
    private final GameUpdateType<PaddlesGame> gameStart = registerMessage(this::receiveGameStart, false, true);
    private final GameUpdateType<PaddlesGame> gameStop = registerMessage(this::receiveGameStop, false, true);
    private final GameUpdateType<PaddlesGame> roundEnd = registerMessage(this::receiveRoundEnd, false, true);
    private final GameUpdateType<PaddlesGame> scoreUpdate = registerMessage(this::receiveScoreUpdate, false, true);
    private final GameUpdateType<PaddlesGame> clientToServerPaddleUpdate = registerMessage(this::receiveClientToServerPaddleUpdate, true, false);
    private final GameUpdateType<PaddlesGame> serverToClientPaddleUpdate = registerMessage(this::receiveServerToClientPaddleUpdate, false, true);
    private final GameUpdateType<PaddlesGame> ballUpdate = registerMessage(this::receiveBallUpdate, true, true);
    private final GameUpdateType<PaddlesGame> gameCodeSelection = registerMessage(this::receiveGameCodeSelection, true, false);
    private final GameUpdateType<PaddlesGame> gameCodeConfirmation = registerMessage(this::receiveGameCodeConfirmation, false, true);

    public PaddlesNetworkManager(PaddlesGame game) {
        super(game);
    }

    void sendGameStart(UUID leftPlayer, UUID rightPlayer) {
        gameStart.sendToAllClients().add(leftPlayer).add(rightPlayer);
    }

    private void receiveGameStart(GameUpdate.Incoming update, UUID sender) {
        UUID leftPlayer = update.get(UUID.class);
        UUID rightPlayer = update.get(UUID.class);

        game.startGame(leftPlayer, rightPlayer);
    }

    void sendGameStop() {
        gameStop.sendToAllClients();
    }

    private void receiveGameStop(GameUpdate.Incoming update, UUID sender) {
        game.stopGame();
    }

    void sendRoundEnd(Direction1D winningSide, boolean finalRound) {
        roundEnd.sendToAllClients().add(winningSide.ordinal()).add(finalRound);
    }

    private void receiveRoundEnd(GameUpdate.Incoming update, UUID sender) {
        Direction1D winningSide = Direction1D.values()[update.get(Integer.class)];
        boolean finalRound = update.get(Boolean.class);

        game.scoreTimer = 40;
        game.winSide = winningSide;
        RegistryObject<SoundEvent> sound = finalRound ? GameblockSounds.PADDLES_WIN : GameblockSounds.PADDLES_SCORE;
        game.playSound(sound.get(), 1.0f, 0.5f);
    }

    void sendScoreUpdate(int leftScore, int rightScore) {
        scoreUpdate.sendToAllClients().add((byte) leftScore).add((byte) rightScore);
    }

    private void receiveScoreUpdate(GameUpdate.Incoming update, UUID sender) {
        byte leftScore = update.get(Byte.class);
        byte rightScore = update.get(Byte.class);

        game.leftScore = leftScore;
        game.rightScore = rightScore;
        game.ball.resetBall();
        game.scoreTimer = 0;
        game.winSide = Direction1D.CENTER;
    }

    void sendClientToServerPaddleUpdate(float pos) {
        clientToServerPaddleUpdate.sendToServer().add(pos);
    }

    private void receiveClientToServerPaddleUpdate(GameUpdate.Incoming update, UUID sender) {
        float pos = update.get(Float.class);

        Paddle paddle;
        if (game.leftPaddle.owner.equals(sender)) {
            paddle = game.leftPaddle;
        } else if (game.rightPaddle.owner.equals(sender)) {
            paddle = game.rightPaddle;
        } else {
            throw new IllegalStateException("Cannot find this player's paddle!");
        }
        paddle.pos = pos;
        sendServerToClientPaddleUpdate(pos, paddle.direction);
    }

    void sendServerToClientPaddleUpdate(float pos, Direction1D dir) {
        serverToClientPaddleUpdate.sendToAllClients().add(pos).add(dir.ordinal());
    }

    private void receiveServerToClientPaddleUpdate(GameUpdate.Incoming update, UUID sender) {
        float pos = update.get(Float.class);
        Direction1D dir = Direction1D.values()[update.get(Integer.class)];

        Paddle paddle;
        if (dir == Direction1D.LEFT) {
            paddle = game.leftPaddle;
        } else if (dir == Direction1D.RIGHT) {
            paddle = game.rightPaddle;
        } else {
            throw new IllegalStateException("Invalid direction for server to client paddle update!");
        }

        paddle.pos = pos;
    }

    void sendBallUpdateToServer(Vec2 pos, Vec2 motion, float speed) {
        ballUpdate.sendToServer()
                .add(pos.x).add(pos.y)
                .add(motion.x).add(motion.y)
                .add(speed);
    }

    void sendBallUpdateToAllClients(Vec2 pos, Vec2 motion, float speed) {
        ballUpdate.sendToAllClients()
                .add(pos.x).add(pos.y)
                .add(motion.x).add(motion.y)
                .add(speed);
    }

    private void receiveBallUpdate(GameUpdate.Incoming update, UUID sender) {
        Vec2 pos = new Vec2(update.get(Float.class), update.get(Float.class));
        Vec2 motion = new Vec2(update.get(Float.class), update.get(Float.class));
        float speed = update.get(Float.class);

        game.ball.pos = pos;
        game.ball.motion = motion;
        game.ball.speed = speed;

        if (game.isClientSide()) {
            game.playSound(GameblockSounds.PADDLES_BOUNCE.get());
        } else {
            sendBallUpdateToAllClients(pos, motion, speed);
        }
    }

    void sendGameCodeSelection(String code) {
        gameCodeSelection.sendToServer().add(code);
    }

    private void receiveGameCodeSelection(GameUpdate.Incoming update, UUID sender) {
        String gameCode = update.get(String.class);

        CompletionStatus result;
        if (game.findGameWithGameCode(gameCode) == null) {
            game.gameCode = gameCode;
            result = CompletionStatus.SUCCESS;
        } else {
            result = CompletionStatus.FAIL;
        }

        sendGameCodeConfirmation(result, sender);
    }

    void sendGameCodeConfirmation(CompletionStatus status, UUID target) {
        gameCodeConfirmation.sendToClient(target).add(status.ordinal());
    }

    private void receiveGameCodeConfirmation(GameUpdate.Incoming update, UUID sender) {
        CompletionStatus result = CompletionStatus.values()[update.get(Integer.class)];

        if (result == CompletionStatus.SUCCESS) {
            if (game.prompt instanceof PaddlesGameCodePrompt) game.prompt.close();
        } else {
            game.gameCode = null;
            PaddlesGameCodePrompt prompt;
            if (game.prompt instanceof PaddlesGameCodePrompt codePrompt) {
                prompt = codePrompt;
            } else {
                prompt = new PaddlesGameCodePrompt(game);
                game.prompt = prompt;
            }

            prompt.setFailed();
        }
    }
}
