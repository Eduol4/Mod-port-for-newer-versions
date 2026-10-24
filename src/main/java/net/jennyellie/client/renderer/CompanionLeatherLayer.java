package net.jennyellie.client.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.jennyellie.entity.CompanionEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * Desenha as peças de armadura de COURO numa passada à parte, multiplicando a textura (faixa de
 * couro em tons de cinza) pela cor do tingimento do item (ou marrom padrão se não tingido).
 * A passada principal esconde os ossos "__l"; aqui eles são mostrados — e só eles — com a cor.
 */
public class CompanionLeatherLayer<T extends CompanionEntity> extends GeoRenderLayer<T> {

    private static final int DEFAULT_LEATHER = 0xA06540;

    private static final Map<EquipmentSlot, String[]> SLOT_ROOTS = new HashMap<>();
    static {
        SLOT_ROOTS.put(EquipmentSlot.HEAD, new String[]{"armorHelmet"});
        SLOT_ROOTS.put(EquipmentSlot.CHEST,
                new String[]{"armorChest", "armorShoulderL", "armorShoulderR", "armorBoobs"});
        SLOT_ROOTS.put(EquipmentSlot.LEGS, new String[]{
                "armorHip", "armorPantsUpL", "armorPantsUpR", "armorPantsLowL", "armorPantsLowR",
                "armorBootyL", "armorBootyR"});
        SLOT_ROOTS.put(EquipmentSlot.FEET, new String[]{"armorShoesL", "armorShoesR"});
    }

    public CompanionLeatherLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                       int packedLight, int packedOverlay) {
        if (animatable.isArmorHidden()) {
            return; // armadura "despida" fica invisível, inclusive o couro
        }
        Map<Integer, Set<String>> byColor = new HashMap<>();
        for (Map.Entry<EquipmentSlot, String[]> e : SLOT_ROOTS.entrySet()) {
            ItemStack stack = animatable.getItemBySlot(e.getKey());
            if (!isLeather(stack)) {
                continue;
            }
            int color = (stack.getItem() instanceof DyeableLeatherItem d) ? d.getColor(stack) : DEFAULT_LEATHER;
            Set<String> targets = byColor.computeIfAbsent(color, k -> new HashSet<>());
            for (String root : e.getValue()) {
                GeoBone rb = findBone(bakedModel, root + "__l");
                if (rb != null) {
                    collectSubtree(rb, targets);
                }
            }
        }
        if (byColor.isEmpty()) {
            return;
        }

        List<GeoBone> all = new ArrayList<>();
        for (GeoBone b : bakedModel.topLevelBones()) {
            collectAll(b, all);
        }

        for (Map.Entry<Integer, Set<String>> e : byColor.entrySet()) {
            Set<String> targets = e.getValue();
            if (targets.isEmpty()) {
                continue;
            }
            Set<String> ancestors = computeAncestors(all, targets);

            boolean[] savedHidden = new boolean[all.size()];
            boolean[] savedChildren = new boolean[all.size()];
            for (int i = 0; i < all.size(); i++) {
                GeoBone b = all.get(i);
                savedHidden[i] = b.isHidden();
                savedChildren[i] = b.isHidingChildren();
                String n = b.getName();
                if (targets.contains(n)) {
                    b.setHidden(false);
                    b.setChildrenHidden(false);
                } else if (ancestors.contains(n)) {
                    b.setHidden(true);          // só transporte; sem cubos próprios
                    b.setChildrenHidden(false); // mantém o caminho até o couro
                } else {
                    b.setHidden(true);
                    b.setChildrenHidden(true);
                }
            }

            int color = e.getKey();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float bl = (color & 0xFF) / 255.0F;
            this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer,
                    partialTick, packedLight, packedOverlay, r, g, bl, 1.0F);

            for (int i = 0; i < all.size(); i++) {
                all.get(i).setHidden(savedHidden[i]);
                all.get(i).setChildrenHidden(savedChildren[i]);
            }
        }
    }

    private static boolean isLeather(ItemStack stack) {
        return stack.getItem() instanceof ArmorItem a && a.getMaterial() == ArmorMaterials.LEATHER;
    }

    private static void collectSubtree(GeoBone b, Set<String> out) {
        out.add(b.getName());
        for (GeoBone c : b.getChildBones()) {
            collectSubtree(c, out);
        }
    }

    private static void collectAll(GeoBone b, List<GeoBone> out) {
        out.add(b);
        for (GeoBone c : b.getChildBones()) {
            collectAll(c, out);
        }
    }

    private static Set<String> computeAncestors(List<GeoBone> all, Set<String> targets) {
        Set<String> anc = new HashSet<>();
        for (GeoBone b : all) {
            if (targets.contains(b.getName())) {
                GeoBone p = b.getParent();
                while (p != null) {
                    anc.add(p.getName());
                    p = p.getParent();
                }
            }
        }
        return anc;
    }

    private GeoBone findBone(BakedGeoModel model, String name) {
        for (GeoBone b : model.topLevelBones()) {
            GeoBone f = findIn(b, name);
            if (f != null) {
                return f;
            }
        }
        return null;
    }

    private GeoBone findIn(GeoBone b, String name) {
        if (b.getName().equals(name)) {
            return b;
        }
        for (GeoBone c : b.getChildBones()) {
            GeoBone f = findIn(c, name);
            if (f != null) {
                return f;
            }
        }
        return null;
    }
}
