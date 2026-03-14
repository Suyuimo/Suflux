package de.weinschenk.suflux.menu;

import de.weinschenk.suflux.blockentity.FluxInfuserBlockEntity;
import de.weinschenk.suflux.init.ModMenuTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;

public class FluxInfuserMenu extends AbstractContainerMenu {

    private final FluxInfuserBlockEntity blockEntity;
    private final ContainerData data;

    /** Wird vom Server aufgerufen (via BlockEntity.createMenu) */
    public FluxInfuserMenu(int id, Inventory inv, FluxInfuserBlockEntity be, ContainerData data) {
        super(ModMenuTypes.FLUX_INFUSER_MENU.get(), id);
        this.blockEntity = be;
        this.data        = data;

        addDataSlots(data);

        // Maschinen-Slots (nutzen Furnace-Textur-Positionen)
        be.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            addSlot(new SlotItemHandler(handler, FluxInfuserBlockEntity.SLOT_FLUX_CORE, 56, 17));
            addSlot(new SlotItemHandler(handler, FluxInfuserBlockEntity.SLOT_BLAZE_ROD, 56, 53));
            addSlot(new SlotItemHandler(handler, FluxInfuserBlockEntity.SLOT_OUTPUT,    116, 35));
        });

        // Spieler-Inventar
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; col++)
            addSlot(new Slot(inv, col, 8 + col * 18, 142));
    }

    /** Wird vom Client aufgerufen (via IForgeMenuType.create factory) */
    public FluxInfuserMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv,
                (FluxInfuserBlockEntity) inv.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(4));
    }

    public int getProgress()    { return data.get(0); }
    public int getMaxProgress() { return data.get(1); }
    public int getEnergy()      { return data.get(2); }
    public int getMaxEnergy()   { return data.get(3); }

    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5) < 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack    = slot.getItem();
        ItemStack original = stack.copy();

        if (index < 3) {
            // Maschinen-Slot → Spieler-Inventar
            if (!moveItemStackTo(stack, 3, this.slots.size(), true)) return ItemStack.EMPTY;
        } else {
            // Spieler-Inventar → Maschinen-Slots
            if (!moveItemStackTo(stack, 0, 3, false)) return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();

        return original;
    }
}
