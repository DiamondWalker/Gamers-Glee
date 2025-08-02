package gameblock.registry;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;

public class GameblockRenderTypes {
    public static final RenderType OS_RENDER_TYPE = RenderType.create(
            "main_menu",
            DefaultVertexFormat.POSITION_COLOR_TEX,
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameblockShaders::getShaderOS))
                    .setTextureState(RenderStateShard.MultiTextureStateShard.builder()
                            //.add(Everend.tl("gui/nebula.png").fullLocation(), false, false)
                            .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                            //.add(Neverend.tl("gui/menu_island.png").fullLocation(), false, false)
                            .build())
                    .createCompositeState(false));
}
