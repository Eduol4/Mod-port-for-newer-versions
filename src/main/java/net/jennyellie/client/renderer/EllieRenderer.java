package net.jennyellie.client.renderer;

import net.jennyellie.client.model.EllieModel;
import net.jennyellie.entity.EllieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class EllieRenderer extends GeoEntityRenderer<EllieEntity> {
    public EllieRenderer(EntityRendererProvider.Context context) {
        super(context, new EllieModel());
        this.shadowRadius = 0.5f;
    }
}
