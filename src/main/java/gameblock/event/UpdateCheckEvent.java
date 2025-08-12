package gameblock.event;

import gameblock.GameblockMod;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class UpdateCheckEvent {
    public static final long FIRST_CHECK_TIME = 20 * 5; // 5 seconds after server open
    public static final long CHECK_INTERVAL = 20 * 60 * 60 * 2; // every 2 hours, notify again

    public static final String[] UPDATE_MESSAGES = new String[] {
            "§e<Gamer's Glee> §4update ur gamers glee bruh",
            "§e<Gamer's Glee> §4Update or die.",
            "§e<Gamer's Glee> §4Not updating your Minecraft mods is a federal offense punishable by life in prison without parole.",
            "§e<Gamer's Glee> §4Update Gamer's Glee. All the cool kids are doing it.",
            "§e<Gamer's Glee> §4Update Gamer's Glee or the man standing behind you will do it for you.",
            "§e<Gamer's Glee> §4Update plz",
            "§e<Gamer's Glee> §4Update me.",
            "§e<Gamer's Glee> §4:("
    };

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        long ticks = event.getServer().getTickCount() - FIRST_CHECK_TIME;

        if (event.phase == TickEvent.Phase.END && ticks % CHECK_INTERVAL == 0) {
            VersionChecker.Status version = VersionChecker.getResult(ModList.get().getModContainerById(GameblockMod.MODID).get().getModInfo()).status();
            // TODO: localization
            if (version == VersionChecker.Status.OUTDATED) {
                Random rand = new Random();
                if (ticks >= CHECK_INTERVAL * 2 && rand.nextInt(10) == 0) {
                    event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(UPDATE_MESSAGES[rand.nextInt(UPDATE_MESSAGES.length)]), false);
                } else {
                    event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("§e<Gamer's Glee> §4Gamer's Glee is out of date! Update for new features and fixes!"), false);
                }
            } else if (ticks == FIRST_CHECK_TIME) {
                if (version == VersionChecker.Status.FAILED) {
                    event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("§e<Gamer's Glee> §5Could not connect to update checker."), false);
                } else {
                    event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("§e<Gamer's Glee> §aGamer's Glee is up to date. Enjoy!"), false);
                }
            }
        }
    }
}
