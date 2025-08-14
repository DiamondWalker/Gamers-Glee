package gameblock.game.os;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockGames;
import net.minecraft.network.FriendlyByteBuf;

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
    public void handleGameUpdate(GameblockOS game) {
        for (OSIcon icon : game.gameIcons) {
            if (icon.index == index) {
                icon.clickAction.run();
            }
        }
    }
}
