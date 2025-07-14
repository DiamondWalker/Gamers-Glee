package gameblock.item;

import gameblock.game.Game;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.lang.reflect.Constructor;

public class CartridgeItem<T extends Game> extends Item {
    private final Class<T> gameType;

    public CartridgeItem(Class<T> game) {
        super(new Item.Properties().stacksTo(1));
        this.gameType = game;
    }

    public T getNewGameInstance(Player player) {
        try {
            Constructor<T> constructor = gameType.getConstructor(Player.class);
            return constructor.newInstance(player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInstance(Game game) {
        return this.gameType.isInstance(game);
    }
}
