package gameblock.game.defusal;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction1D;
import gameblock.util.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

public class TileClickPacket extends UpdateGamePacket<DefusalGame> {
    private Direction1D mouseClick;
    private Vec2i tileClicked;

    public TileClickPacket(Direction1D mouse, Vec2i tile) {
        this.mouseClick = mouse;
        this.tileClicked = tile;
    }

    public TileClickPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(mouseClick);
        buffer.writeShort(tileClicked.getX());
        buffer.writeShort(tileClicked.getY());
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        mouseClick = buffer.readEnum(Direction1D.class);
        tileClicked = new Vec2i(buffer.readShort(), buffer.readShort());
    }

    @Override
    public void handleGameUpdate(DefusalGame game) {
        if (mouseClick == Direction1D.LEFT) {
            game.reveal(tileClicked);
        } else if (mouseClick == Direction1D.RIGHT) {
            // cycle
        }
    }
}
