package gameblock.registry;

import gameblock.cosmetics.particles.*;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;

public class GameblockCosmetics {
    private static final HashMap<String, CosmeticType> REGISTRY = new HashMap<>();

    public static final CosmeticType FIRE_TRAIL = register("fire_trail", FireTrailParticleCosmetic::new);
    public static final CosmeticType RAINBOW_TRAIL = register("rainbow_trail", RainbowTrailParticleCosmetic::new);
    public static final CosmeticType SATURN = register("saturn", SaturnParticleCosmetic::new);
    public static final CosmeticType TRANS = register("trans", TransParticleCosmetic::new);
    public static final CosmeticType RAIN_CLOUD = register("rain_cloud", RainCloudCosmetic::new);
    public static final CosmeticType PENTAGRAM = register("pentagram", PentagramParticleCosmetic::new);
    public static final CosmeticType PATRIOTIC_TRAIL = register("patriotic_trail", PatrioticTrailParticleCosmetic::new);
    public static final CosmeticType COSMIC_TRAIL = register("cosmic_trail", CosmicTrailParticleCosmetic::new);
    public static final CosmeticType BLOOD_HELIX = register("blood_helix", BloodHelixParticleCosmetic::new);

    public static Collection<CosmeticType> getAllTypes() {
        return REGISTRY.values();
    }

    public static CosmeticType getTypeFromID(String id) {
        return REGISTRY.get(id);
    }

    private static CosmeticType register(String id, Function<Player, BaseParticleCosmetic> constructor) {
        CosmeticType type = new CosmeticType(id, constructor);
        if (REGISTRY.containsKey(id)) throw new IllegalStateException("Duplicate cosmetic id: " + id + "!");
        REGISTRY.put(id, type);
        return type;
    }

    public static class CosmeticType {
        public final String id;
        public final Function<Player, BaseParticleCosmetic> constructor;

        private CosmeticType(String id, Function<Player, BaseParticleCosmetic> constructor) {
            this.id = id;
            this.constructor = constructor;
        }
    }
}
