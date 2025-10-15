package gameblock.game.tictactoe.packets;

import gameblock.GameblockMod;
import gameblock.game.tictactoe.TicTacToeGame;
import gameblock.game.tictactoe.TicTacToeShape;
import gameblock.game.tictactoe.TicTacToeShapeType;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.physics.Vec2i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class TicTacToeServerToClientDrawShapePacket extends UpdateGamePacket<TicTacToeGame> {
    public Vec2i tile;
    public TicTacToeShapeType shape;

    public TicTacToeServerToClientDrawShapePacket(Vec2i mouseOver, TicTacToeShapeType shape) {
        tile = mouseOver;
        this.shape = shape;
    }

    public TicTacToeServerToClientDrawShapePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(tile.getX());
        buffer.writeInt(tile.getY());
        buffer.writeEnum(shape);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        tile = new Vec2i(buffer.readInt(), buffer.readInt());
        shape = buffer.readEnum(TicTacToeShapeType.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(TicTacToeGame game) {
        game.shapes.set(tile.getX(), tile.getY(), new TicTacToeShape(game, shape));
    }
}
