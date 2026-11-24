package io.github.rcneg.cataclysmdelicacies.events;

import com.github.L_Ender.cataclysm.init.ModEffect;
import io.github.rcneg.cataclysmdelicacies.init.CDTags;
import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber
public class EffectEvents {
    @SubscribeEvent
    public static void onApplyPotion(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        MobEffect effect = event.getEffectInstance().getEffect();
        if(entity.hasEffect(EffectRegistry.ANKH_BLESSING.get()) && ForgeRegistries.MOB_EFFECTS.tags().getTag(CDTags.ANKH_BLESSING_IMMUNE).contains(effect)){
            event.setResult(Event.Result.DENY);
        }
        if(effect == EffectRegistry.ANKH_BLESSING.get()){
            for(MobEffect mobEffect : ForgeRegistries.MOB_EFFECTS.tags().getTag(CDTags.ANKH_BLESSING_IMMUNE)){
                entity.removeEffect(mobEffect);
                if (entity.hasEffect(mobEffect)) {
                    entity.getActiveEffectsMap().remove(mobEffect);
                }
            }
        }
        //移除抗火效果
        if(entity.hasEffect(EffectRegistry.BLAZE_SCORCHING.get()) && effect == MobEffects.FIRE_RESISTANCE){
            event.setResult(Event.Result.DENY);
        }
        if(effect == EffectRegistry.BLAZE_SCORCHING.get()){
            entity.removeEffect(MobEffects.FIRE_RESISTANCE);
        }
        if(        entity.hasEffect(EffectRegistry.BLAZE_SCORCHING.get())
                && effect == ModEffect.EFFECTBLAZING_BRAND.get()
                && entity.hasEffect(ModEffect.EFFECTBLAZING_BRAND.get())
                && !entity.getPersistentData().contains("CataclysmDelicaciesHasBeenAppliedBrand")){

            MobEffectInstance effectI = event.getEffectInstance();
            if(effectI.getAmplifier() < entity.getEffect(EffectRegistry.BLAZE_SCORCHING.get()).getAmplifier()){
                int amp0 = entity.getEffect(EffectRegistry.BLAZE_SCORCHING.get()).getAmplifier();
                int amp1 = entity.getEffect(ModEffect.EFFECTBLAZING_BRAND.get()).getAmplifier();
                int amp = Math.min(amp1 + effectI.getAmplifier() + 1, amp0);

                event.setResult(Event.Result.DENY);
                entity.getPersistentData().putBoolean("CataclysmDelicaciesHasBeenAppliedBrand", true);
                entity.addEffect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND.get(), effectI.getDuration(), amp, effectI.isAmbient(), effectI.isVisible(), effectI.showIcon()));
                entity.getPersistentData().remove("CataclysmDelicaciesHasBeenAppliedBrand");

            }
        }
    }
}
