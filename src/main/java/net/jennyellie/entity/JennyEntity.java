package net.jennyellie.entity;

import java.util.HashMap;
import java.util.Map;
import net.jennyellie.Config;
import net.jennyellie.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class JennyEntity extends CompanionEntity {

    private static final Map<Item, Double> TAME_CHANCES = new HashMap<>();
    static {
        TAME_CHANCES.put(Items.DANDELION, 0.05);
        TAME_CHANCES.put(Items.POPPY, 0.055);
        TAME_CHANCES.put(Items.BLUE_ORCHID, 0.05);
        TAME_CHANCES.put(Items.ALLIUM, 0.07);
        TAME_CHANCES.put(Items.AZURE_BLUET, 0.05);
        TAME_CHANCES.put(Items.OXEYE_DAISY, 0.06);
        TAME_CHANCES.put(Items.CORNFLOWER, 0.05);
        TAME_CHANCES.put(Items.LILY_OF_THE_VALLEY, 0.10);
        TAME_CHANCES.put(Items.RED_TULIP, 0.075);
        TAME_CHANCES.put(Items.ORANGE_TULIP, 0.05);
        TAME_CHANCES.put(Items.WHITE_TULIP, 0.075);
        TAME_CHANCES.put(Items.PINK_TULIP, 0.10);
        TAME_CHANCES.put(Items.WITHER_ROSE, 0.50);
        TAME_CHANCES.put(Items.GOLD_INGOT, 0.25);
        TAME_CHANCES.put(Items.ENDER_PEARL, 0.33);
        TAME_CHANCES.put(Items.ENDER_EYE, 0.40);
        TAME_CHANCES.put(Items.DIAMOND, 1.00);
    }

    public JennyEntity(EntityType<? extends JennyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected double getTameChance(ItemStack stack) {
        return TAME_CHANCES.getOrDefault(stack.getItem(), 0.0);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return CompanionEntity.createBaseAttributes();
    }

    @Override
    protected String animBase() {
        return "jenny";
    }

    @Override
    protected double cfgMaxHealth() {
        return Config.JENNY_MAX_HEALTH.get();
    }

    @Override
    protected double cfgAttackDamage() {
        return Config.JENNY_ATTACK_DAMAGE.get();
    }

    @Override
    protected double cfgMovementSpeed() {
        return Config.JENNY_MOVEMENT_SPEED.get();
    }

    @Override
    protected double cfgRunSpeed() {
        return Config.JENNY_RUN_SPEED.get();
    }

    @Override
    protected double cfgFollowRange() {
        return Config.JENNY_FOLLOW_RANGE.get();
    }

    // Jenny: som em "parar de seguir", "definir casa" e "ir pra casa".
    @Override
    protected SoundEvent soundFollow() {
        return null;
    }

    @Override
    protected SoundEvent soundStop() {
        return ModSounds.JENNY_SADOH.get();
    }

    @Override
    protected SoundEvent soundSetHome() {
        return ModSounds.JENNY_HAPPYOH.get();
    }

    @Override
    protected SoundEvent soundGoHome() {
        return ModSounds.JENNY_SIGH.get();
    }
}
