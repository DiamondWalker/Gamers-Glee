package gameblock.game.paddles.packets;

import gameblock.game.paddles.PaddlesGame;
import gameblock.packet.UpdateGamePacket;
import gameblock.registry.GameblockSounds;
import gameblock.util.physics.Direction1D;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.RegistryObject;

public class PaddleGameRoundEndPacket extends UpdateGamePacket<PaddlesGame> {
    public Direction1D winningSide;
    public boolean lastRound;

    public PaddleGameRoundEndPacket(Direction1D dir, boolean lastRound) {
        winningSide = dir;
        this.lastRound = lastRound;
    }

    public PaddleGameRoundEndPacket(FriendlyByteBuf buffer) {
        super(buffer);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeEnum(winningSide);
        buffer.writeBoolean(lastRound);
    }

    @Override
    public void readFromBuffer(FriendlyByteBuf buffer) {
        winningSide = buffer.readEnum(Direction1D.class);
        lastRound = buffer.readBoolean();
    }

    @Override
    public void gameUpdateReceivedOnClient(PaddlesGame game) {
        game.scoreTimer.start();
        game.winSide = winningSide;
        RegistryObject<SoundEvent> sound = lastRound ? GameblockSounds.PADDLES_WIN : GameblockSounds.PADDLES_SCORE;
        game.playSound(sound.get(), 1.0f, 0.5f);
    }
}
