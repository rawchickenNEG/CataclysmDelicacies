package io.github.rcneg.cataclysmdelicacies.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.rcneg.cataclysmdelicacies.helper.ItemHelper;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CuttingBoardBlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(value = CuttingBoardBlockEntity.class)
public class CuttingBoardBlockEntityMixin {

    @ModifyExpressionValue(
            method = "lambda$processStoredItemUsingTool$2",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/crafting/CuttingBoardRecipe;rollResults(Lnet/minecraft/util/RandomSource;ILnet/neoforged/neoforge/items/wrapper/RecipeWrapper;)Ljava/util/List;"
            ),
            remap = false
    )
    private List<ItemStack> cataclysmdelicacies$modifyCuttingResult(List<ItemStack> original, ItemStack toolStack, @Nullable Player player) {
        List<ItemStack> modified = new ArrayList<>();
        Level level = player.level();
        if (toolStack.is(ItemRegistry.IGNITIUM_KNIFE.get())) {
            for (ItemStack stack : original) {
                ItemStack smeltedStack = ItemHelper.getItemSmeltResult(stack, level);
                smeltedStack.setCount(stack.getCount());
                if(!smeltedStack.isEmpty()){
                    modified.add(smeltedStack);
                }else{
                    modified.add(stack);
                }
            }
            return modified;
        }else{
            return original;
        }
    }

}



