package gameblock.game.defusal.packets;

import gameblock.game.defusal.DefusalGame;
import gameblock.game.defusal.DefusalTile;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

public class TileStatePacket extends UpdateGamePacket<DefusalGame> {
    private Vec2i tile;
    private DefusalTile.State state;

    public TileStatePacket(Vec2i tile, DefusalTile.State state) {
        this.tile = tile;
        this.state = state;
    }

    public TileStatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeShort(tile.getX());
        buffer.writeShort(tile.getY());
        buffer.writeEnum(state);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        tile = new Vec2i(buffer.readShort(), buffer.readShort());
        state =  buffer.readEnum(DefusalTile.State.class);
    }

    @Override
    public void gameUpdateReceivedOnClient(DefusalGame game) {
        game.tiles.get(tile.getX(), tile.getY()).setState(state);
    }
}
