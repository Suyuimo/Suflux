package de.weinschenk.suflux;

import com.mojang.logging.LogUtils;
import de.weinschenk.suflux.block.FluxCrystalOreBlock;
import de.weinschenk.suflux.block.FluxInfuserBlock;
import de.weinschenk.suflux.block.InfiniteFluxCableBlock;
import de.weinschenk.suflux.block.TestConsumerBlock;
import de.weinschenk.suflux.block.TestGeneratorBlock;
import de.weinschenk.suflux.item.FluxMeterItem;
import de.weinschenk.suflux.init.ModBlockEntities;
import de.weinschenk.suflux.init.ModMenuTypes;
import de.weinschenk.suflux.screen.FluxInfuserScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Suflux.MODID)
public class Suflux {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "suflux";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "suflux" namespace
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "suflux" namespace
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "suflux" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);


    // Infinite Flux Cable – transportiert unbegrenzt RF
    public static final RegistryObject<Block> INFINITE_FLUX_CABLE = BLOCKS.register("infinite_flux_cable",
            () -> new InfiniteFluxCableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(1.5f)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> INFINITE_FLUX_CABLE_ITEM = ITEMS.register("infinite_flux_cable",
            () -> new BlockItem(INFINITE_FLUX_CABLE.get(), new Item.Properties()));

    // Test-Generator – produziert 1000 RF/Tick zum Testen des Kabels
    public static final RegistryObject<Block> TEST_GENERATOR = BLOCKS.register("test_generator",
            () -> new TestGeneratorBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.FIRE)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> TEST_GENERATOR_ITEM = ITEMS.register("test_generator",
            () -> new BlockItem(TEST_GENERATOR.get(), new Item.Properties()));

    // Test-Consumer – verschluckt RF, zählt Gesamtmenge
    public static final RegistryObject<Block> TEST_CONSUMER = BLOCKS.register("test_consumer",
            () -> new TestConsumerBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(2.0f)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> TEST_CONSUMER_ITEM = ITEMS.register("test_consumer",
            () -> new BlockItem(TEST_CONSUMER.get(), new Item.Properties()));

    // Flux Meter – Rechtsklick auf RF-Block zeigt Infos im Chat
    public static final RegistryObject<Item> FLUX_METER = ITEMS.register("flux_meter",
            () -> new FluxMeterItem(new Item.Properties().stacksTo(1)));

    // --- Erze ---
    public static final RegistryObject<Block> FLUX_CRYSTAL_ORE = BLOCKS.register("flux_crystal_ore",
            () -> new FluxCrystalOreBlock(BlockBehaviour.Properties.copy(Blocks.IRON_ORE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> FLUX_CRYSTAL_ORE_ITEM = ITEMS.register("flux_crystal_ore",
            () -> new BlockItem(FLUX_CRYSTAL_ORE.get(), new Item.Properties()));

    public static final RegistryObject<Block> DEEPSLATE_FLUX_CRYSTAL_ORE = BLOCKS.register("deepslate_flux_crystal_ore",
            () -> new FluxCrystalOreBlock(BlockBehaviour.Properties.copy(Blocks.DEEPSLATE_IRON_ORE)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> DEEPSLATE_FLUX_CRYSTAL_ORE_ITEM = ITEMS.register("deepslate_flux_crystal_ore",
            () -> new BlockItem(DEEPSLATE_FLUX_CRYSTAL_ORE.get(), new Item.Properties()));

    // --- Materialien ---
    public static final RegistryObject<Item> RAW_FLUX_CRYSTAL      = ITEMS.register("raw_flux_crystal",  () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUX_CRYSTAL           = ITEMS.register("flux_crystal",      () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUX_CONDUCTOR         = ITEMS.register("flux_conductor",    () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUX_INSULATION        = ITEMS.register("flux_insulation",   () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> FLUX_CORE              = ITEMS.register("flux_core",         () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ENERGIZED_FLUX_CORE   = ITEMS.register("energized_flux_core", () -> new Item(new Item.Properties()));

    // --- Flux Infuser Maschine ---
    public static final RegistryObject<Block> FLUX_INFUSER = BLOCKS.register("flux_infuser",
            () -> new FluxInfuserBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5f)
                    .requiresCorrectToolForDrops()));
    public static final RegistryObject<Item> FLUX_INFUSER_ITEM = ITEMS.register("flux_infuser",
            () -> new BlockItem(FLUX_INFUSER.get(), new Item.Properties()));

    // Creates a creative tab with the id "suflux:example_tab" for the example item, that is placed after the combat tab
    public static final RegistryObject<CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).icon(() -> INFINITE_FLUX_CABLE_ITEM.get().getDefaultInstance()).displayItems((parameters, output) -> {
        output.accept(INFINITE_FLUX_CABLE_ITEM.get());
        output.accept(TEST_GENERATOR_ITEM.get());
        output.accept(TEST_CONSUMER_ITEM.get());
        output.accept(FLUX_METER.get());
        output.accept(FLUX_CRYSTAL_ORE_ITEM.get());
        output.accept(DEEPSLATE_FLUX_CRYSTAL_ORE_ITEM.get());
        output.accept(RAW_FLUX_CRYSTAL.get());
        output.accept(FLUX_CRYSTAL.get());
        output.accept(FLUX_CONDUCTOR.get());
        output.accept(FLUX_INSULATION.get());
        output.accept(FLUX_CORE.get());
        output.accept(ENERGIZED_FLUX_CORE.get());
        output.accept(FLUX_INFUSER_ITEM.get());
    }).build());

    public Suflux() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        ITEMS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so tabs get registered
        CREATIVE_MODE_TABS.register(modEventBus);
        // Register block entity types
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        // Register menu types
        ModMenuTypes.MENUS.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        if (Config.logDirtBlock) LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) { }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
            event.enqueueWork(() ->
                MenuScreens.register(ModMenuTypes.FLUX_INFUSER_MENU.get(), FluxInfuserScreen::new)
            );
        }
    }
}
