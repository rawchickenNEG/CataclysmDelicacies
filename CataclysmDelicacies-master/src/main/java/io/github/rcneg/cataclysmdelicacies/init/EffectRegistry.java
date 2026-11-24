package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.mobeffects.CDMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegistry {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, CataclysmDelicacies.MODID);
    public static final DeferredRegister<Potion> POTION = DeferredRegister.create(ForgeRegistries.POTIONS, CataclysmDelicacies.MODID);
    public static final RegistryObject<MobEffect> ENERGY_MELTDOWN = MOB_EFFECTS.register("energy_meltdown", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final RegistryObject<MobEffect> INFERNO_BREATHING = MOB_EFFECTS.register("inferno_breathing", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final RegistryObject<MobEffect> ANKH_BLESSING = MOB_EFFECTS.register("ankh_blessing", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));
    public static final RegistryObject<MobEffect> MALEDICTUS_AVATAR = MOB_EFFECTS.register("maledictus_avatar", () -> new CDMobEffect(MobEffectCategory.BENEFICIAL, 0x5D5B42));

    public static final RegistryObject<MobEffect> BLAZE_SCORCHING = MOB_EFFECTS.register("blaze_scorching", () -> new CDMobEffect(MobEffectCategory.HARMFUL, 0x5D5B42));
    public static final RegistryObject<MobEffect> CURSED = MOB_EFFECTS.register("cursed", () -> new CDMobEffect(MobEffectCategory.HARMFUL, 0x5D5B42));

}
