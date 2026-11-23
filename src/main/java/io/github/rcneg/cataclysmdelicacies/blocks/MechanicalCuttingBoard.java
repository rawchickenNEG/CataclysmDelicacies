package io.github.rcneg.cataclysmdelicacies.blocks;

import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import vectorwing.farmersdelight.common.block.CuttingBoardBlock;
import vectorwing.farmersdelight.common.block.entity.CabinetBlockEntity;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;

import javax.annotation.Nullable;
import java.util.List;

public class MechanicalCuttingBoard extends CuttingBoardBlock {
    public MechanicalCuttingBoard(Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntityTypes.CUTTING_BOARD.get(), MechanicalCuttingBoard::tick
        );
    }

    private static void tick(Level level, BlockPos pos, BlockState state, CuttingBoardBlockEntity blockEntity) {
        if (level instanceof ServerLevel serverLevel) {
            ItemStack knife = getToolItem(level, pos.below(), blockEntity);
            FakePlayer fake = FakePlayerFactory.getMinecraft(serverLevel);
            if(level.getGameTime() % Config.MECHANICAL_CUTTING_CD.get() == 0L || knife.is(ItemRegistry.WITHERITE_KNIFE.get())){
                blockEntity.processStoredItemUsingTool(knife, fake);
            }
        }
    }

    private static ItemStack getToolItem(Level level, BlockPos pos, CuttingBoardBlockEntity cuttingBoard) {
        BlockEntity blockEntity = level.getBlockEntity(pos);

        if (blockEntity instanceof CabinetBlockEntity cabinet) {
            RecipeWrapper wrapper = new RecipeWrapper((IItemHandlerModifiable) cuttingBoard.getInventory());
            List<CuttingBoardRecipe> recipes = level.getRecipeManager().getRecipesFor(ModRecipeTypes.CUTTING.get(), wrapper, level);

            for (int i = 0; i < cabinet.getContainerSize(); i++) {
                ItemStack tool = cabinet.getItem(i);
                if (tool.isEmpty()) {
                    continue;
                }
                for (CuttingBoardRecipe recipe : recipes) {
                    if (recipe.getTool().test(tool)) {
                        return tool;
                    }
                }
            }
        }
        return new ItemStack(ModItems.IRON_KNIFE.get());
    }
}
