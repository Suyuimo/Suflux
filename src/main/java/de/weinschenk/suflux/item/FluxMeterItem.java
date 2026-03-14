package de.weinschenk.suflux.item;

import de.weinschenk.suflux.blockentity.InfiniteFluxCableBlockEntity;
import de.weinschenk.suflux.blockentity.TestConsumerBlockEntity;
import de.weinschenk.suflux.blockentity.TestGeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.jetbrains.annotations.NotNull;

public class FluxMeterItem extends Item {

    public FluxMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;

        BlockEntity be = level.getBlockEntity(context.getClickedPos());
        if (be == null) {
            player.sendSystemMessage(Component.literal("Kein RF-Block hier.").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }

        if (be instanceof InfiniteFluxCableBlockEntity cable) {
            player.sendSystemMessage(Component.literal("=== Infinite Flux Cable ===").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Durchsatz: " + cable.getLastTickTransferred() + " RF/Tick  (" + (cable.getLastTickTransferred() * 20L) + " RF/s)").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TestGeneratorBlockEntity) {
            be.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
                player.sendSystemMessage(Component.literal("=== Test Generator ===").withStyle(ChatFormatting.GOLD));
                player.sendSystemMessage(Component.literal("Gespeichert: " + storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF").withStyle(ChatFormatting.GREEN));
                player.sendSystemMessage(Component.literal("Produktion: 1.000 RF/Tick  (20.000 RF/s)").withStyle(ChatFormatting.GREEN));
            });
            return InteractionResult.SUCCESS;
        }

        if (be instanceof TestConsumerBlockEntity consumer) {
            player.sendSystemMessage(Component.literal("=== Test Consumer ===").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Empfangen (letzter Tick): " + consumer.getLastTickReceived() + " RF/Tick  (" + (consumer.getLastTickReceived() * 20L) + " RF/s)").withStyle(ChatFormatting.AQUA));
            player.sendSystemMessage(Component.literal("Gesamt verbraucht: " + consumer.getTotalConsumed() + " RF").withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }

        // Fallback: generische RF-Info für beliebige Blöcke
        be.getCapability(ForgeCapabilities.ENERGY).ifPresent(storage -> {
            player.sendSystemMessage(Component.literal("=== RF-Block ===").withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(Component.literal("Gespeichert: " + storage.getEnergyStored() + " / " + storage.getMaxEnergyStored() + " RF").withStyle(ChatFormatting.WHITE));
            player.sendSystemMessage(Component.literal("Kann empfangen: " + storage.canReceive() + "  |  Kann abgeben: " + storage.canExtract()).withStyle(ChatFormatting.GRAY));
        });

        return InteractionResult.SUCCESS;
    }
}
