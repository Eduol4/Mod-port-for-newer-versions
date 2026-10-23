package net.jennyellie.menu;

import net.jennyellie.entity.CompanionEntity;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * "Container" que espelha os slots de equipamento da companheira (4 armaduras + arma na mão).
 * Ler/escrever aqui reflete direto no equipamento da entidade, então atributos (defesa/dano),
 * durabilidade, sincronização com o cliente e drops funcionam pelo próprio vanilla.
 */
public class CompanionEquipmentContainer implements Container {

    /** Ordem dos slots do menu -> slot de equipamento da entidade. */
    public static final EquipmentSlot[] SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND
    };

    private final CompanionEntity entity;

    public CompanionEquipmentContainer(CompanionEntity entity) {
        this.entity = entity;
    }

    public CompanionEntity getEntity() {
        return this.entity;
    }

    @Override
    public int getContainerSize() {
        return SLOTS.length;
    }

    @Override
    public boolean isEmpty() {
        if (this.entity == null) {
            return true;
        }
        for (EquipmentSlot s : SLOTS) {
            if (!this.entity.getItemBySlot(s).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int index) {
        if (this.entity == null || index < 0 || index >= SLOTS.length) {
            return ItemStack.EMPTY;
        }
        return this.entity.getItemBySlot(SLOTS[index]);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack cur = getItem(index).copy();
        if (cur.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack taken = cur.split(count);
        setItem(index, cur.isEmpty() ? ItemStack.EMPTY : cur);
        setChanged();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack cur = getItem(index).copy();
        setItem(index, ItemStack.EMPTY);
        return cur;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (this.entity != null && index >= 0 && index < SLOTS.length) {
            this.entity.setItemSlot(SLOTS[index], stack);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 1; // armadura/arma: uma peça por slot
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entity != null && this.entity.isAlive() && this.entity.distanceToSqr(player) < 64.0D;
    }

    @Override
    public void clearContent() {
        if (this.entity == null) {
            return;
        }
        for (EquipmentSlot s : SLOTS) {
            this.entity.setItemSlot(s, ItemStack.EMPTY);
        }
    }
}
