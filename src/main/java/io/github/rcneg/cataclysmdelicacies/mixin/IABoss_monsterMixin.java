package io.github.rcneg.cataclysmdelicacies.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.IABoss_monster;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(IABoss_monster.class)
public class IABoss_monsterMixin {
    @ModifyExpressionValue(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/IABossMonsters/IABoss_monster;DamageCap()F",
                    ordinal = 0,
                    remap = false
            )
    )
    private float cataclysmdelicacies$ignoreDamageCap(float damageCap, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(EffectRegistry.ENERGY_MELTDOWN) && Config.MELTDOWN_IGNORE_DAMAGE_CAP.get()) {
            return Float.MAX_VALUE;
        }
        return damageCap;
    }


    @ModifyExpressionValue(
            method = "hurt",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z",
                    ordinal = 1
            )
    )
    private boolean cataclysmdelicacies$ignoreDamageBucket(boolean original, DamageSource source, float amount) {
        if (source.getEntity() instanceof LivingEntity attacker && attacker.hasEffect(EffectRegistry.ENERGY_MELTDOWN) && Config.MELTDOWN_IGNORE_DAMAGE_BUCKET.get()) {
            return true;
        }
        return original;
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void cataclysmdelicacies$atomAnnihilationEffectIgnoreArmor(CallbackInfo ci) {
        IABoss_monster entity = (IABoss_monster)(Object)this;
        if(entity.isAlive() && Config.BOSSES_WITH_EFFECT.get()){
            int amp = Config.BOSSES_WITH_EFFECT_LEVEL.get();
            if(entity instanceof Maledictus_Entity){
                entity.addEffect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR, 5, amp));
            }
            if(entity instanceof Ancient_Remnant_Entity){
                entity.addEffect(new MobEffectInstance(EffectRegistry.ANKH_BLESSING, 5, amp));
            }
        }
    }
}
