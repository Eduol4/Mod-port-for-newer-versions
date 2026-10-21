package net.jennyellie.client.model;

import net.jennyellie.JennyEllieMod;
import net.jennyellie.entity.JennyEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class JennyModel extends GeoModel<JennyEntity> {
    @Override
    public ResourceLocation getModelResource(JennyEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "geo/entity/jenny.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(JennyEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "textures/entity/jenny.png");
    }

    @Override
    public ResourceLocation getAnimationResource(JennyEntity animatable) {
        return new ResourceLocation(JennyEllieMod.MODID, "animations/jenny.animation.json");
    }
}
