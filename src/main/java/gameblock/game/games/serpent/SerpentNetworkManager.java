package gameblock.game.games.serpent;

import gameblock.game.GameUpdate;
import gameblock.game.GameUpdateType;
import gameblock.game.NetworkManager;
import gameblock.packet.GameUpdatePacket;
import gameblock.registry.GameblockSounds;
import gameblock.util.Direction2D;
import gameblock.util.Vec2i;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class SerpentNetworkManager extends NetworkManager<SerpentGame> {
    private final GameUpdateType<SerpentGame> snakeUpdate = registerMessage(this::receiveSnakeUpdate, true, false);
    private final GameUpdateType<SerpentGame> eatFood = registerMessage(this::receiveEatFood, false, true);

    public SerpentNetworkManager(SerpentGame game) {
        super(game);
    }

    public void sendSnakeUpdate(Direction2D snakeDirection, ArrayList<Vec2i> coords) {
        GameUpdate.Outgoing update = snakeUpdate.sendToServer()
                .add(snakeDirection.ordinal())
                .add(coords.size());

        for (Vec2i coord : coords) {
            update.add(coord.getX()).add(coord.getY());
        }
    }

    public void receiveSnakeUpdate(GameUpdate.Incoming packet, UUID sender) {
        game.setSnakeDirection(Direction2D.values()[packet.get(Integer.class)]);

        int segmentCount = packet.get(Integer.class);

        game.tiles.setAll((Integer num) -> Integer.MAX_VALUE);

        game.headX = packet.get(Integer.class);
        game.headY = packet.get(Integer.class);
        game.tiles.set(game.headX, game.headY, 0);

        for (int i = 1; i < segmentCount; i++) {
            game.tiles.set(packet.get(Integer.class), packet.get(Integer.class), i);
        }
    }

    public void sendEatFood(int newFoodX, int newFoodY, int segments, int foodEaten) {
        eatFood.sendToAllClients().add(newFoodX).add(newFoodY).add(segments).add(foodEaten);
    }

    public void receiveEatFood(GameUpdate.Incoming packet, UUID sender) {
        game.foodX = packet.get(Integer.class);
        game.foodY = packet.get(Integer.class);
        game.targetSnakeLength = packet.get(Integer.class);
        game.foodEaten = packet.get(Integer.class);
        if (game.foodEaten > 0) game.playSound(GameblockSounds.SERPENT_EAT.get(), 1.0f - new Random().nextFloat() * 0.2f, 1.0f);
    }
}
