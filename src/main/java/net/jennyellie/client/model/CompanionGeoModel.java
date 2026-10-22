package net.jennyellie.client.model;

import net.jennyellie.entity.CompanionEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modelo-base das companheiras. Esconde por padrão os ossos que o renderizador
 * original do mod controlava manualmente e que não usamos (ainda):
 *  - "steve": o modelo de boneco masculino usado nas cenas NSFW removidas;
 *  - "items": itens/visual de equipamento (ex.: diamantes) — some até implementarmos equipamento;
 *  - "armor*": a armadura cosmética (aparecia sempre como "armadura de diamante");
 *  - "cum"/"vagina": peças anatômicas NSFW.
 * ("cock"/"boyCam"/"diamond1-3" ficam sob "steve"/"items", então já somem junto.)
 */
public abstract class CompanionGeoModel<T extends CompanionEntity> extends GeoModel<T> {

    private static final String[] HIDDEN_BONES = {
            "steve", "items",
            "armorHelmet", "armorChest", "armorBoobs", "armorShoulderL", "armorShoulderR",
            "armorHip", "armorBootyL", "armorBootyR", "armorPantsUpL", "armorPantsUpR",
            "armorPantsLowL", "armorPantsLowR", "armorShoesL", "armorShoesR",
            "cum", "vagina"
    };

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);
        for (String name : HIDDEN_BONES) {
            CoreGeoBone bone = getAnimationProcessor().getBone(name);
            if (bone != null) {
                bone.setHidden(true);
                bone.setChildrenHidden(true);
            }
        }
    }
}
