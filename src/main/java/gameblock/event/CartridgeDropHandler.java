package gameblock.event;

import gameblock.registry.GameblockItems;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CartridgeDropHandler {
    @SubscribeEvent
    public static void handleMobDrop(LivingDropsEvent event) {
        LivingEntity dropper = event.getEntity();
        if (dropper.getType() == EntityType.ENDERMAN && event.getSource().getEntity() instanceof Creeper) {
            Item[] cartridges = new Item[] {
                    GameblockItems.CARTRIDGE_DEFUSAL.get(),
                    GameblockItems.CARTRIDGE_SERPENT.get(),
                    GameblockItems.CARTRIDGE_BLOCK_BREAK.get(),
                    GameblockItems.CARTRIDGE_FLYING_CHICKEN.get()
            };
            ItemStack item = new ItemStack(cartridges[dropper.getRandom().nextInt(cartridges.length)]);
            ItemEntity itementity = new ItemEntity(dropper.level(), dropper.getX(), dropper.getY(), dropper.getZ(), item);
            itementity.setDefaultPickUpDelay();
            event.getDrops().add(itementity);
        }
    }
}
