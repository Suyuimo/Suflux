package de.weinschenk.suflux.compat;

import de.weinschenk.suflux.blockentity.InfiniteFluxCableBlockEntity;
import mekanism.api.Action;
import mekanism.api.energy.IStrictEnergyHandler;
import mekanism.api.math.FloatingLong;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.WeakHashMap;

/**
 * Mekanism-Kompatibilität für das Infinite Flux Cable.
 * Diese Klasse wird NUR geladen wenn Mekanism vorhanden ist
 * (ModList-Check in InfiniteFluxCableBlockEntity vor jeder Referenz).
 */
public class MekanismEnergyCompat {

    static final Capability<IStrictEnergyHandler> ENERGY_CAP =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final WeakHashMap<InfiniteFluxCableBlockEntity, LazyOptional<IStrictEnergyHandler>> HANDLERS =
            new WeakHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> LazyOptional<T> getCapability(
            InfiniteFluxCableBlockEntity cable, Capability<T> cap, @Nullable Direction side) {
        if (cap == ENERGY_CAP) {
            return (LazyOptional<T>) HANDLERS.computeIfAbsent(cable,
                    k -> LazyOptional.of(() -> new CableEnergyHandler(cable)));
        }
        return LazyOptional.empty();
    }

    public static void invalidate(InfiniteFluxCableBlockEntity cable) {
        LazyOptional<IStrictEnergyHandler> opt = HANDLERS.remove(cable);
        if (opt != null) opt.invalidate();
    }

    public static void tick(Level level, BlockPos pos, InfiniteFluxCableBlockEntity cable) {
        EnumSet<Direction> pulledFrom = EnumSet.noneOf(Direction.class);

        // Pull: Energie aus Quellen ziehen
        for (Direction dir : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof InfiniteFluxCableBlockEntity) continue;

            neighbor.getCapability(ENERGY_CAP, dir.getOpposite()).ifPresent(source -> {
                FloatingLong available = source.extractEnergy(FloatingLong.MAX_VALUE, Action.SIMULATE);
                if (!available.isZero()) {
                    FloatingLong extracted = source.extractEnergy(available, Action.EXECUTE);
                    cable.mekanismBuffer += extracted.doubleValue();
                    pulledFrom.add(dir);
                }
            });
        }

        // Push: gepufferte Energie an Verbraucher weitergeben – NICHT zurück zur Quelle
        if (cable.mekanismBuffer <= 0) return;
        for (Direction dir : Direction.values()) {
            if (cable.mekanismBuffer <= 0) break;
            if (pulledFrom.contains(dir)) continue;
            BlockEntity neighbor = level.getBlockEntity(pos.relative(dir));
            if (neighbor == null || neighbor instanceof InfiniteFluxCableBlockEntity) continue;

            neighbor.getCapability(ENERGY_CAP, dir.getOpposite()).ifPresent(sink -> {
                FloatingLong toSend = FloatingLong.create(cable.mekanismBuffer);
                FloatingLong remainder = sink.insertEnergy(toSend, Action.EXECUTE);
                cable.mekanismBuffer = remainder.doubleValue();
            });
        }
    }

    // -------------------------------------------------------------------------

    private static class CableEnergyHandler implements IStrictEnergyHandler {

        private final InfiniteFluxCableBlockEntity cable;

        CableEnergyHandler(InfiniteFluxCableBlockEntity cable) {
            this.cable = cable;
        }

        @Override public int getEnergyContainerCount() { return 1; }

        @Override
        public FloatingLong getEnergy(int container) {
            return FloatingLong.create(cable.mekanismBuffer);
        }

        @Override
        public void setEnergy(int container, FloatingLong energy) {
            cable.mekanismBuffer = energy.doubleValue();
        }

        @Override public FloatingLong getMaxEnergy(int container)   { return FloatingLong.MAX_VALUE; }
        @Override public FloatingLong getNeededEnergy(int container) { return FloatingLong.MAX_VALUE; }

        @Override
        public FloatingLong insertEnergy(int container, FloatingLong amount, Action action) {
            if (!action.simulate()) cable.mekanismBuffer += amount.doubleValue();
            return FloatingLong.ZERO;
        }

        @Override
        public FloatingLong extractEnergy(int container, FloatingLong amount, Action action) {
            double toExtract = Math.min(amount.doubleValue(), cable.mekanismBuffer);
            if (!action.simulate()) cable.mekanismBuffer -= toExtract;
            return FloatingLong.create(toExtract);
        }
    }
}
