package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.game.paddles.packets.ClientToServerPaddleUpdatePacket;
import gameblock.game.paddles.packets.PaddleGameStatePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.MathHelper;
import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class PaddlesGame extends GameInstance<PaddlesGame> {

    // COMMON DATA
    private boolean gameStarted = false;
    public String gameCode = null;

    public Paddle leftPaddle;
    public Paddle rightPaddle;

    public PaddlesBall ball;

    // SERVER DATA
    public static final Direction1D[] PLAYER_DIRECTIONS = {Direction1D.LEFT, Direction1D.RIGHT}; // maps player indexes to their paddle directions

    // CLIENT DATA
    public Direction1D whichPaddleAmI;
    public float otherPaddleUpdatePos = 0.0f;

    public PaddlesGame(Player player) {
        super(player, GameblockGames.PADDLES_GAME);
    }

    public Direction1D getDirectionFromPlayer(ServerPlayer player) {
        return PLAYER_DIRECTIONS[getPlayerIndex(player)];
    }

    public Paddle getPaddleFromPlayer(ServerPlayer player) {
        return getPaddleFromDirection(getDirectionFromPlayer(player));
    }

    public Paddle getPaddleFromDirection(Direction1D direction) {
        if (direction == Direction1D.LEFT) return leftPaddle;
        if (direction == Direction1D.RIGHT) return rightPaddle;
        throw new IllegalArgumentException("Attempted to get paddle with invalid direction " + direction);
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public boolean canJoin() {
        return super.canJoin() && !gameStarted;
    }

    @Override
    public String getGameCode() {
        return gameCode;
    }

    public void initializeGame() {
        gameStarted = true;
        leftPaddle = new Paddle(this, Direction1D.LEFT);
        rightPaddle = new Paddle(this, Direction1D.RIGHT);
        ball = new PaddlesBall(this);
        ball.motion = new Vec2(-1, 0).scale(ball.speed);
    }

    public void stopGame() {
        gameStarted = false;
        whichPaddleAmI = null;
    }

    @Override
    protected void onPlayerJoined(int index, ServerPlayer serverPlayer) {
        if (!gameStarted && getPlayerCount() == getMaxPlayers()) {
            initializeGame();
            forEachPlayer((Player p) -> {
                ServerPlayer sp = (ServerPlayer) p;
                GameblockPackets.sendToPlayer(sp, new PaddleGameStatePacket(getDirectionFromPlayer(sp)));
            });
        }
    }

    @Override
    protected void onPlayerDisconnected(int index, ServerPlayer player) {
        if (gameStarted) {
            gameStarted = false;
            forEachPlayer((Player p) -> {
                ServerPlayer sp = (ServerPlayer) p;
                GameblockPackets.sendToPlayer(sp, new PaddleGameStatePacket(Direction1D.CENTER)); // value of center means unassigned
            });
        }
    }

    @Override
    protected void tick() {
        if (!gameStarted) {
            if (isClientSide() && gameCode == null && prompt == null) prompt = new PaddleGameCodePrompt(this);
        } else {
            if (isClientSide() && prompt != null) prompt.close();

            leftPaddle.tick();
            rightPaddle.tick();

            ball.tick();
        }
    }

    @Override
    public void render() {
        if (gameStarted) {
            // draw the dividing line
            for (int i = -30; i <= 30; i++) {
                drawRectangle(0, i * 5, 1, 3, new ColorF(1.0f), 0);
            }
            float partialTicks = getPartialTicks();

            drawRectangle(-Paddle.POSITION, Mth.lerp(partialTicks, leftPaddle.oldPos, leftPaddle.pos), Paddle.DEPTH, Paddle.WIDTH, new ColorF(1.0f), 0);
            drawRectangle(Paddle.POSITION, Mth.lerp(partialTicks, rightPaddle.oldPos, rightPaddle.pos), Paddle.DEPTH, Paddle.WIDTH, new ColorF(1.0f), 0);

            drawRectangle(Mth.lerp(partialTicks, ball.oldPos.x, ball.pos.x), Mth.lerp(partialTicks, ball.oldPos.y, ball.pos.y), PaddlesBall.SIZE, PaddlesBall.SIZE, new ColorF(1.0f), 0);
        } else if (prompt == null) {
            drawText(0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal("Waiting for players...")); // TODO: translate
            drawText(0.0f, -10.0f, 0.5f, new ColorF(1.0f), Component.literal("(Remember: your game code is " + gameCode + ")"));
        }
    }
}
