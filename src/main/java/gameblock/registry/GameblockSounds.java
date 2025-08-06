package gameblock.registry;

import gameblock.GameblockMod;
import gameblock.item.GameblockItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GameblockSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GameblockMod.MODID);

    public static final RegistryObject<SoundEvent> OS_MUSIC = registerSound("os_music");
    public static final RegistryObject<SoundEvent> SNAKE_MUSIC = registerSound("snake_music");
    public static final RegistryObject<SoundEvent> FLYING_CHICKEN_MUSIC = registerSound("flying_chicken_music");
    public static final RegistryObject<SoundEvent> BLOCK_BREAK_MUSIC = registerSound("block_break_music");

    public static final RegistryObject<SoundEvent> BALL_BOUNCE = registerSound("ball_bounce");
    public static final RegistryObject<SoundEvent> SNAKE_DEATH = registerSound("snake_death");


    private static RegistryObject<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(GameblockMod.MODID, name)));
    }

    public static void register(IEventBus bus) {
        SOUNDS.register(bus);
    }
}
