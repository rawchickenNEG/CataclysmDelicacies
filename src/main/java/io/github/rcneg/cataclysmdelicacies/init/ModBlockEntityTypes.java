package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.blocks.MechanicalCuttingBoardEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CataclysmDelicacies.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MechanicalCuttingBoardEntity>> CUTTING_BOARD =
            BLOCK_ENTITY_TYPES.register("mechanical_cutting_board",
                    () -> BlockEntityType.Builder.of(MechanicalCuttingBoardEntity::new,
                            BlockRegistry.MECHANICAL_CUTTING_BOARD.get()).build(null));
}
