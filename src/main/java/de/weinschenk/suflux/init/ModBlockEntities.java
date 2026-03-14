package de.weinschenk.suflux.init;

import de.weinschenk.suflux.Suflux;
import de.weinschenk.suflux.blockentity.FluxInfuserBlockEntity;
import de.weinschenk.suflux.blockentity.InfiniteFluxCableBlockEntity;
import de.weinschenk.suflux.blockentity.TestConsumerBlockEntity;
import de.weinschenk.suflux.blockentity.TestGeneratorBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Suflux.MODID);

    public static final RegistryObject<BlockEntityType<InfiniteFluxCableBlockEntity>> INFINITE_FLUX_CABLE_BE =
            BLOCK_ENTITY_TYPES.register("infinite_flux_cable",
                    () -> BlockEntityType.Builder
                            .of(InfiniteFluxCableBlockEntity::new, Suflux.INFINITE_FLUX_CABLE.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<TestGeneratorBlockEntity>> TEST_GENERATOR_BE =
            BLOCK_ENTITY_TYPES.register("test_generator",
                    () -> BlockEntityType.Builder
                            .of(TestGeneratorBlockEntity::new, Suflux.TEST_GENERATOR.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<TestConsumerBlockEntity>> TEST_CONSUMER_BE =
            BLOCK_ENTITY_TYPES.register("test_consumer",
                    () -> BlockEntityType.Builder
                            .of(TestConsumerBlockEntity::new, Suflux.TEST_CONSUMER.get())
                            .build(null));

    public static final RegistryObject<BlockEntityType<FluxInfuserBlockEntity>> FLUX_INFUSER_BE =
            BLOCK_ENTITY_TYPES.register("flux_infuser",
                    () -> BlockEntityType.Builder
                            .of(FluxInfuserBlockEntity::new, Suflux.FLUX_INFUSER.get())
                            .build(null));
}
