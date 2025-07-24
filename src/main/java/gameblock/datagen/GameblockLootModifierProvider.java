package gameblock.datagen;

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
                LootItemRandomChanceCondition.randomChance(0.2f).build()}, GameblockItems.CARTRIDGE_BLOCK_BREAK.get()));

        add("serpent_cartridge", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(new ResourceLocation("chests/jungle_temple")).build(),
                LootItemRandomChanceCondition.randomChance(0.4f).build()}, GameblockItems.CARTRIDGE_SERPENT.get()));

        /*add("defusal_cartridge", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(new ResourceLocation("chests/desert_pyramid")).build(),
                LootItemRandomChanceCondition.randomChance(0.05f).build()}, GameblockItems.CARTRIDGE_DEFUSAL.get()));*/

        add("flying_chicken_cartridge", new AddItemModifier(new LootItemCondition[] {
                new LootTableIdCondition.Builder(new ResourceLocation("chests/pillager_outpost")).build(),
                LootItemRandomChanceCondition.randomChance(0.5f).build()}, GameblockItems.CARTRIDGE_FLYING_CHICKEN.get()));
    }
}
