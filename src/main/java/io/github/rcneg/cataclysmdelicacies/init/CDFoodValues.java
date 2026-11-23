package io.github.rcneg.cataclysmdelicacies.init;

import com.github.L_Ender.cataclysm.init.ModEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import vectorwing.farmersdelight.common.registry.ModEffects;

public class CDFoodValues {
    public static final FoodProperties BOWL_OF_HIGH_ENERGY_STEW = (new FoodProperties.Builder()).nutrition(16).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 6000, 2), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 600, 2), 1F)
            .alwaysEat().build();
    public static final FoodProperties ENERGY_CURRY_OVER_RICE = (new FoodProperties.Builder()).nutrition(18).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 9600), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 1), 1F)
            .alwaysEat().build();
    public static final FoodProperties ENERGY_JERRYCAN = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.6F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 9600, 1), 1F)
            .alwaysEat().build();
    public static final FoodProperties HIGH_ENERGY_PAGODA_MEAT = (new FoodProperties.Builder()).nutrition(18).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 6000, 1), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4), 1F)
            .alwaysEat().build();
    public static final FoodProperties REDSTONE_FRIED_RICE = (new FoodProperties.Builder()).nutrition(12).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 3600, 1), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000), 1F)
            .alwaysEat().build();
    public static final FoodProperties CABLE_PASTA = (new FoodProperties.Builder()).nutrition(12).saturationMod(0.6F)
            .effect(new MobEffectInstance(EffectRegistry.ENERGY_MELTDOWN.get(), 3600, 0), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000), 1F)
            .build();
    public static final FoodProperties CHIP_COOKIE = (new FoodProperties.Builder()).nutrition(4).saturationMod(0.6F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 600), 1F)
            .effect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), 20), 1F)
            .alwaysEat().build();
    public static final FoodProperties PREMIUM_DOG_FOOD = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.2F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 600), 1F)
            .meat().build();
    public static final FoodProperties BOWL_OF_CURSED_ECTOPLASM_FROZEN_MEAT = (new FoodProperties.Builder()).nutrition(18).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 6000, 2), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3600, 1), 1F)
            .alwaysEat().build();
    public static final FoodProperties CURSED_CURRY_STEW = (new FoodProperties.Builder()).nutrition(16).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 6000), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.REGENERATION, 600, 1), 1F)
            .alwaysEat().build();
    public static final FoodProperties ORLEANS_GRILLED_CURSED_WING = (new FoodProperties.Builder()).nutrition(20).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 9600, 2), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3600, 4), 1F)
            .alwaysEat().build();
    public static final FoodProperties CURSED_JELLY = (new FoodProperties.Builder()).nutrition(10).saturationMod(0.5F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 6000), 1F)
            .alwaysEat().build();
    public static final FoodProperties CURSED_CUSTARD = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.5F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 3600), 1F)
            .alwaysEat().build();
    public static final FoodProperties CURSED_POPSICLE = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.35F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 600, 2), 1F)
            .alwaysEat().build();
    public static final FoodProperties EORGETFULNESS_SOUP = (new FoodProperties.Builder()).nutrition(12).saturationMod(0.6F)
            .effect(new MobEffectInstance(EffectRegistry.MALEDICTUS_AVATAR.get(), 3600, 1), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000), 1F)
            .alwaysEat().build();
    public static final FoodProperties BOWL_OF_REMNANT_SKULL_WITH_STEAK = (new FoodProperties.Builder()).nutrition(18).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ANKH_BLESSING.get(), 9600), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 9600), 1F)
            .effect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 4), 1F)
            .alwaysEat().build();
    public static final FoodProperties CRISPY_KOBOLETON_BONE_MARROW = (new FoodProperties.Builder()).nutrition(12).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ANKH_BLESSING.get(), 3600), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000), 1F)
            .effect(new MobEffectInstance(MobEffects.ABSORPTION, 3600, 1), 1F)
            .alwaysEat().build();
    public static final FoodProperties KOBOLETON_BONE_BROTH = (new FoodProperties.Builder()).nutrition(14).saturationMod(0.6F)
            .effect(new MobEffectInstance(EffectRegistry.ANKH_BLESSING.get(), 6000), 1F)
            .effect(new MobEffectInstance(ModEffects.NOURISHMENT.get(), 6000), 1F)
            .alwaysEat().build();
    public static final FoodProperties KOBOLETON_SKEWERED_RABBIT_MEAT = (new FoodProperties.Builder()).nutrition(14).saturationMod(0.85F)
            .effect(new MobEffectInstance(EffectRegistry.ANKH_BLESSING.get(), 6000), 1F)
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3600,1), 1F)
            .alwaysEat().build();
    public static final FoodProperties KOBOLETON_BONE_MARROW = (new FoodProperties.Builder()).nutrition(2).saturationMod(0.3F).build();
    public static final FoodProperties ROASTED_KOBOLETON_BONE_MARROW = (new FoodProperties.Builder()).nutrition(6).saturationMod(0.6F).build();
    public static final FoodProperties CURSED_WING = (new FoodProperties.Builder()).nutrition(5).saturationMod(0.3F)
            .effect(new MobEffectInstance(MobEffects.LEVITATION, 600,1), 1F)
            .effect(new MobEffectInstance(ModEffect.EFFECTGHOST_SICKNESS.get(), 200), 1F)
            .alwaysEat().build();
    public static final FoodProperties CURSED_ECTOPLASM = (new FoodProperties.Builder()).nutrition(3).saturationMod(0.3F)
            .effect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200), 1F)
            .effect(new MobEffectInstance(ModEffect.EFFECTGHOST_SICKNESS.get(), 60), 1F)
            .alwaysEat().build();
    public static final FoodProperties BREATH_OF_IGNIS = (new FoodProperties.Builder()).nutrition(2).saturationMod(0.3F)
            .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 9600), 1F)
            .effect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND.get(), 600, 4), 1F)
            .alwaysEat().build();
}
