package gameblock.game.serpent;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import gameblock.util.Direction2D;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EatFoodPacket extends UpdateGamePacket<SerpentGame> {
    private int newFoodX;
    private int newFoodY;
    private int newSnakeLength;
    private int foodEaten;

    public EatFoodPacket(int foodX, int foodY, int segments, int foodEaten) {
        this.newFoodX = foodX;
        this.newFoodY = foodY;
        this.newSnakeLength = segments;
        this.foodEaten = foodEaten;
    }

    public EatFoodPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(newFoodX);
        buffer.writeInt(newFoodY);
        buffer.writeInt(newSnakeLength);
        buffer.writeInt(foodEaten);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        this.newFoodX = buffer.readInt();
        this.newFoodY = buffer.readInt();
        this.newSnakeLength = buffer.readInt();
        this.foodEaten = buffer.readInt();
    }

    @Override
    public void gameUpdateReceivedOnClient(SerpentGame game) {
        game.foodX = newFoodX;
        game.foodY = newFoodY;
        game.targetSnakeLength = newSnakeLength;
        game.foodEaten = foodEaten;
        if (foodEaten > 0) game.playSound(GameblockSounds.SERPENT_EAT.get(), 1.0f - new Random().nextFloat() * 0.2f, 1.0f);
    }
}
