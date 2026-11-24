package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.init.ModEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class AbyssalEggShellKnife extends CDKnifeItem {
    public AbyssalEggShellKnife(Properties properties) {
        super(9.0F, -2.6F, properties);
    }

    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean result = super.hurtEnemy(stack, target, attacker);
        if (result) {
            // 1.21.1 的 MobEffectInstance 构造器收 Holder<MobEffect>，DeferredHolder 本身就是 Holder
            target.addEffect(new MobEffectInstance(ModEffect.EFFECTABYSSAL_FEAR, 100));
        }
        return result;
    }
}
