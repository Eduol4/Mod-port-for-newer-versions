package net.jennyellie.entity;

import net.jennyellie.Config;
import net.jennyellie.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class EllieEntity extends CompanionEntity {

    public EllieEntity(EntityType<? extends EllieEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return CompanionEntity.createBaseAttributes();
    }

    @Override
    protected String animBase() {
        return "ellie";
    }

    @Override
    protected double cfgMaxHealth() {
        return Config.ELLIE_MAX_HEALTH.get();
    }

    @Override
    protected double cfgAttackDamage() {
        return Config.ELLIE_ATTACK_DAMAGE.get();
    }

    @Override
    protected double cfgMovementSpeed() {
        return Config.ELLIE_MOVEMENT_SPEED.get();
    }

    @Override
    protected double cfgFollowRange() {
        return Config.ELLIE_FOLLOW_RANGE.get();
    }

    @Override
    protected boolean canCrouch() {
        return true;
    }

    @Override
    protected SoundEvent soundFollow() {
        return ModSounds.ELLIE_GIGGLE.get();
    }

    @Override
    protected SoundEvent soundStop() {
        return ModSounds.ELLIE_SADOH.get();
    }

    @Override
    protected SoundEvent soundSetHome() {
        return ModSounds.ELLIE_HAPPYOH.get();
    }

    @Override
    protected SoundEvent soundGoHome() {
        return ModSounds.ELLIE_SIGH.get();
    }
}
