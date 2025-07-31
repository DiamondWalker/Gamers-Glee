package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.game.GameInstance;
import gameblock.game.blockbreak.BlockBreakGame;
import gameblock.game.flyingchicken.FlyingChickenGame;
import gameblock.game.serpent.SerpentGame;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class GameRegistry {
    private static final HashMap<ResourceLocation, Game> registry = new HashMap<>();

    public static final Game<BlockBreakGame> BLOCK_BREAK_GAME = registerGame("block_break", BlockBreakGame.class);
    public static final Game<SerpentGame> SERPENT_GAME = registerGame("serpent", SerpentGame.class);
    public static final Game<FlyingChickenGame> FLYING_CHICKEN_GAME = registerGame("flying_chicken", FlyingChickenGame.class);

    public static <T extends GameInstance> Game<T> registerGame(String name, Class<T> clazz) {
        ResourceLocation id = new ResourceLocation(GameblockMod.MODID, name);
        Game<T> game = new Game<T>(clazz, id, new ResourceLocation(GameblockMod.MODID, "textures/gui/game/" + name + ".png"));
        registry.put(id, game);
        return game;
    }

    public static <T extends GameInstance> Game<T> getGame(ResourceLocation key) {
        return registry.get(key);
    }

    public static class Game<T extends GameInstance> {
        public final Class<T> gameClass;
        public final ResourceLocation gameID; // TODO: change to something other than ResourceLocation
        public final ResourceLocation logo;

        private Game(Class<T> clazz, ResourceLocation id, ResourceLocation tex) {
            this.gameClass = clazz;
            this.gameID = id;
            this.logo = tex;
        }

        public T createInstance(Player player) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException { // TODO: remove
            Constructor<T> constructor = gameClass.getConstructor(Player.class);
            return constructor.newInstance(player);
        }

        public boolean isInstance(GameInstance instance) {
            return gameClass.isInstance(instance);
        }
    }
}
