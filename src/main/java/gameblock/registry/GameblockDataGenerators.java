package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.datagen.GameblockLootModifierProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GameblockMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameblockDataGenerators {
    @SubscribeEvent
    public static void generateData(GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(), new GameblockLootModifierProvider(event.getGenerator().getPackOutput()));
    }
}
