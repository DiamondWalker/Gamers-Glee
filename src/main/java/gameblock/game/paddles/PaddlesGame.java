package gameblock.game.paddles;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import gameblock.util.ColorF;
import gameblock.util.Direction1D;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

public class PaddlesGame extends GameInstance<PaddlesGame> {

    // COMMON DATA
    Paddle leftPaddle;
    Paddle rightPaddle;
    Vec2 ballPos;
    Vec2 ballOldPos;

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
        return "TESTTEST";
    }

    @Override
    protected void tick() {

    }

    @Override
    public void render(GuiGraphics graphics, float partialTicks) {
        if (getPlayerCount() == getMaxPlayers()) {
            drawRectangle(graphics, -80.0f, leftPaddle.pos, 5.0f, 15.0f, new ColorF(1.0f), 0);
            drawRectangle(graphics, 80.0f, rightPaddle.pos, 5.0f, 15.0f, new ColorF(1.0f), 0);

            drawRectangle(graphics, ballPos.x, ballPos.y, 1.0f, 1.0f, new ColorF(1.0f), 0);
        } else {
            drawText(graphics, 0.0f, 0.0f, 1.0f, new ColorF(1.0f), Component.literal("Waiting for players...")); // TODO: translate
        }
    }
}
