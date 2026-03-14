package de.weinschenk.suflux.blockentity;

import de.weinschenk.suflux.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestGeneratorBlockEntity extends BlockEntity {

    private static final int GENERATION_PER_TICK = 1_000;
    private static final int BUFFER = 1_000_000;

    private int storedEnergy = 0;

    private final IEnergyStorage storage = new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int extracted = Math.min(maxExtract, storedEnergy);
            if (!simulate) storedEnergy -= extracted;
            return extracted;
        }

        @Override
        public int getEnergyStored() { return storedEnergy; }

        @Override
        public int getMaxEnergyStored() { return BUFFER; }

        @Override
        public boolean canExtract() { return storedEnergy > 0; }

        @Override
        public boolean canReceive() { return false; }
    };

    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> storage);

    public TestGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TEST_GENERATOR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, TestGeneratorBlockEntity be) {
        if (level.isClientSide) return;
        be.generate();
        be.distribute(level, pos);
    }

    private void generate() {
        storedEnergy = Math.min(storedEnergy + GENERATION_PER_TICK, BUFFER);
    }

    private void distribute(Level level, BlockPos pos) {
        if (storedEnergy == 0) return;

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(sink -> {
                if (!sink.canReceive()) return;
                int toSend = storage.extractEnergy(Integer.MAX_VALUE, true);
                if (toSend <= 0) return;
                int accepted = sink.receiveEnergy(toSend, false);
                storage.extractEnergy(accepted, false);
            });
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ENERGY) return energyHandler.cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        energyHandler.invalidate();
    }
}
