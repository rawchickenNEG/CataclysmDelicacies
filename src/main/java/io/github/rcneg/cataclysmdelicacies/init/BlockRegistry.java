package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.blocks.MechanicalCuttingBoard;
import io.github.rcneg.cataclysmdelicacies.blocks.RemnantSkullWithSteakBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.FeastBlock;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, CataclysmDelicacies.MODID);

    public static final RegistryObject<Block> MECHANICAL_CUTTING_BOARD = BLOCKS.register("mechanical_cutting_board", () -> new MechanicalCuttingBoard(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(15.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK)));

    public static final RegistryObject<Block> REMNANT_SKULL_WITH_STEAK = BLOCKS.register("remnant_skull_with_steak",
            () -> new RemnantSkullWithSteakBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), ItemRegistry.BOWL_OF_REMNANT_SKULL_WITH_STEAK)
    );
    public static final RegistryObject<Block> CURSED_ECTOPLASM_FROZEN_MEAT = BLOCKS.register("cursed_ectoplasm_frozen_meat",
            () -> new FeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), ItemRegistry.BOWL_OF_CURSED_ECTOPLASM_FROZEN_MEAT, true)
    );
    public static final RegistryObject<Block> HIGH_ENERGY_STEW = BLOCKS.register("high_energy_stew",
            () -> new FeastBlock(BlockBehaviour.Properties.copy(Blocks.CAKE), ItemRegistry.BOWL_OF_HIGH_ENERGY_STEW, true)
    );
}
