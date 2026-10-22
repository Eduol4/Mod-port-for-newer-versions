package net.jennyellie.entity;

import net.jennyellie.Config;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class JennyEntity extends CompanionEntity {

    public JennyEntity(EntityType<? extends JennyEntity> type, Level level) {
        super(type, level);
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
    protected double cfgFollowRange() {
        return Config.JENNY_FOLLOW_RANGE.get();
    }
}
