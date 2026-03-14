package de.weinschenk.suflux.blockentity;

import de.weinschenk.suflux.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestConsumerBlockEntity extends BlockEntity {

    private long totalConsumed = 0;
    private int lastTickReceived = 0;
    private int currentTickReceived = 0;

    private final IEnergyStorage voidStorage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!simulate) {
                totalConsumed += maxReceive;
                currentTickReceived += maxReceive;
            }
            return maxReceive; // nimmt alles an, verschluckt es
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) { return 0; }

        @Override
        public int getEnergyStored() { return 0; }

        @Override
        public int getMaxEnergyStored() { return Integer.MAX_VALUE; }

        @Override
        public boolean canExtract() { return false; }

        @Override
        public boolean canReceive() { return true; }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> voidStorage);

    public TestConsumerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEST_CONSUMER_BE.get(), pos, state);
    }

    /** Wird vom FluxMeter abgefragt */
    public long getTotalConsumed() { return totalConsumed; }

    public int getLastTickReceived() { return lastTickReceived; }

    /** Muss einmal pro Tick aufgerufen werden um lastTickReceived zu aktualisieren */
    public void flushTick() {
        lastTickReceived = currentTickReceived;
        currentTickReceived = 0;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) {
            return energyHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }
}
