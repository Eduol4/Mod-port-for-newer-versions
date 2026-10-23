package net.jennyellie.entity;

import java.util.HashMap;
import java.util.Map;
import net.jennyellie.Config;
import net.jennyellie.registry.ModSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
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
        TAME_CHANCES.put(Items.TORCHFLOWER, 1.00);
    }

    public JennyEntity(EntityType<? extends JennyEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected double getTameChance(ItemStack stack) {
        return TAME_CHANCES.getOrDefault(stack.getItem(), 0.0);
    }

    /**
     * Balde vazio: só funciona se já estiver domesticada (NÃO domestica a Jenny).
     * Vira balde de leite, solta corações, toca um som, e dá Regeneração II + Velocidade I
     * por 30s (reinicia a cada balde).
     */
    @Override
    protected InteractionResult trySpecialInteract(Player player, InteractionHand hand, ItemStack stack) {
        if (!stack.is(Items.BUCKET) || !this.isTame()) {
            return null;
        }
        if (!this.level().isClientSide) {
            ItemStack milk = ItemUtils.createFilledResult(stack, player, Items.MILK_BUCKET.getDefaultInstance());
            player.setItemInHand(hand, milk);
            this.level().broadcastEntityEvent(this, (byte) 7); // corações
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    ModSounds.JENNY_GIGGLE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1));     // Regeneração II
            this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 0));   // Velocidade I
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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

    @Override
    protected SoundEvent soundTameSuccess() {
        return ModSounds.JENNY_HAPPYOH.get();
    }

    @Override
    protected SoundEvent soundSad() {
        return ModSounds.JENNY_SADOH.get();
    }

    @Override
    protected int breedCooldownTicks() {
        return 1200; // 1 min
    }
}
