package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.mobeffects.CDMobEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class EffectRegistry {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, CataclysmDelicacies.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> ENERGY_MELTDOWN = MOB_EFFECTS.register("energy_meltdown", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final DeferredHolder<MobEffect, MobEffect> INFERNO_BREATHING = MOB_EFFECTS.register("inferno_breathing", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final DeferredHolder<MobEffect, MobEffect> ANKH_BLESSING = MOB_EFFECTS.register("ankh_blessing", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final DeferredHolder<MobEffect, MobEffect> MALEDICTUS_AVATAR = MOB_EFFECTS.register("maledictus_avatar", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));

    public static final DeferredHolder<MobEffect, MobEffect> BLAZE_SCORCHING = MOB_EFFECTS.register("blaze_scorching", () -> new CDMobEffect(MobEffectCategory.HARMFUL, 0x5D5B42));
    public static final DeferredHolder<MobEffect, MobEffect> CURSED = MOB_EFFECTS.register("cursed", () -> new CDMobEffect(MobEffectCategory.HARMFUL, 0x5D5B42));

}
