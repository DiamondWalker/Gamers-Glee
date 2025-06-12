package gameblock;

import gameblock.game.Game;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Function;
import java.util.function.Supplier;

public class CartridgeItem extends Item {
    private final Supplier<Game> game;
    public CartridgeItem(Supplier<Game> game) {
        super(new Item.Properties().stacksTo(1));
        this.game = game;
    }

    public Game getNewGameInstance() {
        return game.get();
    }
}
