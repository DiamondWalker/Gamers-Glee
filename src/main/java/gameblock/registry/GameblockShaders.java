package gameblock.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import gameblock.GameblockMod;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = GameblockMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class GameblockShaders {
    public static ShaderInstance shaderOS;

    public static ShaderInstance getShaderOS() {
        return shaderOS;
    }

    @SubscribeEvent
    public static void registerShaders(RegisterShadersEvent event) throws IOException {
        ResourceProvider resourceProvider = event.getResourceProvider();
        event.registerShader(
                new ShaderInstance(resourceProvider, new ResourceLocation(GameblockMod.MODID, "rendertype_os"), DefaultVertexFormat.POSITION_COLOR_TEX),
                shaderInstance -> shaderOS = shaderInstance);
    }
}
