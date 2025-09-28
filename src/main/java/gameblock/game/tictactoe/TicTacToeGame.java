package gameblock.game.tictactoe;

import gameblock.game.GameInstance;
import gameblock.game.GamePlayer;
import gameblock.game.tictactoe.packets.TicTacToeCanMakeMovePacket;
import gameblock.game.tictactoe.packets.TicTacToeStartGamePacket;
import gameblock.game.tictactoe.packets.TicTacToeStopGamePacket;
import gameblock.game.tictactoe.packets.TicTacToeClientToServerDrawShapePacket;
import gameblock.registry.GameblockGames;
import gameblock.registry.GameblockPackets;
import gameblock.util.TickTimer;
import gameblock.util.datastructure.TileGrid2D;
import gameblock.util.physics.Direction1D;
import gameblock.util.physics.Vec2i;
import gameblock.util.rendering.ColorF;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

import java.util.Random;

public class TicTacToeGame extends GameInstance<TicTacToeGame, TicTacToePlayerData> {
    // COMMON DATA
    public String gameCode = null;
    private TicTacToeShapeType currentlyMakingMove = null; // if this is null, game isn't started
    public TileGrid2D<TicTacToeShape> shapes = new TileGrid2D<>(-1, 1, -1, 1);

    // CLIENT DATA
    public TicTacToeShapeType myType = null;
    public boolean canMakeMove = false;

    public TicTacToeGame(Player player) {
        super(player, GameblockGames.TIC_TAC_TOE_GAME, TicTacToePlayerData::new);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        super.writeToBuffer(buffer);
        buffer.writeBoolean(gameCode == null); // whether the prompt should be opened. The game code hasn't been selected so this must be the host player
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        super.readFromBuffer(buffer);
        if (buffer.readBoolean()) prompt = new TicTacToeGameCodePrompt(this);
    }

    @Override
    public int getMaxPlayers() {
        return 2;
    }

    @Override
    protected void onPlayerJoined(GamePlayer<TicTacToePlayerData> player) {
        super.onPlayerJoined(player);
        if (getPlayerCount() == getMaxPlayers()) {
            startGame();
        }
    }

    @Override
    protected void onPlayerDisconnected(GamePlayer<TicTacToePlayerData> player) {
        super.onPlayerDisconnected(player);
        stopGame();
    }

    private void startGame() {
        if (new Random().nextBoolean()) {
            getPlayer(0).data().shape = TicTacToeShapeType.X;
            getPlayer(1).data().shape = TicTacToeShapeType.O;
        } else {
            getPlayer(0).data().shape = TicTacToeShapeType.O;
            getPlayer(1).data().shape = TicTacToeShapeType.X;
        }

        forEachPlayer((GamePlayer<TicTacToePlayerData> player) -> {
            GameblockPackets.sendToPlayer((ServerPlayer) player.playerEntity(), new TicTacToeStartGamePacket(player.data().shape));
        });

        setNextShape(TicTacToeShapeType.X);
    }

    private void stopGame() {
        currentlyMakingMove = null;
        forEachPlayer((GamePlayer<TicTacToePlayerData> player) -> {
            GameblockPackets.sendToPlayer((ServerPlayer) player.playerEntity(), new TicTacToeStopGamePacket());
        });
    }

    public void setNextShape(TicTacToeShapeType shape) {
        currentlyMakingMove = shape;
        forEachPlayer((GamePlayer<TicTacToePlayerData> player) -> {
            GameblockPackets.sendToPlayer((ServerPlayer) player.playerEntity(), new TicTacToeCanMakeMovePacket(player.data().shape == currentlyMakingMove));
        });
    }

    public TicTacToeShapeType getNextShape() {
        return currentlyMakingMove;
    }

    @Override
    protected void tick() {

    }

    @Override
    public String getGameCode() {
        return gameCode;
    }

    public boolean isShapeCurrentlyBeingDrawn() {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                if (shapes.get(x, y) != null && shapes.get(x, y).timer.getState() == TickTimer.TimerState.RUNNING) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void click(Vec2 clickCoordinates, Direction1D buttonPressed) {
        if (buttonPressed == Direction1D.LEFT) {
            if (canMakeMove) {
                Vec2i mouseOver = getSlotAtCoordinates(clickCoordinates);
                if (shapes.get(mouseOver.getX(), mouseOver.getY()) == null && !isShapeCurrentlyBeingDrawn()) {
                    GameblockPackets.sendToServer(new TicTacToeClientToServerDrawShapePacket(mouseOver));
                }
            }
        }
    }

    @Override
    public void render() {
        if (myType != null) {
            drawLine(-60 + 3, 20, 60 - 3, 20, 3, true, ColorF.WHITE);
            drawLine(-60 + 3, -20, 60 - 3, -20, 3, true, ColorF.WHITE);
            drawLine(20, -60 + 3, 20, 60 - 3, 3, true, ColorF.WHITE);
            drawLine(-20, -60 + 3, -20, 60 - 3, 3, true, ColorF.WHITE);

            shapes.forEach((Vec2i vec, TicTacToeShape shape) -> {
                if (shape != null) {
                    shape.type.render(this, new Vec2(vec.getX(), vec.getY()).scale(40), shape.timer.getProgress(), ColorF.WHITE);
                }
            });

            if (canMakeMove) {
                Vec2i hoveringOver = getSlotAtCoordinates(getMouseCoordinates());
                if (Math.abs(hoveringOver.getX()) <= 1 && Math.abs(hoveringOver.getY()) <= 1) {
                    if (shapes.get(hoveringOver.getX(), hoveringOver.getY()) == null && !isShapeCurrentlyBeingDrawn()) {
                        myType.render(this, new Vec2(hoveringOver.getX(), hoveringOver.getY()).scale(40), 1.0f, new ColorF(0.4f));
                    }
                }
            }
        } else if (prompt == null) {
            drawText(0.0f, 8.0f, 0.85f, ColorF.WHITE, Component.literal("Waiting for opponent")); // TODO: localize
            float f = -(getPartialTicks() + getGameTime()) * 0.25f;
            drawArc(0.0f, -8.0f, 3.5f, 6.8f, f, f + 1.2f, ColorF.WHITE);
        }
    }

    private Vec2i getSlotAtCoordinates(Vec2 pos) {
        return new Vec2i(Math.round(pos.x / 40), Math.round(pos.y / 40));
    }
}
