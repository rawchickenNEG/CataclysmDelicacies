package io.github.rcneg.cataclysmdelicacies.helper;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class ItemHelper {

    /**
     * 1.21.1 里 Item#toString() 返回的是带命名空间的注册名（如 cataclysm_delicacies:witherite_knife），
     * 旧代码直接拿它拼 tooltip key 会拼错，这里统一取 path。
     */
    public static String itemPath(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();
    }
    public static boolean itemHasSmeltResult(ItemStack itemStack, Level level){
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), level).isPresent();
    }

    public static ItemStack getItemSmeltResult(ItemStack itemStack, Level level){
        if (itemHasSmeltResult(itemStack, level)) {
            return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(itemStack), level).map(holder -> holder.value().getResultItem(level.registryAccess()).copy()).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }
}
