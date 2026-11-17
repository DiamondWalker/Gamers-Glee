package gameblock.item;

import gameblock.game.Game;
import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class CartridgeItem<T extends GameInstance> extends Item {
    private final RegistryObject<Game<T>> gameType;

    public CartridgeItem(RegistryObject<Game<T>> game) {
        super(new Item.Properties().stacksTo(1));
        this.gameType = game;
    }

    public Game<T> getGame() {
        return gameType.get();
    }
}
