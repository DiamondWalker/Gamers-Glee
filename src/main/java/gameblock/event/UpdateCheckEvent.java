package gameblock.event;

import gameblock.GameblockMod;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class UpdateCheckEvent {
    public static final long FIRST_CHECK_TIME = 20 * 5; // 5 seconds after server open
    public static final long CHECK_INTERVAL = 20 * 60 * 60 * 2; // every 2 hours, notify again

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        long ticks = event.getServer().getTickCount() - FIRST_CHECK_TIME;

        if (event.phase == TickEvent.Phase.END && ticks % CHECK_INTERVAL == 0) {
            VersionChecker.Status version = VersionChecker.getResult(ModList.get().getModContainerById(GameblockMod.MODID).get().getModInfo()).status();
            if (version == VersionChecker.Status.OUTDATED) {
                if (ticks >= CHECK_INTERVAL * 2 /*&& new Random().nextInt(100) == 0*/) {
                    event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("§e<Gamer's Glee> §4update ur gamers glee bruh"), false);
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
