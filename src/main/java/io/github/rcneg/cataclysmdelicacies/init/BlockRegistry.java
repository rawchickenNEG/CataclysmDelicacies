package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.blocks.MechanicalCuttingBoard;
import io.github.rcneg.cataclysmdelicacies.blocks.RemnantSkullWithSteakBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.block.FeastBlock;

public class BlockRegistry {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CataclysmDelicacies.MODID);

    public static final DeferredBlock<Block> MECHANICAL_CUTTING_BOARD = BLOCKS.register("mechanical_cutting_board", () -> new MechanicalCuttingBoard(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).requiresCorrectToolForDrops().strength(15.0F, 1200.0F).sound(SoundType.NETHERITE_BLOCK)));

    // 1.21.1: 活塞推动行为只能通过 Properties#pushReaction 指定，Block#getPistonPushReaction 已被移除。
    public static final DeferredBlock<Block> REMNANT_SKULL_WITH_STEAK = BLOCKS.register("remnant_skull_with_steak",
            () -> new RemnantSkullWithSteakBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE).pushReaction(PushReaction.DESTROY), ItemRegistry.BOWL_OF_REMNANT_SKULL_WITH_STEAK)
    );
    public static final DeferredBlock<Block> CURSED_ECTOPLASM_FROZEN_MEAT = BLOCKS.register("cursed_ectoplasm_frozen_meat",
            () -> new FeastBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), ItemRegistry.BOWL_OF_CURSED_ECTOPLASM_FROZEN_MEAT, true)
    );
    public static final DeferredBlock<Block> HIGH_ENERGY_STEW = BLOCKS.register("high_energy_stew",
            () -> new FeastBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAKE), ItemRegistry.BOWL_OF_HIGH_ENERGY_STEW, true)
    );
}
