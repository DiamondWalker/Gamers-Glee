package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.game.defusal.DefusalGame;
import gameblock.item.CartridgeItem;
import gameblock.item.GameblockItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GameblockItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, GameblockMod.MODID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GameblockMod.MODID);

    public static final RegistryObject<Item> GAMEBLOCK = ITEMS.register("gameblock", GameblockItem::new);
    public static final RegistryObject<Item> CARTRIDGE_BLOCK_BREAK = ITEMS.register("block_break", () -> new CartridgeItem<>(GameblockGames.BLOCK_BREAK_GAME)); // block break
    public static final RegistryObject<Item> CARTRIDGE_SERPENT = ITEMS.register("serpent", () -> new CartridgeItem<>(GameblockGames.SERPENT_GAME)); // snake
    public static final RegistryObject<Item> CARTRIDGE_FLYING_CHICKEN = ITEMS.register("flying_chicken", () -> new CartridgeItem<>(GameblockGames.FLYING_CHICKEN_GAME)); // flappy bird
    public static final RegistryObject<Item> CARTRIDGE_DEFUSAL = ITEMS.register("defusal", () -> new CartridgeItem<>(GameblockGames.DEFUSAL_GAME)); // minesweeper
    public static final RegistryObject<Item> CARTRIDGE_PADDLES = ITEMS.register("paddles", () -> new CartridgeItem<>(GameblockGames.PADDLES_GAME));


    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("games", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GAMEBLOCK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(GAMEBLOCK.get());
                output.accept(CARTRIDGE_BLOCK_BREAK.get());
                output.accept(CARTRIDGE_SERPENT.get());
                output.accept(CARTRIDGE_FLYING_CHICKEN.get());
                output.accept(CARTRIDGE_DEFUSAL.get());
            }).build());

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }
}
