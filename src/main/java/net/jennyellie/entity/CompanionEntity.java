package net.jennyellie.entity;

import java.util.EnumSet;
import java.util.UUID;
import net.jennyellie.JennyEllieMod;
import net.jennyellie.network.CompanionActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Base das companheiras (Jenny/Ellie).
 */
public abstract class CompanionEntity extends TamableAnimal implements GeoEntity {

    private static final EntityDataAccessor<Boolean> DATA_FOLLOWING =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RUNNING =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_CROUCHING =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos homePos;

    @Nullable
    private UUID awaitPlayerId;
    private int awaitUntilTick;

    private boolean appliedConfig = false;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected CompanionEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
        super.setCustomName(getFixedName());
        this.setCustomNameVisible(true);
    }

    protected abstract String animBase();

    protected abstract double cfgMaxHealth();
    protected abstract double cfgAttackDamage();
    protected abstract double cfgMovementSpeed();
    protected abstract double cfgRunSpeed();
    protected abstract double cfgFollowRange();

    protected abstract SoundEvent soundFollow();
    protected abstract SoundEvent soundStop();
    protected abstract SoundEvent soundSetHome();
    protected abstract SoundEvent soundGoHome();

    /** Chance de domesticação (0..1) para o item na mão; 0 = não é item de domesticação. */
    protected abstract double getTameChance(ItemStack stack);

    /** Som ao domesticar com sucesso (pode ser null). */
    protected abstract SoundEvent soundTameSuccess();

    /**
     * Interação especial que roda ANTES da lógica normal (domesticar / menu), valendo
     * domesticada ou não. Retorna um resultado para consumir o clique, ou null para seguir
     * com a lógica normal. Usado, por ex., pelo balde da Ellie.
     */
    protected InteractionResult trySpecialInteract(Player player, InteractionHand hand, ItemStack stack) {
        return null;
    }

    /** Só a Ellie tem animações de agachar. */
    protected boolean canCrouch() {
        return false;
    }

    private Component getFixedName() {
        return Component.translatable("entity." + JennyEllieMod.MODID + "." + animBase());
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(getFixedName());
    }

    public static AttributeSupplier.Builder createBaseAttributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.FOLLOW_RANGE, 24.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FOLLOWING, false);
        this.entityData.define(DATA_RUNNING, false);
        this.entityData.define(DATA_CROUCHING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new AwaitCommandGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true));
        this.goalSelector.addGoal(3, new CompanionFollowOwnerGoal(this, 1.15D, 6.0F, 2.0F));
        this.goalSelector.addGoal(4, new ReturnHomeGoal(this, 1.0D, 10.0D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    private void applyConfigAttributes() {
        setBase(Attributes.MAX_HEALTH, cfgMaxHealth());
        setBase(Attributes.ATTACK_DAMAGE, cfgAttackDamage());
        setBase(Attributes.MOVEMENT_SPEED, cfgMovementSpeed());
        setBase(Attributes.FOLLOW_RANGE, cfgFollowRange());
    }

    private void setBase(Attribute attribute, double value) {
        AttributeInstance inst = this.getAttribute(attribute);
        if (inst != null) {
            inst.setBaseValue(value);
        }
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        // Nascem NÃO domesticadas; o jogador precisa domesticá-las.
        this.setPersistenceRequired();
        applyConfigAttributes();
        this.setHealth(this.getMaxHealth());
        this.appliedConfig = true;
        super.setCustomName(getFixedName());
        this.setCustomNameVisible(true);
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (!this.appliedConfig) {
                applyConfigAttributes();
                this.setPersistenceRequired(); // nunca desaparecem (vale também para /summon)
                this.appliedConfig = true;
            }
            updateMovementStates();
        }
    }

    /** Atualiza os estados sincronizados usados pelas animações. */
    private void updateMovementStates() {
        boolean chasing = this.getTarget() != null;
        boolean ownerSprinting = this.isFollowing()
                && this.getOwner() instanceof Player p && p.isSprinting();
        boolean running = chasing || ownerSprinting;
        if (this.entityData.get(DATA_RUNNING) != running) {
            this.entityData.set(DATA_RUNNING, running);
        }

        // Velocidade diferente andando vs correndo.
        double desiredSpeed = running ? cfgRunSpeed() : cfgMovementSpeed();
        AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && speed.getBaseValue() != desiredSpeed) {
            speed.setBaseValue(desiredSpeed);
        }

        boolean crouch = this.canCrouch() && isUnderLowCeiling();
        if (this.entityData.get(DATA_CROUCHING) != crouch) {
            this.entityData.set(DATA_CROUCHING, crouch);
        }
    }

    /** Verdadeiro quando há um bloco logo acima da hitbox (teto de 2 blocos de altura). */
    private boolean isUnderLowCeiling() {
        BlockPos above = this.blockPosition().above(2);
        return !this.level().getBlockState(above).getCollisionShape(this.level(), above).isEmpty();
    }

    public boolean isRunning() {
        return this.entityData.get(DATA_RUNNING);
    }

    public boolean isCrouchingAnim() {
        return this.entityData.get(DATA_CROUCHING);
    }

    // ---------------- Estado de companheira ----------------
    public boolean isFollowing() {
        return this.entityData.get(DATA_FOLLOWING);
    }

    public void setFollowing(boolean value) {
        this.entityData.set(DATA_FOLLOWING, value);
    }

    @Nullable
    public BlockPos getHomePos() {
        return this.homePos;
    }

    public boolean hasHome() {
        return this.homePos != null;
    }

    public void commandFollow(Player player) {
        this.setTame(true);
        this.setOwnerUUID(player.getUUID());
        this.setFollowing(true);
        this.setOrderedToSit(false);
        clearAwait();
    }

    public void commandStopFollow() {
        this.setFollowing(false);
        clearAwait();
    }

    public void commandSetHome() {
        this.homePos = this.blockPosition();
        clearAwait();
    }

    public void commandGoHome() {
        this.setFollowing(false);
        if (this.homePos == null) {
            this.homePos = this.blockPosition();
        }
        clearAwait();
    }

    /** Mensagem no chat + som, após uma ação do menu. */
    public void onCommandFeedback(ServerPlayer player, CompanionActionPacket.Action action) {
        String key = switch (action) {
            case FOLLOW -> "follow";
            case STOP -> "stop";
            case SET_HOME -> "sethome";
            case GO_HOME -> "gohome";
        };
        Component msg = Component.translatable(
                "message." + JennyEllieMod.MODID + "." + animBase() + "." + key, player.getDisplayName());
        player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), msg));

        SoundEvent sound = switch (action) {
            case FOLLOW -> soundFollow();
            case STOP -> soundStop();
            case SET_HOME -> soundSetHome();
            case GO_HOME -> soundGoHome();
        };
        if (sound != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    sound, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    // ---------------- "Aguardando comando" ----------------
    public boolean isAwaiting() {
        return this.awaitPlayerId != null && this.tickCount < this.awaitUntilTick && getAwaitPlayer() != null;
    }

    @Nullable
    public Player getAwaitPlayer() {
        return this.awaitPlayerId == null ? null : this.level().getPlayerByUUID(this.awaitPlayerId);
    }

    private void clearAwait() {
        this.awaitPlayerId = null;
        this.awaitUntilTick = 0;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Interação especial (ex.: balde da Ellie) tem prioridade.
        InteractionResult special = trySpecialInteract(player, hand, stack);
        if (special != null) {
            return special;
        }

        if (!this.isTame()) {
            double chance = getTameChance(stack);
            if (chance > 0.0) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                if (!this.level().isClientSide) {
                    if (this.random.nextDouble() < chance) {
                        tameSuccess(player);
                    } else {
                        this.level().broadcastEntityEvent(this, (byte) 6); // fumaça
                    }
                }
                return InteractionResult.sidedSuccess(this.level().isClientSide);
            }
            // Não domesticada e sem item de domesticação: nada acontece.
            return InteractionResult.PASS;
        }

        // Domesticada: para, encara o jogador e abre o menu.
        if (!this.level().isClientSide) {
            if (this.getTarget() == null) {
                this.awaitPlayerId = player.getUUID();
                this.awaitUntilTick = this.tickCount + 100;
                this.getNavigation().stop();
                this.getLookControl().setLookAt(player, 30.0F, 30.0F);
            }
        } else {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.jennyellie.client.ClientHooks.openCompanionMenu(this));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** Domestica com dono = player, corações e som de sucesso. Chamar no lado servidor. */
    protected void tameSuccess(Player player) {
        this.tame(player);
        this.setFollowing(false);
        this.level().broadcastEntityEvent(this, (byte) 7); // corações
        SoundEvent s = soundTameSuccess();
        if (s != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    s, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean result = super.doHurtTarget(target);
        if (result && !this.level().isClientSide) {
            this.triggerAnim("attack", "attack" + this.random.nextInt(3));
        }
        return result;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Following", this.isFollowing());
        if (this.homePos != null) {
            tag.putInt("HomeX", this.homePos.getX());
            tag.putInt("HomeY", this.homePos.getY());
            tag.putInt("HomeZ", this.homePos.getZ());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFollowing(tag.getBoolean("Following"));
        if (tag.contains("HomeX")) {
            this.homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
        super.setCustomName(getFixedName());
        this.setCustomNameVisible(true);
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    // ---------------- GeckoLib ----------------
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String b = "animation." + animBase() + ".";
        controllers.add(new AnimationController<>(this, "main", 5, this::mainPredicate));
        controllers.add(new AnimationController<>(this, "attack", 0, state -> PlayState.STOP)
                .triggerableAnim("attack0", RawAnimation.begin().thenPlay(b + "attack0"))
                .triggerableAnim("attack1", RawAnimation.begin().thenPlay(b + "attack1"))
                .triggerableAnim("attack2", RawAnimation.begin().thenPlay(b + "attack2")));
    }

    private PlayState mainPredicate(AnimationState<CompanionEntity> state) {
        String b = "animation." + animBase() + ".";
        boolean moving = state.isMoving();

        if (isCrouchingAnim()) {
            state.setAnimation(RawAnimation.begin().thenLoop(b + (moving ? "crouchwalk" : "crouchidle")));
            return PlayState.CONTINUE;
        }
        if (moving) {
            state.setAnimation(RawAnimation.begin().thenLoop(b + (isRunning() ? "run" : "walk")));
            return PlayState.CONTINUE;
        }
        state.setAnimation(RawAnimation.begin().thenLoop(b + "idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ---------------- Goals auxiliares ----------------
    static class AwaitCommandGoal extends Goal {
        private final CompanionEntity companion;

        AwaitCommandGoal(CompanionEntity companion) {
            this.companion = companion;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.companion.isAwaiting() && this.companion.getTarget() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            this.companion.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.companion.getNavigation().stop();
            Player p = this.companion.getAwaitPlayer();
            if (p != null) {
                this.companion.getLookControl().setLookAt(p, 30.0F, 30.0F);
            }
        }
    }

    static class CompanionFollowOwnerGoal extends FollowOwnerGoal {
        private final CompanionEntity companion;

        CompanionFollowOwnerGoal(CompanionEntity companion, double speed, float startDist, float stopDist) {
            super(companion, speed, startDist, stopDist, false);
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            return this.companion.isFollowing() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return this.companion.isFollowing() && super.canContinueToUse();
        }
    }

    static class ReturnHomeGoal extends Goal {
        private final CompanionEntity companion;
        private final double speed;
        private final double startDistSqr;

        ReturnHomeGoal(CompanionEntity companion, double speed, double startDist) {
            this.companion = companion;
            this.speed = speed;
            this.startDistSqr = startDist * startDist;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (this.companion.isFollowing() || !this.companion.hasHome() || this.companion.isAwaiting()) {
                return false;
            }
            return this.companion.blockPosition().distSqr(this.companion.getHomePos()) > this.startDistSqr;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.companion.isFollowing()
                    && !this.companion.isAwaiting()
                    && this.companion.hasHome()
                    && !this.companion.getNavigation().isDone()
                    && this.companion.blockPosition().distSqr(this.companion.getHomePos()) > 4.0D;
        }

        @Override
        public void start() {
            BlockPos h = this.companion.getHomePos();
            this.companion.getNavigation().moveTo(h.getX() + 0.5D, h.getY(), h.getZ() + 0.5D, this.speed);
        }

        @Override
        public void stop() {
            this.companion.getNavigation().stop();
        }
    }
}
