package de.weinschenk.suflux.block;

import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class FluxCrystalOreBlock extends DropExperienceBlock {

    public FluxCrystalOreBlock(BlockBehaviour.Properties properties) {
        super(properties, UniformInt.of(2, 5));
    }
}
