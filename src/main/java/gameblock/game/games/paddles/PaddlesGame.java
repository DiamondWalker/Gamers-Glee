package gameblock.game.games.paddles;

import gameblock.GameblockMod;
import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.ColorF;
import gameblock.util.Direction1D;
import gameblock.util.GameState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

import java.util.*;

public class PaddlesGame extends GameInstance<PaddlesNetworkManager> {
    public static ResourceLocation SPRITE = ResourceLocation.fromNamespaceAndPath(GameblockMod.MODID, "textures/gui/game/paddles.png");

    private boolean gameStarted = false;
    public String gameCode = null;

    public Paddle leftPaddle;
    public Paddle rightPaddle;
    public PaddlesBall ball;

    public int leftScore;
    public int rightScore;
    Direction1D winSide = Direction1D.CENTER;

    int scoreTimer;
    private boolean gameEnd = false;

    public PaddlesGame(boolean clientSide) {
        super(clientSide, GameblockGames.PADDLES.get());
    }

    @Override
    public void initialize() {
        if (isClientSide()) prompt = new PaddlesGameCodePrompt(this);
    }

    public Paddle getPaddleFromDirection(Direction1D direction) {
        if (direction == Direction1D.LEFT) return leftPaddle;
        if (direction == Direction1D.RIGHT) return rightPaddle;
        throw new IllegalArgumentException("Attempted to get paddle with invalid direction " + direction);
    }

    @Override
    protected PaddlesNetworkManager getNetworkManager() {
        return new PaddlesNetworkManager(this);
    }

    @Override
    protected int getMaxPlayers() {
        return 2;
    }

    @Override
    protected boolean canJoin() {
        return !gameStarted;
    }

    @Override
    public String getGameCode() {
        return gameCode;
    }

    @Override
    protected void onPlayerJoined(UUID player) {
        if (!gameStarted && players.getPlayerCount() == getMaxPlayers() && !isClientSide()) {
            List<UUID> playerList = new ArrayList<>(players.getPlayers());

            if (new Random().nextBoolean()) {
                UUID swap = playerList.get(0);
                playerList.set(0, playerList.get(1));
                playerList.set(1, swap);
            }

            startGame(
                    new Paddle(
                            this,
                            Direction1D.LEFT,
                            playerList.get(0)
                    ),
                    new Paddle(
                            this,
                            Direction1D.RIGHT,
                            playerList.get(1)
                    )
            );

            networkManager.sendPaddleInitialization(playerList.get(0), playerList.get(1));

            setGameState(GameState.ACTIVE);
        }
    }

    @Override
    protected void onPlayerLeft(UUID player) {
        if (gameStarted) {
            gameStarted = false;
        }
    }

    void startGame(Paddle left, Paddle right) {
        gameStarted = true;

        prompt = null;

        leftPaddle = left;
        rightPaddle = right;

        ball = new PaddlesBall(this);
        ball.motion = new Vec2(-1, 0);
        leftScore = rightScore = 0;
    }

    public void score(Direction1D dir) {
        if (scoreTimer <= 0) {
            if (dir == Direction1D.LEFT) {
                leftScore++;
            } else if (dir == Direction1D.RIGHT) {
                rightScore++;
            } else {
                throw new IllegalArgumentException("Invalid score direction!");
            }

            gameEnd = leftScore >= 11 || rightScore >= 11;
            // tell the clients that the round has ended and inform them of the winner. This lets them know to start flashing one of the numbers
            networkManager.sendRoundEnd(dir, gameEnd);

            // schedule the start of the new round.
            scoreTimer = 40;
        }
    }

    @Override
    protected void tick() {
        if (gameStarted) {
            leftPaddle.tick();
            rightPaddle.tick();

            if (scoreTimer <= 0) ball.tick();
        }

        if (scoreTimer > 0) {
            scoreTimer--;

            if (scoreTimer == 0) {
                ball.resetBall();
                networkManager.sendScoreUpdate(leftScore, rightScore);
                if (gameEnd) {
                    setGameState(GameState.LOSS);
                }
            }
        }
    }

    @Override
    public void render(float partialTicks) {
        if (gameStarted) {
            // draw the dividing line
            for (int i = -30; i <= 30; i++) {
                renderer.drawRectangle(0, i * 5, 1, 3, ColorF.WHITE, 0);
            }

            // draw the score counters
            if ((scoreTimer / 3) % 2 == 0 || winSide != Direction1D.LEFT) {
                renderer.drawTexture(SPRITE, -30, 50, 18, 27, 0, 250, 9 * leftScore, 6, 9);
            }
            if ((scoreTimer / 3) % 2 == 0 || winSide != Direction1D.RIGHT) {
                renderer.drawTexture(SPRITE, 30, 50, 18, 27, 0, 250, 9 * rightScore, 6, 9);
            }

            if (!isGameOver()) {
                renderer.drawRectangle(-Paddle.POSITION, Mth.lerp(partialTicks, leftPaddle.oldPos, leftPaddle.pos), Paddle.DEPTH, Paddle.WIDTH, ColorF.WHITE, 0);
                renderer.drawRectangle(Paddle.POSITION, Mth.lerp(partialTicks, rightPaddle.oldPos, rightPaddle.pos), Paddle.DEPTH, Paddle.WIDTH, ColorF.WHITE, 0);
            }

            if (scoreTimer <= 0) renderer.drawRectangle(Mth.lerp(partialTicks, ball.oldPos.x, ball.pos.x), Mth.lerp(partialTicks, ball.oldPos.y, ball.pos.y), PaddlesBall.SIZE, PaddlesBall.SIZE, ColorF.WHITE, 0);
        } else if (prompt == null) {
            renderer.drawText(0.0f, 0.0f, 1.0f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.waiting_for_players"));
            renderer.drawText(0.0f, -10.0f, 0.5f, ColorF.WHITE, Component.translatable("gui.gameblock.paddles.game_code_reminder", gameCode));
        }
    }
}
