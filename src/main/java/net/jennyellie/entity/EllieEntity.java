package net.jennyellie.entity;

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
}
