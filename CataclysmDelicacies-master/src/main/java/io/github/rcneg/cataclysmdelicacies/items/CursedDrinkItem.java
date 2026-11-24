package io.github.rcneg.cataclysmdelicacies.items;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import javax.annotation.Nullable;
import java.util.List;

public class CursedDrinkItem extends DrinkableItem {
    public CursedDrinkItem(Properties p_41383_) {
        super(p_41383_);
    }

    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        consumer.heal(consumer.getMaxHealth() * 0.5F);
        consumer.removeEffect(ModEffect.EFFECTGHOST_SICKNESS.get());
        if (consumer.hasEffect(ModEffect.EFFECTGHOST_SICKNESS.get())) {
            consumer.getActiveEffectsMap().remove(ModEffect.EFFECTGHOST_SICKNESS.get());
        }
        AABB screamBox = new AABB(consumer.blockPosition()).inflate(32);
        for(Maledictus_Entity entity : level.getEntitiesOfClass(Maledictus_Entity.class, screamBox)) {
            entity.setRageMeter(Math.max(entity.getRageMeter() - 1, 0));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.BLUE));
    }
}
