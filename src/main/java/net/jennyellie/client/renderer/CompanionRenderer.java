package net.jennyellie.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Renderizador-base das companheiras. Ao sentar numa CAMA, abaixa um pouco o modelo para
 * encostar na cama (sem isso ela "flutua"). O barco não é afetado — lá a pose já fica correta.
 */
public abstract class CompanionRenderer<T extends CompanionEntity> extends GeoEntityRenderer<T> {

    private static final double BED_SIT_LOWER = 0.4D;

    protected CompanionRenderer(EntityRendererProvider.Context context, GeoModel<T> model) {
        super(context, model);
        this.shadowRadius = 0.5f;
        this.addRenderLayer(new CompanionWeaponLayer<>(this)); // arma na mão em combate
    }

    @Override
    public void render(T entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        if (entity.isSittingOnBed()) {
            poseStack.pushPose();
            poseStack.translate(0.0D, -BED_SIT_LOWER, 0.0D);
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
            poseStack.popPose();
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        }
    }
}
