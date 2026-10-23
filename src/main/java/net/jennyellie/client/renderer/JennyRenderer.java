package net.jennyellie.client.renderer;

import net.jennyellie.client.model.JennyModel;
import net.jennyellie.entity.JennyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class JennyRenderer extends GeoEntityRenderer<JennyEntity> {
    public JennyRenderer(EntityRendererProvider.Context context) {
        super(context, new JennyModel());
        this.shadowRadius = 0.5f;
    }
}
