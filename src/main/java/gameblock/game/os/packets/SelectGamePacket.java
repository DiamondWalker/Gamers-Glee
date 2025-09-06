package gameblock.game.os.packets;

import gameblock.game.os.GameblockOS;
import gameblock.game.os.OSIcon;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockGames;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SelectGamePacket extends UpdateGamePacket<GameblockOS> {
    private int index;

    public SelectGamePacket(int index) {
        this.index = index;
    }

    public SelectGamePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(index);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        index = buffer.readInt();
    }

    @Override
    public void gameUpdateReceivedOnServer(GameblockOS game, ServerPlayer sender) {
        for (OSIcon icon : game.getIcons()) {
            if (icon.index == index) {
                icon.clickAction.run();
            }
        }
    }
}
