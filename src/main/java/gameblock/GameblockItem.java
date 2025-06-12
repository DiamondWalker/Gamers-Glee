package gameblock;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class GameblockItem extends Item {
    public GameblockItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (pLevel.isClientSide()) {
            if (pUsedHand == InteractionHand.MAIN_HAND) {
                if (pPlayer.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof CartridgeItem cartridge) {
                    Minecraft.getInstance().setScreen(new GameScreen(cartridge.getNewGameInstance()));
                    pPlayer.awardStat(Stats.ITEM_USED.get(this));
                    return InteractionResultHolder.success(itemstack);
                }
            }
        }
        return InteractionResultHolder.fail(itemstack);
    }
}
