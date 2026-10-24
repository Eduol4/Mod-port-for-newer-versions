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
 * Controla a ARMADURA como o mod original:
 *  - Peças "chapadas" (capacete, peitoral, ombros, quadril, calças, botas) têm variantes de osso
 *    por material (base = diamante, "__g" ouro, "__i" ferro/malha, "__l" couro) com UVs deslocadas.
 *  - As peças que CONTORNAM seios/glúteos são os ossos armorBoobs/armorBootyL/R (seus filhos
 *    braLside/braRside, innerCheek…, formam a casca no formato do corpo). Elas usam a faixa de
 *    diamante (como no original) e aparecem quando a peça correspondente está equipada.
 *  - Ao equipar peitoral/calça, escondemos a "carne" nua (seios/glúteos nus) para a armadura cobrir,
 *    mantendo o volume (a casca da armadura preserva o formato).
 */
public abstract class CompanionGeoModel<T extends CompanionEntity> extends GeoModel<T> {

    private static final String[] ALWAYS_HIDDEN = {"steve", "items", "cum", "vagina"};

    // Peças com variante por material (inclui as cascas de contorno dos seios/glúteos).
    private static final String[] HEAD_VAR = {"armorHelmet"};
    private static final String[] CHEST_VAR = {"armorChest", "armorShoulderL", "armorShoulderR", "armorBoobs"};
    private static final String[] LEGS_VAR = {
            "armorHip", "armorPantsUpL", "armorPantsUpR", "armorPantsLowL", "armorPantsLowR",
            "armorBootyL", "armorBootyR"};
    private static final String[] FEET_VAR = {"armorShoesL", "armorShoesR"};

    // "Carne" nua escondida quando a peça correspondente está equipada.
    private static final String[] CHEST_HIDE = {"boobsFlesh", "upperBodyL", "upperBodyR"};
    private static final String[] LEGS_HIDE = {"fleshL", "fleshR", "curvesL", "curvesR", "kneeL", "kneeR"};

    private static final String[] VARIANTS = {"", "__g", "__i", "__l"};

    @Override
    public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        applySlot(animatable, EquipmentSlot.HEAD, HEAD_VAR, null);
        applySlot(animatable, EquipmentSlot.CHEST, CHEST_VAR, CHEST_HIDE);
        applySlot(animatable, EquipmentSlot.LEGS, LEGS_VAR, LEGS_HIDE);
        applySlot(animatable, EquipmentSlot.FEET, FEET_VAR, null);

        for (String name : ALWAYS_HIDDEN) {
            setVisible(name, false);
        }
    }

    private void applySlot(T entity, EquipmentSlot slot, String[] varBones, String[] hideBones) {
        String visibleSuffix = variantForItem(entity.getItemBySlot(slot)); // null = vazio/não suportado
        boolean equipped = visibleSuffix != null;

        for (String base : varBones) {
            for (String suffix : VARIANTS) {
                setVisible(base + suffix, equipped && suffix.equals(visibleSuffix));
            }
        }
        if (hideBones != null) {
            for (String name : hideBones) {
                setVisible(name, !equipped); // esconde a carne quando a armadura cobre
            }
        }
    }

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
