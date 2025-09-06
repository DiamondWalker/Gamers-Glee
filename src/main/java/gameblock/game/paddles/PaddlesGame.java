package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.ColorF;
import gameblock.util.Direction1D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public class PaddlesGame extends GameInstance<PaddlesGame> {

    // COMMON DATA
    boolean gameStarted = false;
    String gameCode = null;

    Paddle leftPaddle;
    Paddle rightPaddle;

    Vec2 ballPos;
    Vec2 ballOldPos;
    Vec2 ballMotion;

    // SERVER DATA
    final static Direction1D[] PLAYER_DIRECTIONS = {Direction1D.LEFT, Direction1D.RIGHT}; // maps player indexes to their paddle directions

    // CLIENT DATA
    Direction1D whichPaddleAmI;
    float otherPaddleUpdatePos = 0.0f;

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

    protected void initializeGame() {
        leftPaddle = new Paddle();
        rightPaddle = new Paddle();

        ballPos = Vec2.ZERO;
        ballOldPos = Vec2.ZERO;
        ballMotion = Vec2.ZERO;
    }

    @Override
    protected void onPlayerJoined(int index, ServerPlayer serverPlayer) {
        if (!gameStarted && getPlayerCount() == getMaxPlayers()) {
            gameStarted = true;
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
        }
    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        if (gameStarted) {
            drawRectangle(graphics, -80.0f, leftPaddle.oldPos + partialTicks * (leftPaddle.pos - leftPaddle.oldPos), 5.0f, 25.0f, new ColorF(1.0f), 0);
            drawRectangle(graphics, 80.0f, rightPaddle.oldPos + partialTicks * (rightPaddle.pos - rightPaddle.oldPos), 5.0f, 25.0f, new ColorF(1.0f), 0);

            drawRectangle(graphics, ballOldPos.x + partialTicks * (ballPos.x - ballOldPos.x), ballOldPos.y + partialTicks * (ballPos.y - ballOldPos.y), 3.0f, 3.0f, new ColorF(1.0f), 0);
        } else if (prompt == null) {
            drawText(graphics, 0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal("Waiting for players...")); // TODO: translate
            drawText(graphics, 0.0f, -10.0f, 0.5f, new ColorF(1.0f), Component.literal("(Remember: your game code is " + gameCode + ")"));
        }
    }
}
