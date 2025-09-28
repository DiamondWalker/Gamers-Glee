package gameblock.game.tictactoe.packets;

import gameblock.GameblockMod;
import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.game.tictactoe.TicTacToeShape;
import gameblock.game.tictactoe.TicTacToeShapeType;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.physics.Vec2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class TicTacToeClientToServerDrawShapePacket extends UpdateGamePacket<TicTacToeGame> {
    public Vec2i tile;

    public TicTacToeClientToServerDrawShapePacket(Vec2i mouseOver) {
        tile = mouseOver;
    }

    public TicTacToeClientToServerDrawShapePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(tile.getX());
        buffer.writeInt(tile.getY());
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        tile = new Vec2i(buffer.readInt(), buffer.readInt());
    }

    @Override
    public void gameUpdateReceivedOnServer(TicTacToeGame game, ServerPlayer sender) {
        TicTacToeShapeType playerShape = game.getGamePlayer(sender).data().shape;

        if (playerShape != game.getNextShape()) {
            GameblockMod.LOGGER.warn("Tic-tac-toe player attempted to draw shape when it wasn't their turn");
            return;
        }
        if (Math.abs(tile.getX()) > 1 || Math.abs(tile.getY()) > 1) {
            GameblockMod.LOGGER.warn("Tic-tac-toe player attempted to draw shape in invalid tile");
            return;
        }
        if (game.shapes.get(tile.getX(), tile.getY()) != null) {
            GameblockMod.LOGGER.warn("Player attempted to draw shape in tile that already had a shape. This shouldn't happen!");
        }

        game.shapes.set(tile.getX(), tile.getY(), new TicTacToeShape(game, playerShape));
        game.setNextShape(game.getNextShape() == TicTacToeShapeType.X ? TicTacToeShapeType.O : TicTacToeShapeType.X);
        game.sendToAllPlayers(new TicTacToeServerToClientDrawShapePacket(tile, playerShape), null);
    }
}
