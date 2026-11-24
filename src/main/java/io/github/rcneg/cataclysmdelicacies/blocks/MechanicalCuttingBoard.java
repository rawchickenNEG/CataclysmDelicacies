package io.github.rcneg.cataclysmdelicacies.blocks;

import com.mojang.serialization.MapCodec;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import io.github.rcneg.cataclysmdelicacies.init.ModBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CabinetBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipeInput;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import javax.annotation.Nullable;

public class MechanicalCuttingBoard extends CuttingBoardBlock {
    public MechanicalCuttingBoard(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CuttingBoardBlock.CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalCuttingBoardEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntityTypes.CUTTING_BOARD.get(), MechanicalCuttingBoard::tick);
    }

    private static void tick(Level level, BlockPos pos, BlockState state, MechanicalCuttingBoardEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack knife = getToolItem(level, pos.below(), blockEntity);
            FakePlayer fake = FakePlayerFactory.getMinecraft(serverLevel);
            if (level.getGameTime() % Config.MECHANICAL_CUTTING_CD.get() == 0L || knife.is(ItemRegistry.WITHERITE_KNIFE.get())) {
                blockEntity.processStoredItemUsingTool(knife, fake);
            }
        }
    }

    private static ItemStack getToolItem(Level level, BlockPos pos, CuttingBoardBlockEntity cuttingBoard) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof CabinetBlockEntity cabinet) {
            ItemStack stored = cuttingBoard.getStoredItem();
            for (int i = 0; i < cabinet.getContainerSize(); i++) {
                ItemStack tool = cabinet.getItem(i);
                if (tool.isEmpty()) {
                    continue;
                }
                // 1.21.1 里 CuttingBoardBlockEntity#getInventory() 返回的是 IItemHandler（不再是 Modifiable），
                // 旧代码的 new RecipeWrapper((IItemHandlerModifiable) ...) 会 ClassCastException，
                // 这里改用 FD 自己的 CuttingBoardRecipeInput 做匹配。
                CuttingBoardRecipeInput input = new CuttingBoardRecipeInput(stored, tool);
                if (!level.getRecipeManager().getRecipesFor(ModRecipeTypes.CUTTING.get(), input, level).isEmpty()) {
                    return tool;
                }
            }
        }
        return new ItemStack(ModItems.IRON_KNIFE.get());
    }
}
