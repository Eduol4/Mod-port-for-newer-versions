package net.jennyellie.client.model;

import net.jennyellie.entity.CompanionEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modelo-base das companheiras.
 *
 * Esconde os ossos que o render original do mod controlava manualmente e que não usamos
 * (steve/itens/peças NSFW), e controla a ARMADURA: cada osso de armadura foi duplicado no geo
 * em 4 variantes (diamante = base, ouro = "__g", ferro/malha = "__i", couro = "__l"), cada uma
 * com as UVs deslocadas em U para a faixa daquele material na textura. Aqui mostramos apenas a
 * variante do material equipado em cada slot (ou nenhuma, se o slot estiver vazio).
 */
public abstract class CompanionGeoModel<T extends CompanionEntity> extends GeoModel<T> {

    private static final String[] ALWAYS_HIDDEN = {
            "steve", "items", "cum", "vagina",
            "armorBoobs", "armorBootyL", "armorBootyR" // sem cubos no nosso geo
    };

    private static final String[] HEAD_BONES = {"armorHelmet"};
    private static final String[] CHEST_BONES = {"armorChest", "armorShoulderL", "armorShoulderR"};
    private static final String[] LEGS_BONES = {
            "armorHip", "armorPantsUpL", "armorPantsUpR", "armorPantsLowL", "armorPantsLowR"};
    private static final String[] FEET_BONES = {"armorShoesL", "armorShoesR"};

    /** Sufixos das variantes: diamante (base), ouro, ferro/malha, couro. */
    private static final String[] VARIANTS = {"", "__g", "__i", "__l"};

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        for (String name : ALWAYS_HIDDEN) {
            setVisible(name, false);
        }
        applySlot(animatable, EquipmentSlot.HEAD, HEAD_BONES);
        applySlot(animatable, EquipmentSlot.CHEST, CHEST_BONES);
        applySlot(animatable, EquipmentSlot.LEGS, LEGS_BONES);
        applySlot(animatable, EquipmentSlot.FEET, FEET_BONES);
    }

    private void applySlot(T entity, EquipmentSlot slot, String[] baseBones) {
        String visibleSuffix = variantForItem(entity.getItemBySlot(slot)); // null = slot vazio/não suportado
        for (String base : baseBones) {
            for (String suffix : VARIANTS) {
                setVisible(base + suffix, visibleSuffix != null && suffix.equals(visibleSuffix));
            }
        }
    }

    /** Sufixo da variante de osso para o material da peça, ou null se não for armadura suportada. */
    private static String variantForItem(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorItem armor)) {
            return null;
        }
        var mat = armor.getMaterial();
        if (mat == ArmorMaterials.DIAMOND) {
            return "";
        }
        if (mat == ArmorMaterials.GOLD) {
            return "__g";
        }
        if (mat == ArmorMaterials.IRON || mat == ArmorMaterials.CHAIN) {
            return "__i";
        }
        if (mat == ArmorMaterials.LEATHER) {
            return "__l";
        }
        return null;
    }

    private void setVisible(String boneName, boolean visible) {
        CoreGeoBone bone = getAnimationProcessor().getBone(boneName);
        if (bone != null) {
            bone.setHidden(!visible);
            bone.setChildrenHidden(!visible);
        }
    }
}
