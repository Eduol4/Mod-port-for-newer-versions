package net.jennyellie.entity;

import net.jennyellie.Config;
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
}
