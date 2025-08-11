package gameblock.game.defusal;

import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import net.minecraft.network.FriendlyByteBuf;

public class TimePacket extends UpdateGamePacket<DefusalGame> {
    int seconds;

    public TimePacket(int ticks) {
        seconds = ticks / 20;
    }

    public TimePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeInt(seconds);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        seconds = buffer.readInt();
    }

    @Override
    public void handleGameUpdate(DefusalGame game) {
        game.timeLeft = seconds * 20;
        game.playSound(GameblockSounds.DEFUSAL_TIMER.get());
    }
}
