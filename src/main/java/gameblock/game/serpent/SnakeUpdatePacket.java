package gameblock.game.serpent;

import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction2D;
import gameblock.util.Vec2i;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SnakeUpdatePacket extends UpdateGamePacket<SerpentGame> {
    private Direction2D dir;
    private List<Vec2i> coordinates;
    public SnakeUpdatePacket(Direction2D dir, ArrayList<Vec2i> segments) {
        super();
        this.dir = dir;
        this.coordinates = segments;
    }

    public SnakeUpdatePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(dir);
        buffer.writeInt(coordinates.size());
        for (Vec2i i : coordinates) {
            buffer.writeInt(i.getX());
            buffer.writeInt(i.getY());
        }
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.dir = buffer.readEnum(Direction2D.class);
        int size = buffer.readInt();
        coordinates = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            coordinates.add(new Vec2i(buffer.readInt(), buffer.readInt()));
        }
    }

    @Override
    public void handleGameUpdate(SerpentGame game) {
        synchronized(game.tiles) {
            game.setSnakeDirection(dir);
            game.tiles.setAll((Integer num) -> Integer.MAX_VALUE);
            game.headX = coordinates.get(0).getX();
            game.headY = coordinates.get(0).getY();
            for (int i = 0; i < coordinates.size(); i++) {
                game.tiles.set(coordinates.get(i).getX(), coordinates.get(i).getY(), i);
            }
        }
    }
}
