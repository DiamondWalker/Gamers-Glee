package gameblock.game.serpent;

import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction2D;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SnakeUpdatePacket extends UpdateGamePacket<SerpentGame> {
    private Direction2D dir;
    private List<Integer> coordinates;
    public SnakeUpdatePacket(Direction2D dir, ArrayList<Integer> segments) {
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
        for (Integer i : coordinates) buffer.writeInt(i);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.dir = buffer.readEnum(Direction2D.class);

        int size = buffer.readInt();
        coordinates = new ArrayList<>(size);
        for (int i = 0; i < size; i++) coordinates.add(buffer.readInt());
    }

    @Override
    public void handleGameUpdate(SerpentGame game) {
        game.tiles.setAll((Integer num) -> Integer.MAX_VALUE);
        game.headX = coordinates.get(0);
        game.headY = coordinates.get(1);
        for (int i = 0; i < coordinates.size(); i += 2) {
            game.tiles.set(coordinates.get(i), coordinates.get(i + 1), i / 2);
        }

        game.setSnakeDirection(dir, false);
    }
}
