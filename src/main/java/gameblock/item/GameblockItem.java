package gameblock.item;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.Game;
import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.gui.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GameblockItem extends Item {
    public GameblockItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (pUsedHand == InteractionHand.MAIN_HAND) {
            if (pPlayer.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CartridgeItem cartridge) {
                Game gameInstance = cartridge.getNewGameInstance();

                if (pLevel.isClientSide()) {
                    Minecraft.getInstance().setScreen(new GameScreen(gameInstance));
                } else {
                    GameCapability cap = pPlayer.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
                    if (cap != null) {
                        cap.setGame(gameInstance);
                    } else {
                        return InteractionResultHolder.fail(itemstack);
                    }
                }

                pPlayer.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.success(itemstack);
            }
        }
        return InteractionResultHolder.fail(itemstack);
    }
}
