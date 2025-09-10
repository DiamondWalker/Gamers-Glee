package gameblock.cosmetics.particles;

import gameblock.registry.GameblockCosmetics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public abstract class BaseParticleCosmetic {
    public final GameblockCosmetics.CosmeticType type;
    protected final Player player;
    protected final boolean isClientSide;

    public BaseParticleCosmetic(Player player, GameblockCosmetics.CosmeticType type) {
        this.player = player;
        isClientSide = player.level().isClientSide();
        this.type = type;
    }

    public abstract void tick();

    public abstract boolean displaysInFirstPerson();
}
