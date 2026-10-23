package net.jennyellie.client.renderer;

import net.jennyellie.client.model.EllieModel;
import net.jennyellie.entity.EllieEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EllieRenderer extends CompanionRenderer<EllieEntity> {
    public EllieRenderer(EntityRendererProvider.Context context) {
        super(context, new EllieModel());
    }
}
