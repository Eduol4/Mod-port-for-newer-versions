package net.jennyellie.entity;

import java.util.EnumSet;
import java.util.UUID;
import net.jennyellie.JennyEllieMod;
import net.jennyellie.network.CompanionActionPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.Path;
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
    private static final EntityDataAccessor<Boolean> DATA_BREED_ENABLED =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_BREED_COOLDOWN_SECS =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_HAS_HOME =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos homePos;

    @Nullable
    private UUID awaitPlayerId;
    private int awaitUntilTick;

    private int calmUntilTick;   // até quando (não domesticada) continua revidando
    private int regenTimer;
    private int teleportHomeTick; // > 0 = teletransporte pra casa agendado
    private boolean appliedConfig = false;

    // Reprodução com aldeão
    private long breedCooldownUntil;   // gameTime até o fim do cooldown
    protected long breedBoostUntil;    // gameTime até o fim do "boost" (ex.: maçã/cenoura dourada da Ellie)
    private boolean breedActive;
    @Nullable
    private UUID breedVillagerId;
    private int breedContactTicks;
    private boolean wasBreedReady;

    // ~4,58s de animação (throwpearl), sem atraso extra.
    private static final int TELEPORT_HOME_DELAY = 92;
    private static final double WALK_HOME_MAX_SQR = 24.0D * 24.0D;

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
    protected abstract SoundEvent soundTameSuccess();
    protected abstract SoundEvent soundSad();

    protected abstract double getTameChance(ItemStack stack);

    /** Cooldown da reprodução (em ticks) após gerar um bebê. */
    protected abstract int breedCooldownTicks();

    /** Condição extra pra habilitar o botão de reprodução (Jenny: sempre; Ellie: fraqueza/boost). */
    protected boolean isBreedConditionMet() {
        return true;
    }

    protected boolean canCrouch() {
        return false;
    }

    /** Interação especial antes da lógica normal (ex.: balde da Ellie). null = segue normal. */
    protected InteractionResult trySpecialInteract(Player player, InteractionHand hand, ItemStack stack) {
        return null;
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
        this.entityData.define(DATA_BREED_ENABLED, false);
        this.entityData.define(DATA_BREED_COOLDOWN_SECS, 0);
        this.entityData.define(DATA_HAS_HOME, false);
    }

    /** Navegação que atravessa e abre portas, como aldeões. */
    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation nav = new GroundPathNavigation(this, level);
        nav.setCanOpenDoors(true);
        nav.setCanPassDoors(true);
        return nav;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true)); // abre e fecha portas
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1D, true)); // defesa tem prioridade sobre reprodução
        this.goalSelector.addGoal(3, new BreedWithVillagerGoal(this));
        this.goalSelector.addGoal(4, new AwaitCommandGoal(this));
        this.goalSelector.addGoal(5, new CompanionFollowOwnerGoal(this, 1.15D, 6.0F, 2.0F));
        this.goalSelector.addGoal(6, new ReturnHomeGoal(this, 1.0D, 10.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        // Defesa própria: sempre (domesticada ou não).
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Defesa do dono / alvo do dono: só quando está seguindo.
        this.targetSelector.addGoal(2, new FollowingOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(3, new FollowingOwnerHurtTargetGoal(this));
    }

    /** Não ataca animais domesticados nem jogadores quando defende o dono. */
    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target instanceof TamableAnimal tamable && tamable.isTame()) {
            return false;
        }
        if (target instanceof Player) {
            return false;
        }
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Domesticada: imune a golpes de jogador.
        if (this.isTame() && source.getEntity() instanceof Player) {
            return false;
        }
        boolean result = super.hurt(source, amount);
        // Não domesticada: revida por ~8-10s a partir do último golpe.
        if (result && !this.isTame() && !this.level().isClientSide && source.getEntity() instanceof LivingEntity) {
            this.calmUntilTick = this.tickCount + 160 + this.random.nextInt(41);
        }
        return result;
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
                this.setPersistenceRequired();
                this.appliedConfig = true;
            }
            updateMovementStates();
            updateCalmDown();
            updateRegen();
            updateTeleportHome();
            updateBreedState();

            boolean home = hasHome();
            if (this.entityData.get(DATA_HAS_HOME) != home) {
                this.entityData.set(DATA_HAS_HOME, home);
            }
        }
    }

    /** Lido pela tela (cliente): existe uma casa definida? */
    public boolean isHomeSet() {
        return this.entityData.get(DATA_HAS_HOME);
    }

    // ---------------- Reprodução com aldeão ----------------
    private void updateBreedState() {
        boolean enabled = isBreedEnabledNow();
        if (this.entityData.get(DATA_BREED_ENABLED) != enabled) {
            this.entityData.set(DATA_BREED_ENABLED, enabled);
        }
        int secs = (int) Math.max(0, Math.ceil((this.breedCooldownUntil - this.level().getGameTime()) / 20.0D));
        if (this.entityData.get(DATA_BREED_COOLDOWN_SECS) != secs) {
            this.entityData.set(DATA_BREED_COOLDOWN_SECS, secs);
        }

        // Corações leves durante o processo (~1,7/s), em ambos. A rajada grande fica no nascimento.
        if (this.breedActive && this.tickCount % 12 == 0 && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.2D, this.getZ(), 1, 0.3D, 0.3D, 0.3D, 0.0D);
            net.minecraft.world.entity.npc.Villager v = getBreedVillager();
            if (v != null) {
                sl.sendParticles(ParticleTypes.HEART, v.getX(), v.getY() + 1.2D, v.getZ(), 1, 0.3D, 0.3D, 0.3D, 0.0D);
            }
        }

        // Transição para "pronta para reproduzir" (dispara a mensagem da Ellie).
        boolean ready = this.isTame() && isBreedConditionMet();
        if (ready && !this.wasBreedReady) {
            onBecameBreedReady();
        }
        this.wasBreedReady = ready;
    }

    /** Chamado quando a companheira passa a estar pronta pra reprodução. Base: nada. */
    protected void onBecameBreedReady() {
    }

    private boolean isBreedOnCooldown() {
        return this.level().getGameTime() < this.breedCooldownUntil;
    }

    /** Pode reproduzir agora? (domesticada, sem cooldown, sem estar no processo, e condição atendida). */
    public boolean isBreedEnabledNow() {
        return this.isTame() && !this.breedActive && !isBreedOnCooldown() && isBreedConditionMet();
    }

    public boolean isBreedActive() {
        return this.breedActive;
    }

    // Lidos pela tela (cliente), via dados sincronizados.
    public boolean isBreedButtonEnabled() {
        return this.entityData.get(DATA_BREED_ENABLED);
    }

    public int getBreedCooldownSecs() {
        return this.entityData.get(DATA_BREED_COOLDOWN_SECS);
    }

    @Nullable
    private net.minecraft.world.entity.npc.Villager getBreedVillager() {
        if (this.breedVillagerId == null || !(this.level() instanceof ServerLevel sl)) {
            return null;
        }
        return sl.getEntity(this.breedVillagerId) instanceof net.minecraft.world.entity.npc.Villager v ? v : null;
    }

    /** Chamado pelo botão do menu. Procura o aldeão mais próximo e inicia a aproximação. */
    public void startBreeding(ServerPlayer player) {
        if (!isBreedEnabledNow()) {
            return;
        }
        net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(16.0D);
        net.minecraft.world.entity.npc.Villager nearest = null;
        double best = Double.MAX_VALUE;
        for (net.minecraft.world.entity.npc.Villager v : this.level().getEntitiesOfClass(net.minecraft.world.entity.npc.Villager.class, box)) {
            if (v.isBaby()) {
                continue;
            }
            double d = this.distanceToSqr(v);
            if (d < best) {
                best = d;
                nearest = v;
            }
        }
        if (nearest == null) {
            // Sem aldeões por perto: som triste + mensagem.
            SoundEvent sad = soundSad();
            if (sad != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), sad, SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
            player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(),
                    Component.translatable("message." + JennyEllieMod.MODID + "." + animBase() + ".novillager")));
            return;
        }
        // Reprodução vira prioridade máxima: deixa de seguir e de "aguardar comando".
        this.setFollowing(false);
        clearAwait();
        this.breedActive = true;
        this.breedVillagerId = nearest.getUUID();
        this.breedContactTicks = 0;
    }

    private void cancelBreed() {
        net.minecraft.world.entity.npc.Villager v = getBreedVillager();
        if (v != null) {
            v.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET); // aldeão volta ao normal
        }
        this.breedActive = false;
        this.breedVillagerId = null;
        this.breedContactTicks = 0;
    }

    private void finishBreeding(net.minecraft.world.entity.npc.Villager villager) {
        if (this.level() instanceof ServerLevel sl) {
            net.minecraft.world.entity.npc.Villager baby =
                    net.minecraft.world.entity.EntityType.VILLAGER.create(sl);
            if (baby != null) {
                baby.setBaby(true);
                baby.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                sl.addFreshEntity(baby);
            }
            sl.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 1.0D, this.getZ(),
                    12, 0.4D, 0.6D, 0.4D, 0.1D);
            // XP como na reprodução de animais.
            ExperienceOrb.award(sl, this.position(), 1 + this.random.nextInt(7));
        }
        // Aldeão volta imediatamente ao que estava fazendo.
        villager.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        SoundEvent s = soundTameSuccess();
        if (s != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), s, SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        this.breedCooldownUntil = this.level().getGameTime() + breedCooldownTicks();
        this.breedBoostUntil = 0L; // desabilita o botão imediatamente (Ellie)
        cancelBreed();
    }

    private void updateTeleportHome() {
        if (this.teleportHomeTick == 0) {
            return;
        }
        if (this.isFollowing() || !this.hasHome()) {
            this.teleportHomeTick = 0; // cancela
        } else if (this.tickCount >= this.teleportHomeTick) {
            performTeleportHome();
        } else {
            this.getNavigation().stop(); // parada durante a animação
        }
    }

    public boolean isTeleportingHome() {
        return this.teleportHomeTick != 0;
    }

    /** Inicia a sequência: animação de jogar pearl + teleport agendado. */
    public void beginTeleportHome() {
        if (this.teleportHomeTick != 0) {
            return;
        }
        this.triggerAnim("action", "throwpearl");
        this.teleportHomeTick = this.tickCount + TELEPORT_HOME_DELAY;
        this.getNavigation().stop();
    }

    private void performTeleportHome() {
        BlockPos h = this.homePos;
        this.teleportHomeTick = 0;
        if (h == null) {
            return;
        }
        spawnTeleportFx(this.getX(), this.getY() + 1.0D, this.getZ());
        this.moveTo(h.getX() + 0.5D, h.getY(), h.getZ() + 0.5D, this.getYRot(), this.getXRot());
        this.getNavigation().stop();
        spawnTeleportFx(h.getX() + 0.5D, h.getY() + 1.0D, h.getZ() + 0.5D);
    }

    private void spawnTeleportFx(double x, double y, double z) {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.PORTAL, x, y, z, 24, 0.5D, 0.6D, 0.5D, 0.1D);
        }
        this.level().playSound(null, x, y, z,
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private void updateMovementStates() {
        boolean chasing = this.getTarget() != null;
        boolean ownerSprinting = this.isFollowing()
                && this.getOwner() instanceof Player p && p.isSprinting();
        boolean running = chasing || ownerSprinting;
        if (this.entityData.get(DATA_RUNNING) != running) {
            this.entityData.set(DATA_RUNNING, running);
        }

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

    /** Não domesticada "se acalma" e para de revidar passado o tempo. */
    private void updateCalmDown() {
        if (!this.isTame() && this.calmUntilTick != 0 && this.tickCount >= this.calmUntilTick) {
            this.setTarget(null);
            this.setLastHurtByMob(null);
            this.calmUntilTick = 0;
        }
    }

    private void updateRegen() {
        if (this.isAlive() && this.getHealth() < this.getMaxHealth()) {
            this.regenTimer++;
            int interval = this.isTame() ? 80 : 100;   // 4s domesticada / 5s não
            float amount = this.isTame() ? 2.0F : 1.0F;
            if (this.regenTimer >= interval) {
                this.heal(amount);
                this.regenTimer = 0;
            }
        } else {
            this.regenTimer = 0;
        }
    }

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
        this.setOwnerUUID(player.getUUID());
        this.setFollowing(true);
        this.setOrderedToSit(false);
        clearAwait();
    }

    public void commandStopFollow() {
        this.setFollowing(false);
        this.setTarget(null); // deixa de defender o dono ao parar de seguir
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

    public void onCommandFeedback(ServerPlayer player, CompanionActionPacket.Action action) {
        String key = switch (action) {
            case FOLLOW, TOGGLE_FOLLOW, BREED -> "follow";
            case STOP -> "stop";
            case SET_HOME -> "sethome";
            case GO_HOME -> "gohome";
        };
        Component msg = Component.translatable(
                "message." + JennyEllieMod.MODID + "." + animBase() + "." + key, player.getDisplayName());
        player.sendSystemMessage(Component.translatable("chat.type.text", this.getDisplayName(), msg));

        SoundEvent sound = switch (action) {
            case FOLLOW, TOGGLE_FOLLOW, BREED -> soundFollow();
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
            return InteractionResult.PASS;
        }

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

    /** Aplica o estado domesticado (dono, seguir por padrão, sem alvo). Sem efeitos visuais. */
    protected void applyTamed(Player player) {
        this.tame(player);
        this.setFollowing(true);
        this.setTarget(null);
        this.calmUntilTick = 0;
    }

    /** Domestica com corações e som de sucesso. Chamar no lado servidor. */
    protected void tameSuccess(Player player) {
        applyTamed(player);
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
        tag.putLong("BreedCooldownUntil", this.breedCooldownUntil);
        tag.putLong("BreedBoostUntil", this.breedBoostUntil);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setFollowing(tag.getBoolean("Following"));
        if (tag.contains("HomeX")) {
            this.homePos = new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ"));
        }
        this.breedCooldownUntil = tag.getLong("BreedCooldownUntil");
        this.breedBoostUntil = tag.getLong("BreedBoostUntil");
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
        controllers.add(new AnimationController<>(this, "action", 0, state -> PlayState.STOP)
                .triggerableAnim("throwpearl", RawAnimation.begin().thenPlay(b + "throwpearl")));
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
            if (this.companion.isFollowing() || !this.companion.hasHome()
                    || this.companion.isAwaiting() || this.companion.isTeleportingHome()) {
                return false;
            }
            return this.companion.blockPosition().distSqr(this.companion.getHomePos()) > this.startDistSqr;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.companion.isFollowing()
                    && !this.companion.isAwaiting()
                    && !this.companion.isTeleportingHome()
                    && this.companion.hasHome()
                    && !this.companion.getNavigation().isDone()
                    && this.companion.blockPosition().distSqr(this.companion.getHomePos()) > 4.0D;
        }

        @Override
        public void start() {
            BlockPos h = this.companion.getHomePos();
            double dist2 = this.companion.blockPosition().distSqr(h);
            Path path = this.companion.getNavigation().createPath(h, 0);
            boolean reachable = path != null && path.canReach();

            if (dist2 <= WALK_HOME_MAX_SQR && reachable) {
                this.companion.getNavigation().moveTo(path, this.speed); // anda até em casa
            } else {
                this.companion.beginTeleportHome();                      // longe/inacessível: teletransporta
            }
        }

        @Override
        public void stop() {
            this.companion.getNavigation().stop();
        }
    }

    /** Defende o dono apenas quando está seguindo. */
    static class FollowingOwnerHurtByTargetGoal extends OwnerHurtByTargetGoal {
        private final CompanionEntity companion;

        FollowingOwnerHurtByTargetGoal(CompanionEntity companion) {
            super(companion);
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            return this.companion.isFollowing() && super.canUse();
        }
    }

    /** Ataca o alvo do dono apenas quando está seguindo. */
    static class FollowingOwnerHurtTargetGoal extends OwnerHurtTargetGoal {
        private final CompanionEntity companion;

        FollowingOwnerHurtTargetGoal(CompanionEntity companion) {
            super(companion);
            this.companion = companion;
        }

        @Override
        public boolean canUse() {
            return this.companion.isFollowing() && super.canUse();
        }
    }

    /** Aproxima a companheira e o aldeão, com corações, até gerar um bebê aldeão. */
    static class BreedWithVillagerGoal extends Goal {
        private final CompanionEntity companion;

        BreedWithVillagerGoal(CompanionEntity companion) {
            this.companion = companion;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return this.companion.isBreedActive() && this.companion.getBreedVillager() != null;
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void tick() {
            net.minecraft.world.entity.npc.Villager v = this.companion.getBreedVillager();
            if (v == null || !v.isAlive()) {
                this.companion.cancelBreed();
                return;
            }
            this.companion.getLookControl().setLookAt(v, 30.0F, 30.0F);

            if (this.companion.distanceToSqr(v) > 2.5D * 2.5D) {
                this.companion.getNavigation().moveTo(v, 1.0D);
                // Força o aldeão a vir — prioridade máxima via brain, sobrepondo trabalho/fuga/etc.
                v.getBrain().setMemory(MemoryModuleType.WALK_TARGET,
                        new WalkTarget(new EntityTracker(this.companion, false), 0.6F, 1));
                v.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.companion, true));
                this.companion.breedContactTicks = 0;
            } else {
                this.companion.getNavigation().stop();
                v.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(this.companion, true));
                this.companion.breedContactTicks++;
                if (this.companion.breedContactTicks >= 60) { // ~3s em contato
                    this.companion.finishBreeding(v);
                }
            }
        }
    }
}
