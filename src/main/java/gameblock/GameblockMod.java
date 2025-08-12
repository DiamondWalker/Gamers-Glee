package gameblock;

import com.mojang.logging.LogUtils;
import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.game.flyingchicken.FlyingChickenGame;
import gameblock.game.serpent.SerpentGame;
import gameblock.item.CartridgeItem;
import gameblock.item.GameblockItem;
import gameblock.registry.GameblockItems;
import gameblock.registry.GameblockLootModifiers;
import gameblock.registry.GameblockPackets;
import gameblock.registry.GameblockSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(GameblockMod.MODID)
public class GameblockMod
{
    public static final String MODID = "gameblock";

    public static final Logger LOGGER = LogUtils.getLogger();


    public GameblockMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

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
