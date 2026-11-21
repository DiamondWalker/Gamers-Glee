package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.game.Game;
import gameblock.game.games.blockbreak.BlockBreakGame;
import gameblock.game.games.defusal.DefusalGame;
import gameblock.game.games.flyingchicken.FlyingChickenGame;
import gameblock.game.games.os.GameblockOS;
import gameblock.game.games.serpent.SerpentGame;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class GameblockGames {
    public static final DeferredRegister<Game<?>> GAMES = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(GameblockMod.MODID, "games"), GameblockMod.MODID);

    public static final RegistryObject<Game<GameblockOS>> GAMEBLOCK_OS = GAMES.register("gameblock_os", () -> new Game<>(GameblockOS::new));
    public static final RegistryObject<Game<BlockBreakGame>> BLOCK_BREAK = GAMES.register("block_break", () -> new Game<>(BlockBreakGame::new));
    public static final RegistryObject<Game<SerpentGame>> SERPENT = GAMES.register("serpent", () -> new Game<>(SerpentGame::new));
    public static final RegistryObject<Game<FlyingChickenGame>> FLYING_CHICKEN = GAMES.register("flying_chicken", () -> new Game<>(FlyingChickenGame::new));
    public static final RegistryObject<Game<DefusalGame>> DEFUSAL = GAMES.register("defusal", () -> new Game<>(DefusalGame::new));
    //public static final RegistryObject<Game<PaddlesGame>> PADDLES = GAMES.register("paddles", () -> new Game<>(PaddlesGame::new));

    public static void register(IEventBus bus) {
        GAMES.register(bus);
    }
}
