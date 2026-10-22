package net.jennyellie.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
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
import net.minecraft.world.item.Items;
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
 * - "Sempre domesticadas por padrão" (a mecânica de domesticar por item existe mas está desativada).
 * - Clique direito abre o menu de opções SFW (seguir / parar / definir casa / ir pra casa).
 * - Seguem quem as mandou seguir, voltam pra casa quando têm uma definida.
 */
public abstract class CompanionEntity extends TamableAnimal implements GeoEntity {

    /** Mecânica de domesticar por item: desativada por ora, mantida para uso futuro. */
    public static final boolean TAMING_ITEM_ENABLED = false;

    private static final EntityDataAccessor<Boolean> DATA_FOLLOWING =
            SynchedEntityData.defineId(CompanionEntity.class, EntityDataSerializers.BOOLEAN);

    @Nullable
    private BlockPos homePos;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected CompanionEntity(EntityType<? extends TamableAnimal> type, Level level) {
        super(type, level);
    }

    /** Prefixo de animação: "jenny" ou "ellie". */
    protected abstract String animBase();

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
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
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

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        // Nascem "já domesticadas" e não desaparecem.
        this.setTame(true);
        this.setPersistenceRequired();
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
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
    }

    public void commandStopFollow() {
        this.setFollowing(false);
    }

    public void commandSetHome() {
        this.homePos = this.blockPosition();
    }

    public void commandGoHome() {
        this.setFollowing(false);
        if (this.homePos == null) {
            this.homePos = this.blockPosition();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // Domesticar por item — desativado por ora (mantido para uso futuro).
        if (TAMING_ITEM_ENABLED && !this.isTame() && stack.is(Items.DIAMOND)) {
            return tameWithItem(player, stack);
        }

        // Abre o menu de opções SFW (apenas no cliente).
        if (this.level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                    () -> () -> net.jennyellie.client.ClientHooks.openCompanionMenu(this));
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    /** Mantido para uso futuro; atualmente nunca chamado (ver TAMING_ITEM_ENABLED). */
    private InteractionResult tameWithItem(Player player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        if (!this.level().isClientSide) {
            if (this.random.nextInt(3) == 0) {
                this.tame(player);
                this.level().broadcastEntityEvent(this, (byte) 7);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
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
        controllers.add(new AnimationController<>(this, "main", 5, this::mainPredicate));
    }

    private PlayState mainPredicate(AnimationState<CompanionEntity> state) {
        String base = "animation." + animBase() + ".";
        if (state.isMoving()) {
            state.setAnimation(RawAnimation.begin().thenLoop(base + "walk"));
            return PlayState.CONTINUE;
        }
        state.setAnimation(RawAnimation.begin().thenLoop(base + "idle"));
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    // ---------------- Goals auxiliares ----------------
    /** Seguir o dono somente quando o jogador mandou seguir. */
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

    /** Voltar pra casa quando não está seguindo e tem uma casa definida. */
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
            if (this.companion.isFollowing() || !this.companion.hasHome()) {
                return false;
            }
            return this.companion.blockPosition().distSqr(this.companion.getHomePos()) > this.startDistSqr;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.companion.isFollowing()
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
