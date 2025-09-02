package gameblock.game;

import gameblock.packet.UpdateGamePacket;
import net.minecraft.network.FriendlyByteBuf;

public class SpectatorModePacket extends UpdateGamePacket<GameInstance<?>> {
    boolean spectatorMode;

    public SpectatorModePacket(boolean spectator) {
        this.spectatorMode = spectator;
    }

    public SpectatorModePacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBoolean(spectatorMode);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        spectatorMode = buffer.readBoolean();
    }

    @Override
    public void gameUpdateReceivedOnClient(GameInstance<?> game) {
        game.setSpectatorMode(spectatorMode);
    }
}
