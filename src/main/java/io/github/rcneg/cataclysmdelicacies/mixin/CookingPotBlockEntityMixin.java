package io.github.rcneg.cataclysmdelicacies.mixin;

import com.github.L_Ender.cataclysm.init.ModBlocks;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import vectorwing.farmersdelight.common.block.entity.CookingPotBlockEntity;

@Mixin(value = CookingPotBlockEntity.class)
public class CookingPotBlockEntityMixin {
    //修改烹饪时间
    @ModifyExpressionValue(
            method = "processCooking",
            at = @At(
                    value = "INVOKE",
                    target = "Lvectorwing/farmersdelight/common/crafting/CookingPotRecipe;getCookTime()I",
                    remap = false
            ),
            remap = false
    )
    private int cataclysmdelicacies$changeCookingSpeed(int original) {
        CookingPotBlockEntity blockEntity = (CookingPotBlockEntity) (Object) this;
        Level level = blockEntity.getLevel();
        if (level != null) {
            BlockState stoveBlock = level.getBlockState(blockEntity.getBlockPos().below());
            if (stoveBlock.is(ModBlocks.ALTAR_OF_FIRE.get())) {
                return Math.max(1, Math.round(original / 5.0F));
            }
        }
        return original;
    }
}
