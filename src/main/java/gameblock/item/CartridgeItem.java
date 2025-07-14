package gameblock.item;

import gameblock.game.Game;
import net.minecraft.world.item.Item;

public class CartridgeItem<T extends Game> extends Item {
    private final Class<T> gameType;

    public CartridgeItem(Class<T> game) {
        super(new Item.Properties().stacksTo(1));
        this.gameType = game;
    }

    public T getNewGameInstance() {
        try {
            return gameType.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInstance(Game game) {
        return this.gameType.isInstance(game);
    }
}
