package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.game.paddles.packets.ClientToServerPaddleUpdatePacket;
import gameblock.game.paddles.packets.PaddleGameStatePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.rendering.ColorF;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

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
        leftPaddle = new Paddle();
        rightPaddle = new Paddle();
        ball = new PaddlesBall();
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
            if (isClientSide()) {
                if (prompt != null) prompt.close();

                Paddle myPaddle = getPaddleFromDirection(whichPaddleAmI);
                Paddle opponentPaddle = getPaddleFromDirection(whichPaddleAmI.getOpposite());

                myPaddle.oldPos = myPaddle.pos;
                myPaddle.pos = getMouseCoordinates().y;

                opponentPaddle.oldPos = opponentPaddle.pos;
                opponentPaddle.pos = otherPaddleUpdatePos;

                if (myPaddle.pos != myPaddle.oldPos) GameblockPackets.sendToServer(new ClientToServerPaddleUpdatePacket(myPaddle.pos));
            }

            ball.oldPos = ball.pos;
            ball.pos = ball.pos.add(ball.motion);

            if (Math.abs(ball.pos.y + PaddlesBall.SIZE / 2) >= 75) {
                if (Math.round(Math.signum(ball.pos.y)) == Math.round(Math.signum(ball.motion.y))) { // make sure it's still moving out of the screen
                    ball.motion = new Vec2(ball.motion.x, ball.motion.y * -1);
                }
            }
            if (Math.abs(Math.abs(ball.pos.x) - Paddle.POSITION) <= (Paddle.DEPTH + PaddlesBall.SIZE) / 2) { // is ball at the correct x range to hit a paddle?
                if (Math.round(Math.signum(ball.pos.x)) == Math.round(Math.signum(ball.motion.x))) { // make sure ball is moving towards the paddles and not bouncing off
                    Direction1D side = ball.pos.x > 0 ? Direction1D.RIGHT : Direction1D.LEFT;
                    Paddle hitPaddle = getPaddleFromDirection(side);

                    float yComponent = (ball.pos.y - hitPaddle.pos) / ((PaddlesBall.SIZE + Paddle.WIDTH) / 2); // if the ball hits the very corner, this will be 1 or -1. If the ball hits the center, it'll be 0
                    if (Math.abs(yComponent) <= 1.0f) { // ball is at the correct y range (hit paddle)
                        ball.speed *= 1.2f;
                        ball.motion = new Vec2(side.getOpposite().getComponent(), yComponent).scale(ball.speed);
                    }
                }
            }
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

            drawRectangle(-Paddle.POSITION, leftPaddle.oldPos + partialTicks * (leftPaddle.pos - leftPaddle.oldPos), Paddle.DEPTH, Paddle.WIDTH, new ColorF(1.0f), 0);
            drawRectangle(Paddle.POSITION, rightPaddle.oldPos + partialTicks * (rightPaddle.pos - rightPaddle.oldPos), Paddle.DEPTH, Paddle.WIDTH, new ColorF(1.0f), 0);

            drawRectangle(ball.oldPos.x + partialTicks * (ball.pos.x - ball.oldPos.x), ball.oldPos.y + partialTicks * (ball.pos.y - ball.oldPos.y), PaddlesBall.SIZE, PaddlesBall.SIZE, new ColorF(1.0f), 0);
        } else if (prompt == null) {
            drawText(0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal("Waiting for players...")); // TODO: translate
            drawText(0.0f, -10.0f, 0.5f, new ColorF(1.0f), Component.literal("(Remember: your game code is " + gameCode + ")"));
        }
    }
}
