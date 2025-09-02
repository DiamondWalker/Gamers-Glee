package gameblock.event;

import gameblock.GameblockConfig;
import gameblock.GameblockMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
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
            VersionChecker.CheckResult version = VersionChecker.getResult(ModList.get().getModContainerById(GameblockMod.MODID).get().getModInfo());

            boolean firstCheck = ticks == 0;

            MutableComponent modName = Component.literal("<Gamer's Glee>").withStyle(ChatFormatting.YELLOW);
            MutableComponent updateMsg = null;

            if (version.status() == VersionChecker.Status.OUTDATED) {
                if (firstCheck || GameblockConfig.REPEAT_UPDATE_NOTIFICATION.get()) {
                    Random rand = new Random();
                    if (!firstCheck && rand.nextInt(5) == 0) {
                        updateMsg = Component.translatable("chat.gameblock.update.out_of_date_special_" + rand.nextInt(8), modName)
                                .withStyle(ChatFormatting.DARK_RED);
                    } else {
                        Component linkComponent = Component.translatable("chat.gameblock.update.download").withStyle(style -> style
                                .applyFormats(ChatFormatting.DARK_RED, ChatFormatting.UNDERLINE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, version.url() + "/version/" + version.target())));
                        Component versionsComponent = Component.translatable("chat.gameblock.update.out_of_date_" + (version.changes().size() == 1 ? "singular" : "multiple"), version.changes().size())
                                .withStyle(ChatFormatting.DARK_RED);
                        updateMsg = Component.translatable("chat.gameblock.update.out_of_date", modName, versionsComponent, linkComponent)
                                .withStyle(ChatFormatting.DARK_RED);
                    }
                }

            } else if (firstCheck) {
                if (version.status() == VersionChecker.Status.FAILED) {
                    updateMsg = Component.translatable("chat.gameblock.update.could_not_connect", modName).withStyle(ChatFormatting.DARK_PURPLE);
                } else {
                    updateMsg = Component.translatable("chat.gameblock.update.up_to_date", modName).withStyle(ChatFormatting.GREEN);
                }
            }

            if (updateMsg != null) event.getServer().getPlayerList().broadcastSystemMessage(updateMsg, false);
            if (firstCheck && GameblockConfig.PROMOTE_DISCORD_SERVER.get()) {
                Component discordComponent = Component.translatable("chat.gameblock.discord.link").withStyle(style -> style
                        .applyFormats(ChatFormatting.BLUE, ChatFormatting.UNDERLINE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/mAnZzgGXCv")));
                event.getServer().getPlayerList().broadcastSystemMessage(Component.translatable("chat.gameblock.discord.message", modName, discordComponent)
                        .withStyle(ChatFormatting.BLUE), false);
            }
        }
    }
}
