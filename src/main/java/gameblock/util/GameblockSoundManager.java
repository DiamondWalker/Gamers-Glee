package gameblock.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;

public class GameblockSoundManager {
    public final ArrayList<SimpleSoundInstance> sounds = new ArrayList<>();

    public void play(SoundEvent event, float pitch, float volume) {
        SimpleSoundInstance sound = SimpleSoundInstance.forUI(event, pitch, volume);
        Minecraft.getInstance().getSoundManager().play(sound);
        sounds.add(sound);
    }

    public void update() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();;
        int i = 0;
        while (i < sounds.size()) {
            if (!soundManager.isActive(sounds.get(i))) {
                sounds.remove(i);
            } else {
                i++;
            }
        }
    }

    public void stopAll() {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        for (SimpleSoundInstance sound : sounds) if (soundManager.isActive(sound)) soundManager.stop(sound);
    }
}
