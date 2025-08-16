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
    /*public static final RegistryObject<Item> CARTRIDGE_RHYTHM_FIGURES = ITEMS.register("rhythm_figures", () -> new CartridgeItem(Game::new)); // just shapes and beats
    public static final RegistryObject<Item> CARTRIDGE_SUPER_PLUMBER_DUDES = ITEMS.register("super_plumber_dudes", () -> new CartridgeItem(Game::new)); // super mario bros
    public static final RegistryObject<Item> CARTRIDGE_MILLIONAIRE = ITEMS.register("millionaire", () -> new CartridgeItem(Game::new)); // monopoly
    public static final RegistryObject<Item> CARTRIDGE_FIRST_FABLE = ITEMS.register("first_fable", () -> new CartridgeItem(Game::new)); // final fantasy
    public static final RegistryObject<Item> CARTRIDGE_NOM_NOM = ITEMS.register("nom_nom", () -> new CartridgeItem(Game::new)); // pacman
    public static final RegistryObject<Item> CARTRIDGE_SUBTERRANEAN_LEGEND = ITEMS.register("subterranean_legend", () -> new CartridgeItem(Game::new));
    public static final RegistryObject<Item> CARTRIDGE_BOUNCEART = ITEMS.register("bounceart", () -> new CartridgeItem(Game::new)); // inkball
    public static final RegistryObject<Item> CARTRIDGE_MEGA_MEAT_MAN = ITEMS.register("mega_meat_man", () -> new CartridgeItem(Game::new)); // super meat boy
    public static final RegistryObject<Item> CARTRIDGE_RAP_BATTLE = ITEMS.register("rap_battle", () -> new CartridgeItem(Game::new)); // friday night funkin'
    public static final RegistryObject<Item> CARTRIDGE_DEATH_OF_INNOCENCE = ITEMS.register("death_of_innocence", () -> new CartridgeItem(Game::new)); // binding of isaac
    public static final RegistryObject<Item> CARTRIDGE_SUBTERRANEAN_LEGEND = ITEMS.register("subterranean_legend", () -> new CartridgeItem(Game::new)); // undertale
    public static final RegistryObject<Item> CARTRIDGE_EMPTY_WARRIOR = ITEMS.register("empty_warrior", () -> new CartridgeItem(Game::new)); // hollow knight
    public static final RegistryObject<Item> CARTRIDGE_CELESTIAL = ITEMS.register("celestial", () -> new CartridgeItem(Game::new)); // celeste*/


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
