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
    public InteractionResultHolder<ItemStack> use(Level p_43449_, Player p_43450_, InteractionHand p_43451_) {
        ItemStack itemstack = p_43450_.getItemInHand(p_43451_);
        if (p_43449_.isClientSide()) Minecraft.getInstance().setScreen(new GameScreen());
        p_43450_.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemstack, p_43449_.isClientSide());
    }
}
