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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class GameblockGames {
    private static final HashMap<String, Game> registry = new HashMap<>();

    public static final Game<GameblockOS> GAMEBLOCK_OS = registerGame("gameblock_os", GameblockOS.class);
    public static final Game<BlockBreakGame> BLOCK_BREAK_GAME = registerGame("block_break", BlockBreakGame.class);
    public static final Game<SerpentGame> SERPENT_GAME = registerGame("serpent", SerpentGame.class);
    public static final Game<FlyingChickenGame> FLYING_CHICKEN_GAME = registerGame("flying_chicken", FlyingChickenGame.class);
    public static final Game<DefusalGame> DEFUSAL_GAME = registerGame("defusal", DefusalGame.class);
    public static final Game<PaddlesGame> PADDLES_GAME = registerGame("paddles", PaddlesGame.class);

    public static <T extends GameInstance> Game<T> registerGame(String name, Class<T> clazz) {
        Game<T> game = new Game<T>(clazz, name, new ResourceLocation(GameblockMod.MODID, "textures/gui/logo/" + name + ".png"));
        registry.put(name, game);
        return game;
    }

    public static <T extends GameInstance> Game<T> getGame(String key) {
        return registry.get(key);
    }

    public static class Game<T extends GameInstance> {
        public final Class<T> gameClass;
        public final String gameID;
        public final ResourceLocation logo;

        private Game(Class<T> clazz, String id, ResourceLocation tex) {
            this.gameClass = clazz;
            this.gameID = id;
            this.logo = tex;
        }

        public T createInstance(Player player) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
            Constructor<T> constructor = gameClass.getConstructor(Player.class);
            return constructor.newInstance(player);
        }
    }
}
