package net.jennyellie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderizador-base das companheiras. Durante a reprodução com um aldeão, abaixa
 * levemente o modelo (reproduzindo o "agachar" do sneak do mod original, que apenas
 * desloca o modelo ~0.075 bloco para baixo mantendo a idle).
 */
public abstract class CompanionRenderer<T extends CompanionEntity> extends GeoEntityRenderer<T> {

    private static final double BREEDING_CROUCH = 0.075D;

    protected CompanionRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isBreedingPose()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -BREEDING_CROUCH, 0.0D);
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
