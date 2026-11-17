package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.game.Game;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GameblockCustomRegistries {
    public static final DeferredRegister<Game<?>> GAMES_REGISTER = DeferredRegister.create(ResourceLocation.fromNamespaceAndPath(GameblockMod.MODID, "games"), GameblockMod.MODID);
    public static final Supplier<IForgeRegistry<Game<?>>> GAMES = GAMES_REGISTER.makeRegistry(RegistryBuilder::new);

    public static void register(IEventBus bus) {
        GAMES_REGISTER.register(bus);
    }
}
