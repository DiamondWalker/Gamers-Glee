package gameblock.game.serpent;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction2D;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class SnakeDirectionChangePacket extends UpdateGamePacket<SerpentGame> {
    private Direction2D dir;

    public SnakeDirectionChangePacket(Direction2D dir) {
        super();
        this.dir = dir;
    }

    public SnakeDirectionChangePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(dir);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.dir = buffer.readEnum(Direction2D.class);
    }

    @Override
    public void handleGameUpdate(SerpentGame game) {
        game.setSnakeDirection(dir, true);
    }
}
