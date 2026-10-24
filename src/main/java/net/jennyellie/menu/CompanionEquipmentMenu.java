package net.jennyellie.menu;

import net.jennyellie.entity.CompanionEntity;
import net.jennyellie.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Menu de equipamento: 4 slots de armadura + 1 de arma (ligados ao equipamento da entidade)
 * e o inventário do jogador. Validação restringe a itens vanilla (sem netherite).
 */
public class CompanionEquipmentMenu extends AbstractContainerMenu {

    private static final int ENTITY_SLOTS = 5;
    private static final int INV_START = ENTITY_SLOTS;        // 5
    private static final int INV_END = ENTITY_SLOTS + 27;     // 32
    private static final int HOTBAR_END = ENTITY_SLOTS + 36;  // 41

    private final Container equip;
    private final CompanionEntity entity;

    public static CompanionEquipmentMenu fromNetwork(int windowId, Inventory inv, FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        Entity e = inv.player.level().getEntity(id);
        CompanionEntity companion = (e instanceof CompanionEntity c) ? c : null;
        return new CompanionEquipmentMenu(windowId, inv, companion);
    }

    public CompanionEquipmentMenu(int windowId, Inventory inv, CompanionEntity entity) {
        super(ModMenus.EQUIPMENT.get(), windowId);
        this.entity = entity;
        this.equip = new CompanionEquipmentContainer(entity);

        // Slots da entidade: armadura (coluna) + arma.
        this.addSlot(new ArmorSlot(this.equip, 0, 8, 18, EquipmentSlot.HEAD));
        this.addSlot(new ArmorSlot(this.equip, 1, 8, 36, EquipmentSlot.CHEST));
        this.addSlot(new ArmorSlot(this.equip, 2, 8, 54, EquipmentSlot.LEGS));
        this.addSlot(new ArmorSlot(this.equip, 3, 8, 72, EquipmentSlot.FEET));
        this.addSlot(new WeaponSlot(this.equip, 4, 80, 36));

        // Inventário do jogador (3 linhas), mais abaixo para não encostar na coluna de armadura.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 96 + row * 18));
            }
        }
        // Hotbar.
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inv, col, 8 + col * 18, 154));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.entity != null && this.entity.isAlive() && this.entity.isTame()
                && this.entity.distanceToSqr(player) < 64.0D;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return result;
        }
        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index < ENTITY_SLOTS) {
            // Da entidade -> inventário do jogador.
            if (!this.moveItemStackTo(stack, INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Do jogador -> slots da entidade (se válido), senão alterna inventário/hotbar.
            if (this.moveItemStackTo(stack, 0, ENTITY_SLOTS, false)) {
                // equipado
            } else if (index < INV_END) {
                if (!this.moveItemStackTo(stack, INV_END, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, INV_START, INV_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == result.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return result;
    }

    /** Slot de armadura: só aceita a peça vanilla correta para aquele lugar. */
    private static class ArmorSlot extends Slot {
        private final EquipmentSlot eqSlot;

        ArmorSlot(Container container, int index, int x, int y, EquipmentSlot eqSlot) {
            super(container, index, x, y);
            this.eqSlot = eqSlot;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return CompanionEntity.isAllowedArmor(stack, this.eqSlot);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }

    /** Slot de arma: só aceita armas/ferramentas vanilla (sem netherite). */
    private static class WeaponSlot extends Slot {
        WeaponSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return CompanionEntity.isAllowedWeapon(stack);
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
