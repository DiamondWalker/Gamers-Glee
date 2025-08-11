package gameblock.game.defusal;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

public class TileRevealPacket extends UpdateGamePacket<DefusalGame> {
    private TileInfo[] tiles;

    public TileRevealPacket(TileInfo[] tiles) {
        this.tiles = tiles;
    }

    public TileRevealPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(tiles.length);
        for (TileInfo tile : tiles) {
            buffer.writeShort(tile.coords.getX());
            buffer.writeShort(tile.coords.getY());
            buffer.writeByte(tile.adjacentBombs);
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        tiles = new TileInfo[buffer.readInt()];

        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = new TileInfo(new Vec2i(buffer.readShort(), buffer.readShort()), buffer.readByte());
        }
    }

    @Override
    public void handleGameUpdate(DefusalGame game) {
        for (TileInfo tile : tiles) {
            DefusalTile defusalTile = game.tiles.get(tile.coords.getX(), tile.coords.getY());
            defusalTile.adjacentBombs = tile.adjacentBombs;
            defusalTile.reveal();
            game.lastRevealTime = game.getGameTime();
        }
    }

    public static class TileInfo {
        private Vec2i coords;
        private int adjacentBombs;

        public TileInfo(Vec2i coords, int adjacentBombs) {
            this.coords = coords;
            this.adjacentBombs = adjacentBombs;
        }
    }
}
