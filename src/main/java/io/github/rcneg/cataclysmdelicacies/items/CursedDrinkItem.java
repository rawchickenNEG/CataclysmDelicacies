package io.github.rcneg.cataclysmdelicacies.items;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import io.github.rcneg.cataclysmdelicacies.helper.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import java.util.List;

public class CursedDrinkItem extends DrinkableItem {
    public CursedDrinkItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        consumer.heal(consumer.getMaxHealth() * 0.5F);
        // 1.21.1: MobEffect 一律用 Holder<MobEffect> 引用；DeferredHolder 本身就是 Holder，
        // 所以这里不能再写 .get()（.get() 返回的是 MobEffect 本体）。
        consumer.removeEffect(ModEffect.EFFECTGHOST_SICKNESS);
        if (consumer.hasEffect(ModEffect.EFFECTGHOST_SICKNESS)) {
            consumer.getActiveEffectsMap().remove(ModEffect.EFFECTGHOST_SICKNESS);
        }
        AABB screamBox = new AABB(consumer.blockPosition()).inflate(32);
        for (Maledictus_Entity entity : level.getEntitiesOfClass(Maledictus_Entity.class, screamBox)) {
            entity.setRageMeter(Math.max(entity.getRageMeter() - 1, 0));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + ItemHelper.itemPath(stack)).withStyle(ChatFormatting.BLUE));
    }
}
