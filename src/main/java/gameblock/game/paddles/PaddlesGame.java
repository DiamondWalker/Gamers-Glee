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

    Paddle leftPaddle = new Paddle();
    Paddle rightPaddle = new Paddle();

    Vec2 ballPos = new Vec2(0.0f, 0.0f);
    Vec2 ballOldPos = new Vec2(0.0f, 0.0f);
    Vec2 ballMotion = new Vec2(0.0f, 0.0f);

    // SERVER DATA
    final static Direction1D[] PLAYER_DIRECTIONS = {Direction1D.LEFT, Direction1D.RIGHT}; // maps player indexes to their paddle directions

    // CLIENT DATA
    Direction1D whichPaddleAmI;

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

    public Paddle getMyPaddle() {
        if (!isClientSide()) throw new IllegalStateException("Attempted to call getMyPaddle on server side");
        return getPaddleFromDirection(whichPaddleAmI);
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    public String getGameCode() {
        if (getPlayerCount() == getMaxPlayers()) return null;
        return gameCode;
    }

    @Override
    protected void tick() {
        if (!gameStarted) {
            if (isClientSide()) {
                if (gameCode == null && prompt == null) prompt = new PaddleGameCodePrompt(this);
            } else if (getPlayerCount() == getMaxPlayers()) {
                gameStarted = true;
                forEachPlayer((Player player) -> {
                    ServerPlayer serverPlayer = (ServerPlayer) player;
                    GameblockPackets.sendToPlayer(serverPlayer, new GameStartPacket(getDirectionFromPlayer(serverPlayer)));
                });
            }
        } else {
            if (isClientSide()) {
                if (prompt != null) prompt.close();

                Paddle paddle = getMyPaddle(); // TODO: figure out how to update oldPos for the other paddle
                paddle.oldPos = paddle.pos;
                paddle.pos = getMouseCoordinates().y;
                if (paddle.pos != paddle.oldPos) GameblockPackets.sendToServer(new ClientToServerPaddleUpdatePacket(paddle.pos));
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
        }
    }
}
