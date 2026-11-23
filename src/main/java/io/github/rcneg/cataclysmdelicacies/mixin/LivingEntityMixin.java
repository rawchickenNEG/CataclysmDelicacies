package io.github.rcneg.cataclysmdelicacies.mixin;

import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(
            method = "getDamageAfterArmorAbsorb",
            at = @At("HEAD"),
            cancellable = true
    )
    private void cataclysmdelicacies$atomAnnihilationEffectIgnoreArmor(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
        if(source.getEntity() instanceof LivingEntity living && living.hasEffect(EffectRegistry.ENERGY_MELTDOWN.get())){
            LivingEntity self = (LivingEntity)(Object)this;
            int amp = living.getEffect(EffectRegistry.ENERGY_MELTDOWN.get()).getAmplifier();
            float armor = self.getArmorValue();
            float extraDamage = armor * (amp + 1) * 0.2F;
            cir.setReturnValue(amount + extraDamage);
        }
    }

}
