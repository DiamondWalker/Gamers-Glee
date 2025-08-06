package gameblock.item;

import gameblock.game.GameInstance;
import gameblock.registry.GameblockGames;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public class CartridgeItem<T extends GameInstance> extends Item {
    public final GameblockGames.Game<T> gameType;

    public CartridgeItem(GameblockGames.Game<T> game) {
        super(new Item.Properties().stacksTo(1));
        this.gameType = game;
    }
}
