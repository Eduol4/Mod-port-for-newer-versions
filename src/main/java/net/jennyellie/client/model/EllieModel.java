package net.jennyellie.client.model;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.EllieEntity;
import net.minecraft.resources.ResourceLocation;

public class EllieModel extends CompanionGeoModel<EllieEntity> {
    @Override
    public ResourceLocation getModelResource(EllieEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "geo/entity/ellie.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EllieEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "textures/entity/ellie.png");
    }

    @Override
    public ResourceLocation getAnimationResource(EllieEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "animations/ellie.animation.json");
    }
}
