package net.jennyellie.client.renderer;

import net.jennyellie.client.model.JennyModel;
import net.jennyellie.entity.JennyEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class JennyRenderer extends CompanionRenderer<JennyEntity> {
    public JennyRenderer(EntityRendererProvider.Context context) {
        super(context, new JennyModel());
    }
}
