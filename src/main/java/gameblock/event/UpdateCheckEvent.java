package gameblock.event;

import gameblock.GameblockMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber
public class UpdateCheckEvent {
    public static final long FIRST_CHECK_TIME = 20 * 5; // 5 seconds after server open
    public static final long CHECK_INTERVAL = 20 * 60 * 60 * 3; // every 3 hours, notify again

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        long ticks = event.getServer().getTickCount() - FIRST_CHECK_TIME;

        if (event.phase == TickEvent.Phase.END && ticks % CHECK_INTERVAL == 0) {
            VersionChecker.Status version = VersionChecker.getResult(ModList.get().getModContainerById(GameblockMod.MODID).get().getModInfo()).status();

            MutableComponent modName = Component.literal("<Gamer's Glee>").withStyle(ChatFormatting.YELLOW);
            MutableComponent updateMsg = null;

            if (version == VersionChecker.Status.OUTDATED) {
                Random rand = new Random();
                if (ticks >= CHECK_INTERVAL * 2 && rand.nextInt(10) == 0) {
                    updateMsg = Component.translatable("chat.gameblock.update.out_of_date_special_" + rand.nextInt(8), modName);
                } else {
                    updateMsg = Component.translatable("chat.gameblock.update.out_of_date", modName);
                }

                updateMsg.withStyle(ChatFormatting.DARK_RED);

            } else if (ticks == FIRST_CHECK_TIME) {
                if (version == VersionChecker.Status.FAILED) {
                    updateMsg = Component.translatable("chat.gameblock.update.could_not_connect", modName).withStyle(ChatFormatting.DARK_PURPLE);
                } else {
                    updateMsg = Component.translatable("chat.gameblock.update.up_to_date", modName).withStyle(ChatFormatting.GREEN);
                }
            }

            if (updateMsg != null) event.getServer().getPlayerList().broadcastSystemMessage(updateMsg, false);
        }
    }
}
