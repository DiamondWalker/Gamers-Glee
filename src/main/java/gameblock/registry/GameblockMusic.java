package gameblock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;

public class GameblockMusic {
    public static final Music OS = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GameblockSounds.OS_MUSIC.get()), 0, 0, true);
    public static final Music SNAKE = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GameblockSounds.SNAKE_MUSIC.get()), 0, 0, true);
    public static final Music FLYING_CHICKEN = new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(GameblockSounds.FLYING_CHICKEN_MUSIC.get()), 0, 0, true);
}
