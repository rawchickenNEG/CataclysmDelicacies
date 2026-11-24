package io.github.rcneg.cataclysmdelicacies.events;

import com.github.L_Ender.cataclysm.init.ModEffect;
import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.init.CDTags;
import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 药水/状态效果相关事件。
 *
 * <p>1.21.1 改动要点：
 * <ul>
 *   <li>{@code MobEffectEvent.Applicable#setResult} 现在吃的是它自己的嵌套枚举
 *       {@code MobEffectEvent.Applicable.Result}（APPLY / DEFAULT / DO_NOT_APPLY），
 *       旧的 {@code Event.Result.DENY} 对应 {@code DO_NOT_APPLY}。</li>
 *   <li>效果一律以 {@code Holder<MobEffect>} 指代；标签用
 *       {@code BuiltInRegistries.MOB_EFFECT.getTagOrEmpty(tag)}（ForgeRegistries 已删除）。</li>
 * </ul>
 */
@EventBusSubscriber(modid = CataclysmDelicacies.MODID)
public class EffectEvents {
    @SubscribeEvent
    public static void onApplyPotion(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffectInstance instance = event.getEffectInstance();
        Holder<MobEffect> effect = instance.getEffect();
        MobEffect raw = effect.value();

        if (entity.hasEffect(EffectRegistry.ANKH_BLESSING) && isAnkhImmune(effect)) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
        if (raw == EffectRegistry.ANKH_BLESSING.get()) {
            for (Holder<MobEffect> mobEffect : BuiltInRegistries.MOB_EFFECT.getTagOrEmpty(CDTags.ANKH_BLESSING_IMMUNE)) {
                entity.removeEffect(mobEffect);
                if (entity.hasEffect(mobEffect)) {
                    entity.getActiveEffectsMap().remove(mobEffect);
                }
            }
        }
        //移除抗火效果
        if (entity.hasEffect(EffectRegistry.BLAZE_SCORCHING) && raw == MobEffects.FIRE_RESISTANCE.value()) {
            event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
        }
        if (raw == EffectRegistry.BLAZE_SCORCHING.get()) {
            entity.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
        if (entity.hasEffect(EffectRegistry.BLAZE_SCORCHING)
                && raw == ModEffect.EFFECTBLAZING_BRAND.get()
                && entity.hasEffect(ModEffect.EFFECTBLAZING_BRAND)
                && !entity.getPersistentData().contains("CataclysmDelicaciesHasBeenAppliedBrand")) {

            if (instance.getAmplifier() < entity.getEffect(EffectRegistry.BLAZE_SCORCHING).getAmplifier()) {
                int amp0 = entity.getEffect(EffectRegistry.BLAZE_SCORCHING).getAmplifier();
                int amp1 = entity.getEffect(ModEffect.EFFECTBLAZING_BRAND).getAmplifier();
                int amp = Math.min(amp1 + instance.getAmplifier() + 1, amp0);

                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                entity.getPersistentData().putBoolean("CataclysmDelicaciesHasBeenAppliedBrand", true);
                entity.addEffect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND, instance.getDuration(), amp, instance.isAmbient(), instance.isVisible(), instance.showIcon()));
                entity.getPersistentData().remove("CataclysmDelicaciesHasBeenAppliedBrand");
            }
        }
    }

    private static boolean isAnkhImmune(Holder<MobEffect> effect) {
        for (Holder<MobEffect> immune : BuiltInRegistries.MOB_EFFECT.getTagOrEmpty(CDTags.ANKH_BLESSING_IMMUNE)) {
            if (immune.value() == effect.value()) {
                return true;
            }
        }
        return false;
    }
}
