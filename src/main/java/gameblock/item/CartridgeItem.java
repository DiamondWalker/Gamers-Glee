package gameblock.item;

import gameblock.game.GameInstance;
import gameblock.registry.GameRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

import java.lang.reflect.Constructor;

public class CartridgeItem<T extends GameInstance> extends Item {
    public final GameRegistry.Game<T> gameType;

    public CartridgeItem(GameRegistry.Game<T> game) {
        super(new Item.Properties().stacksTo(1));
        this.gameType = game;
    }

    public T getNewGameInstance(Player player) {
        try {
            return gameType.createInstance(player);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInstance(GameInstance game) {
        return this.gameType.isInstance(game);
    }
}
