package io.github.rcneg.cataclysmdelicacies.blocks;

import io.github.rcneg.cataclysmdelicacies.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

/**
 * 1.20.1 版本是靠 access transformer 把机械切菜板塞进 FD 切菜板的 BlockEntityType.validBlocks 里。
 * 1.21.1 改成给方块注册自己的 BlockEntityType，并在这里把 getType() 指向它，
 * 这样就不用再动 FD 的注册表内部结构了。
 */
public class MechanicalCuttingBoardEntity extends CuttingBoardBlockEntity {
    public MechanicalCuttingBoardEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return ModBlockEntityTypes.CUTTING_BOARD.get();
    }
}
