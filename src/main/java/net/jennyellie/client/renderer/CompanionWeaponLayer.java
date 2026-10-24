package net.jennyellie.client.renderer;

import net.jennyellie.entity.CompanionEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

/**
 * Mostra a arma equipada no osso "weapon" (mão direita), mas apenas quando a companheira
 * está em combate (tem um alvo). A animação de golpear não muda.
 */
public class CompanionWeaponLayer<T extends CompanionEntity> extends BlockAndItemGeoLayer<T> {

    public CompanionWeaponLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    protected ItemStack getStackForBone(GeoBone bone, T animatable) {
        if (!"weapon".equals(bone.getName())) {
            return null;
        }
        if (!animatable.isInCombat()) {
            return null; // só aparece em combate (estado sincronizado com o cliente)
        }
        ItemStack weapon = animatable.getMainHandItem();
        return weapon.isEmpty() ? null : weapon;
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, T animatable) {
        return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }
}
