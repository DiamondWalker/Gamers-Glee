package gameblock.game.serpent;

import gameblock.packet.UpdateGamePacket;
import gameblock.util.Direction2D;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class EatFoodPacket extends UpdateGamePacket<SerpentGame> {
    private int newFoodX;
    private int newFoodY;
    private int newSnakeLength;

    public EatFoodPacket(int foodX, int foodY, int segments) {
        this.newFoodX = foodX;
        this.newFoodY = foodY;
        this.newSnakeLength = segments;
    }

    public EatFoodPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(newFoodX);
        buffer.writeInt(newFoodY);
        buffer.writeInt(newSnakeLength);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.newFoodX = buffer.readInt();
        this.newFoodY = buffer.readInt();
        this.newSnakeLength = buffer.readInt();
    }

    @Override
    public void handleGameUpdate(SerpentGame game) {
        game.foodX = newFoodX;
        game.foodY = newFoodY;
        game.snakeLength = newSnakeLength;
    }
}
