package gameblock.item;

import gameblock.capability.GameCapability;
import gameblock.capability.GameCapabilityProvider;
import gameblock.game.GameInstance;
import gameblock.game.os.GameblockOS;
import gameblock.registry.GameblockGames;
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
        if (!pLevel.isClientSide()) {
            GameCapability cap = pPlayer.getCapability(GameCapabilityProvider.CAPABILITY_GAME, null).orElse(null);
            if (cap != null) {
                cap.setGame(GameblockGames.GAMEBLOCK_OS.createInstance(pPlayer));
                pPlayer.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
            }
        }

        return InteractionResultHolder.fail(pPlayer.getItemInHand(pUsedHand));
    }
}
