package gameblock.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

public class ChatHelper {
    public static void sendModMessageToAllPlayers(MinecraftServer server, Component msg) {
        server.getPlayerList().broadcastSystemMessage(Component.literal("<Gamer's Glee> ").withStyle(ChatFormatting.YELLOW).append(msg), false);
    }
}
