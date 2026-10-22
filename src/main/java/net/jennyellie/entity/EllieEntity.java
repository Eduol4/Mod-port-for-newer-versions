package net.jennyellie.entity;

import java.util.HashMap;
import java.util.Map;
import net.jennyellie.Config;
import net.jennyellie.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class EllieEntity extends CompanionEntity {

    private static final Map<Item, Double> TAME_CHANCES = new HashMap<>();
    static {
        TAME_CHANCES.put(Items.LILAC, 0.10);
        TAME_CHANCES.put(Items.ROSE_BUSH, 0.12);
        TAME_CHANCES.put(Items.PEONY, 0.10);
        TAME_CHANCES.put(Items.SUNFLOWER, 0.10);
        TAME_CHANCES.put(Items.HONEY_BOTTLE, 0.15);
        TAME_CHANCES.put(Items.HONEYCOMB, 0.15);
        TAME_CHANCES.put(Items.ENDER_PEARL, 0.22);
        TAME_CHANCES.put(Items.ENDER_EYE, 0.30);
        TAME_CHANCES.put(Items.PITCHER_PLANT, 1.00);
    }

    public EllieEntity(EntityType<? extends EllieEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected double getTameChance(ItemStack stack) {
        return TAME_CHANCES.getOrDefault(stack.getItem(), 0.0);
    }

    /**
     * Balde vazio: domestica na hora (se ainda não for), vira balde de leite, solta corações
     * e toca um som. Funciona mesmo já domesticada (e aí NÃO abre o menu).
     */
    @Override
    protected InteractionResult trySpecialInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.is(Items.BUCKET)) {
            return null;
        }
        if (!this.level().isClientSide) {
            if (!this.isTame()) {
                applyTamed(player);
            }
            ItemStack milk = ItemUtils.createFilledResult(stack, player, Items.MILK_BUCKET.getDefaultInstance());
            player.setItemInHand(hand, milk);
            this.level().broadcastEntityEvent(this, (byte) 7); // corações
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.ELLIE_GIGGLE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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
    protected double cfgRunSpeed() {
        return Config.ELLIE_RUN_SPEED.get();
    }

    @Override
    protected double cfgFollowRange() {
        return Config.ELLIE_FOLLOW_RANGE.get();
    }

    @Override
    protected boolean canCrouch() {
        return true;
    }

    // Ellie: som em "parar de seguir", "definir casa" e "ir pra casa".
    @Override
    protected SoundEvent soundFollow() {
        return null;
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

    @Override
    protected SoundEvent soundTameSuccess() {
        return ModSounds.ELLIE_HAPPYOH.get();
    }
}
