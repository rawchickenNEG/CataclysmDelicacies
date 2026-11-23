package io.github.rcneg.cataclysmdelicacies.helper;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public class ItemHelper {
    public static boolean itemHasSmeltResult(ItemStack itemStack, Level level){
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), level).isPresent();
    }

    public static ItemStack getItemSmeltResult(ItemStack itemStack, Level level){
        if (itemHasSmeltResult(itemStack, level)) {
            return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SimpleContainer(itemStack), level).map(recipe -> recipe.getResultItem(level.registryAccess()).copy()).orElse(ItemStack.EMPTY);
        }
        return ItemStack.EMPTY;
    }
}
