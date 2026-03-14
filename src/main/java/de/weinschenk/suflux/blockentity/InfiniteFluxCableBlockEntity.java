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
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InfiniteFluxCableBlockEntity extends BlockEntity {

    // Integer.MAX_VALUE als Puffer – praktisch unbegrenzt
    private static final int INFINITE = Integer.MAX_VALUE;

    private final EnergyStorage buffer = new EnergyStorage(INFINITE, INFINITE, INFINITE);
    private final LazyOptional<IEnergyStorage> energyHandler = LazyOptional.of(() -> buffer);

    private int transferredThisTick = 0;
    private int transferredLastTick = 0;

    public int getLastTickTransferred() { return transferredLastTick; }

    public InfiniteFluxCableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFINITE_FLUX_CABLE_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, InfiniteFluxCableBlockEntity be) {
        if (level.isClientSide) return;
        be.transferredLastTick = be.transferredThisTick;
        be.transferredThisTick = 0;
        be.pullEnergy(level, pos);
        be.pushEnergy(level, pos);
    }

    /** Energie von benachbarten Blöcken ziehen */
    private void pullEnergy(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(source -> {
                if (!source.canExtract()) return;
                int available = source.extractEnergy(INFINITE, true);
                if (available <= 0) return;
                int accepted = buffer.receiveEnergy(available, false);
                source.extractEnergy(accepted, false);
                transferredThisTick += accepted;
            });
        }
    }

    /** Energie an benachbarte Blöcke weitergeben */
    private void pushEnergy(Level level, BlockPos pos) {
        if (buffer.getEnergyStored() == 0) return;

        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null) continue;

            neighbor.getCapability(ForgeCapabilities.ENERGY, dir.getOpposite()).ifPresent(sink -> {
                if (!sink.canReceive()) return;
                int toSend = buffer.extractEnergy(INFINITE, true);
                if (toSend <= 0) return;
                int accepted = sink.receiveEnergy(toSend, false);
                buffer.extractEnergy(accepted, false);
            });
        }
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
