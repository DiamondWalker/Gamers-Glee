package gameblock.util;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class ServerSafeMinecraftAccess {
    public static void accessPlayerObject(Consumer<Player> func) {
        func.accept(Minecraft.getInstance().player);
    }

    public static boolean isFirstPersonCharacter(Player player) {
        return Minecraft.getInstance().player == player && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
    }
}
