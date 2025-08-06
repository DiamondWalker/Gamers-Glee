package gameblock.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

public class GameblockMusic {
    public static final Music OS = registerGameMusic(GameblockSounds.OS_MUSIC.get());
    public static final Music SNAKE = registerGameMusic(GameblockSounds.SNAKE_MUSIC.get());
    public static final Music FLYING_CHICKEN = registerGameMusic(GameblockSounds.FLYING_CHICKEN_MUSIC.get());
    public static final Music BLOCK_BREAK = registerGameMusic(GameblockSounds.BLOCK_BREAK_MUSIC.get());

    private static Music registerGameMusic(SoundEvent event) {
        return new Music(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), 0, 0, true);
    }
}
