package gameblock.loot;

import gameblock.GameblockMod;
import gameblock.registry.GameblockItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class GameblockLootModifierProvider extends GlobalLootModifierProvider {
    public GameblockLootModifierProvider(PackOutput output) {
        super(output, GameblockMod.MODID);
    }

    @Override
    protected void start() {
        add("block_break_cartridge", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(new ResourceLocation("chests/simple_dungeon")).build(),
                LootItemRandomChanceCondition.randomChance(1.0f).build()}, GameblockItems.CARTRIDGE_BLOCK_BREAK.get()));
    }
}
