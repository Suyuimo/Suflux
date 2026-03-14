package de.weinschenk.suflux.blockentity;

import de.weinschenk.suflux.Suflux;
import de.weinschenk.suflux.init.ModBlockEntities;
import de.weinschenk.suflux.menu.FluxInfuserMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FluxInfuserBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_FLUX_CORE = 0;
    public static final int SLOT_BLAZE_ROD = 1;
    public static final int SLOT_OUTPUT    = 2;

    private static final int MAX_PROGRESS   = 100;
    private static final int MAX_ENERGY     = 30_000;
    private static final int ENERGY_PER_TICK = 300;

    private int progress     = 0;
    private int storedEnergy = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(3) {
        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return switch (slot) {
                case SLOT_FLUX_CORE -> stack.is(Suflux.FLUX_CORE.get());
                case SLOT_BLAZE_ROD -> stack.is(Items.BLAZE_ROD);
                case SLOT_OUTPUT    -> false;
                default             -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final IEnergyStorage energyStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = Math.min(maxReceive, MAX_ENERGY - storedEnergy);
            if (!simulate) { storedEnergy += received; setChanged(); }
            return received;
        }

        @Override public int extractEnergy(int maxExtract, boolean simulate) { return 0; }
        @Override public int getEnergyStored()    { return storedEnergy; }
        @Override public int getMaxEnergyStored() { return MAX_ENERGY; }
        @Override public boolean canExtract()     { return false; }
        @Override public boolean canReceive()     { return storedEnergy < MAX_ENERGY; }
    };

    private final LazyOptional<IItemHandler>   itemHandlerOpt   = LazyOptional.of(() -> itemHandler);
    private final LazyOptional<IEnergyStorage> energyHandlerOpt = LazyOptional.of(() -> energyStorage);

    // ContainerData: [0]=progress [1]=maxProgress [2]=energy [3]=maxEnergy
    final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> MAX_PROGRESS;
                case 2 -> storedEnergy;
                case 3 -> MAX_ENERGY;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) progress = value;
            if (index == 2) storedEnergy = value;
        }

        @Override
        public int getCount() { return 4; }
    };

    public FluxInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUX_INFUSER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluxInfuserBlockEntity be) {
        if (level.isClientSide) return;

        if (be.hasRecipe() && be.canOutput() && be.storedEnergy >= ENERGY_PER_TICK) {
            be.storedEnergy -= ENERGY_PER_TICK;
            be.progress++;
            be.setChanged();

            if (be.progress >= MAX_PROGRESS) {
                be.craft();
            }
        } else if (!be.hasRecipe() || !be.canOutput()) {
            if (be.progress != 0) {
                be.progress = 0;
                be.setChanged();
            }
        }
    }

    private boolean hasRecipe() {
        return !itemHandler.getStackInSlot(SLOT_FLUX_CORE).isEmpty()
            && !itemHandler.getStackInSlot(SLOT_BLAZE_ROD).isEmpty();
    }

    private boolean canOutput() {
        ItemStack out = itemHandler.getStackInSlot(SLOT_OUTPUT);
        return out.isEmpty()
            || (out.is(Suflux.ENERGIZED_FLUX_CORE.get()) && out.getCount() < out.getMaxStackSize());
    }

    private void craft() {
        itemHandler.extractItem(SLOT_FLUX_CORE, 1, false);
        itemHandler.extractItem(SLOT_BLAZE_ROD, 1, false);

        ItemStack out = itemHandler.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            itemHandler.setStackInSlot(SLOT_OUTPUT, new ItemStack(Suflux.ENERGIZED_FLUX_CORE.get()));
        } else {
            out.grow(1);
        }
        progress = 0;
        setChanged();
    }

    /** Drops all inventory contents into the world (called when block is broken). */
    public void drops() {
        if (level == null) return;
        SimpleContainer container = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            container.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(level, worldPosition, container);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.suflux.flux_infuser");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new FluxInfuserMenu(id, inv, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        tag.put("inventory", itemHandler.serializeNBT());
        tag.putInt("progress", progress);
        tag.putInt("energy", storedEnergy);
        super.saveAdditional(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
        progress     = tag.getInt("progress");
        storedEnergy = tag.getInt("energy");
        super.load(tag);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) return itemHandlerOpt.cast();
        if (cap == ForgeCapabilities.ENERGY)       return energyHandlerOpt.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandlerOpt.invalidate();
        energyHandlerOpt.invalidate();
    }
}
