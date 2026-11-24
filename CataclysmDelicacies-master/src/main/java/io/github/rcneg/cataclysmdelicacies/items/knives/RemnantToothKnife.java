package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTier;
import vectorwing.farmersdelight.common.item.KnifeItem;

import javax.annotation.Nullable;
import java.util.List;

public class RemnantToothKnife extends KnifeItem {
    public RemnantToothKnife(Properties properties) {
        super(new ForgeTier(3, 1750, 8.0F, 0.0F, 10,
                BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.WITHERITE_INGOT.get())),
                6.0F, -2.0F, properties);
    }

    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, -1);
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.DARK_GREEN));
    }
}
