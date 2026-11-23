package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.init.ModEffect;
import com.github.L_Ender.cataclysm.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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

public class AbyssalEggShellKnife extends KnifeItem {
    public AbyssalEggShellKnife(Properties properties) {
        super(new ForgeTier(3, 1750, 8.0F, 0.0F, 10,
                BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.WITHERITE_INGOT.get())),
                9.0F, -2.6F, properties);
    }

    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, -1);
    }

    public boolean hurtEnemy(ItemStack p_40994_, LivingEntity p_40995_, LivingEntity p_40996_) {
        boolean result = super.hurtEnemy(p_40994_, p_40995_, p_40996_);
        if(result){
            p_40995_.addEffect(new MobEffectInstance(ModEffect.EFFECTABYSSAL_FEAR.get(), 100));
        }
        return result;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem() + "_1").withStyle(ChatFormatting.DARK_GREEN));
    }
}
