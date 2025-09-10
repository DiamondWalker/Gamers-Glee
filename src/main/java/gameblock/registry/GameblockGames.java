package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.game.defusal.DefusalGame;
import gameblock.game.flyingchicken.FlyingChickenGame;
import gameblock.game.os.GameblockOS;
import gameblock.game.paddles.PaddlesGame;
import gameblock.game.serpent.SerpentGame;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.function.Function;

public class GameblockGames {
    private static final HashMap<String, Game> REGISTRY = new HashMap<>();

    public static final Game<GameblockOS> GAMEBLOCK_OS = registerGame("gameblock_os", GameblockOS::new);
    public static final Game<BlockBreakGame> BLOCK_BREAK_GAME = registerGame("block_break", BlockBreakGame::new);
    public static final Game<SerpentGame> SERPENT_GAME = registerGame("serpent", SerpentGame::new);
    public static final Game<FlyingChickenGame> FLYING_CHICKEN_GAME = registerGame("flying_chicken", FlyingChickenGame::new);
    public static final Game<DefusalGame> DEFUSAL_GAME = registerGame("defusal", DefusalGame::new);
    public static final Game<PaddlesGame> PADDLES_GAME = registerGame("paddles", PaddlesGame::new);

    public static <T extends GameInstance> Game<T> registerGame(String name, Function<Player, T> constructor) {
        Game<T> game = new Game<T>(constructor, name, new ResourceLocation(GameblockMod.MODID, "textures/gui/logo/" + name + ".png"));
        REGISTRY.put(name, game);
        return game;
    }

    public static <T extends GameInstance> Game<T> getGame(String key) {
        return REGISTRY.get(key);
    }

    public static class Game<T extends GameInstance> {
        public final Function<Player, T> constructor;
        public final String gameID;
        public final ResourceLocation logo;

        private Game(Function<Player, T> constructor, String id, ResourceLocation tex) {
            this.constructor = constructor;
            this.gameID = id;
            this.logo = tex;
        }

        public T createInstance(Player player) {
            return constructor.apply(player);
        }
    }
}
