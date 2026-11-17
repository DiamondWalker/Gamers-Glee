package gameblock;

import com.mojang.logging.LogUtils;
import gameblock.registry.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GameblockMod.MODID)
public class GameblockMod
{
    public static final String MODID = "gameblock";

    public static final Logger LOGGER = LogUtils.getLogger();


    public GameblockMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        GameblockCustomRegistries.register(modEventBus);
        GameblockGames.register(modEventBus);
        GameblockItems.register(modEventBus);
        GameblockSounds.register(modEventBus);

        GameblockLootModifiers.register(modEventBus);

        context.getModEventBus().addListener(GameblockMod::commonSetup);

        context.registerConfig(ModConfig.Type.COMMON, GameblockConfig.SPEC);
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(GameblockPackets::registerPackets);
    }
}
