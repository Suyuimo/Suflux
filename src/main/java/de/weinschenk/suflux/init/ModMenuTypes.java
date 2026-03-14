package de.weinschenk.suflux.init;

import de.weinschenk.suflux.Suflux;
import de.weinschenk.suflux.menu.FluxInfuserMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Suflux.MODID);

    public static final RegistryObject<MenuType<FluxInfuserMenu>> FLUX_INFUSER_MENU =
            MENUS.register("flux_infuser",
                    () -> IForgeMenuType.create(FluxInfuserMenu::new));
}
