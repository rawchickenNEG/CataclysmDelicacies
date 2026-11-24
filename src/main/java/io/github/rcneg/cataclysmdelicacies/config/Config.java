package io.github.rcneg.cataclysmdelicacies.config;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = CataclysmDelicacies.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config
{
    public static ModConfigSpec SPEC;
    // COMMON
    public static ModConfigSpec.BooleanValue MELTDOWN_IGNORE_DAMAGE_CAP;
    public static ModConfigSpec.BooleanValue MELTDOWN_IGNORE_DAMAGE_BUCKET;
    public static ModConfigSpec.DoubleValue CURSED_PERCENTAGE_BASE;
    public static ModConfigSpec.DoubleValue CURSED_PERCENTAGE_EXTRA;
    public static ModConfigSpec.IntValue CURSED_DURATION;
    public static ModConfigSpec.IntValue CURSED_CD;
    public static ModConfigSpec.IntValue MECHANICAL_CUTTING_CD;
    public static ModConfigSpec.IntValue CURSIUM_KNIFE_POWER_DURATION;
    public static ModConfigSpec.DoubleValue CURSIUM_KNIFE_POWER_ADD;
    public static ModConfigSpec.IntValue REVENANT_KNIFE_HIGH_HP;
    public static ModConfigSpec.DoubleValue REVENANT_KNIFE_CHANCE;

    public static ModConfigSpec.BooleanValue BOSSES_WITH_EFFECT;
    public static ModConfigSpec.IntValue BOSSES_WITH_EFFECT_LEVEL;

    static {
        ModConfigSpec.Builder COMMON_BUILDER = new ModConfigSpec.Builder();

        COMMON_BUILDER.push("MobEffects");
        MELTDOWN_IGNORE_DAMAGE_CAP = COMMON_BUILDER.comment("If it's true, Energy Meltdown effect will have the ability to ignore Damage Cap in Cataclysm.")
                .define("MeltdownIgnoreDamageCap", true);
        MELTDOWN_IGNORE_DAMAGE_BUCKET = COMMON_BUILDER.comment("If it's true, Energy Meltdown effect will have the ability to ignore Dps Cap in Cataclysm.")
                .define("MeltdownIgnoreDPSCap", true);
        CURSED_PERCENTAGE_BASE = COMMON_BUILDER.comment("Define the base damage percentage based on effect holder's Max HP of Curse effect from Maledictus Avatar.")
                .defineInRange("CursedPercentageBase", 1.5, 0, Double.MAX_VALUE);
        CURSED_PERCENTAGE_EXTRA = COMMON_BUILDER.comment("Define the extra damage (per effect level) percentage based on effect holder's Max HP of Curse effect from Maledictus Avatar.")
                .defineInRange("CursedPercentageExtra", 0.5, 0, Double.MAX_VALUE);
        CURSED_DURATION = COMMON_BUILDER.comment("Define the Duration of Curse effect from Maledictus Avatar (in ticks)")
                .defineInRange("CursedDuration", 200, 0, Integer.MAX_VALUE);
        CURSED_CD = COMMON_BUILDER.comment("Define the Cooldown of triggering Curse effect from Maledictus Avatar (in ticks)")
                .defineInRange("CursedCD", 20, 0, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("CookingSets");
        MECHANICAL_CUTTING_CD = COMMON_BUILDER.comment("Define the Cooldown of Mechanical Cutting Board processing (in ticks)")
                .defineInRange("MechanicalCuttingCD", 10, 1, Integer.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("Knives");
        CURSIUM_KNIFE_POWER_DURATION = COMMON_BUILDER.comment("Define the duration of Cursium Knife Power-UP (in ticks, each level)")
                .defineInRange("CursiumKnifePowerDuration", 200, 1, Integer.MAX_VALUE);
        CURSIUM_KNIFE_POWER_ADD = COMMON_BUILDER.comment("Define the crit-damage add of Cursium Knife Power-UP (each level)")
                .defineInRange("CursiumKnifePowerAdd", 25, 1, Double.MAX_VALUE);
        REVENANT_KNIFE_HIGH_HP = COMMON_BUILDER.comment("Define the HP amount which can be considered as 'High HP'. When the targets' original max HP higher or equal to this value, the Revenant knife will not effective when their HP more than 50%")
                .defineInRange("RevenantKnifeHighHP", 200, 0, Integer.MAX_VALUE);
        REVENANT_KNIFE_CHANCE = COMMON_BUILDER.comment("Define the chance of get looting when using Revenant knife")
                .defineInRange("RevenantKnifeChance", 10, 0, Double.MAX_VALUE);
        COMMON_BUILDER.pop();

        COMMON_BUILDER.push("BossesWithFoodEffects");
        BOSSES_WITH_EFFECT = COMMON_BUILDER.comment("If it's true, Cataclysm Bosses will have corresponding food effects.")
                .define("BossesWithFoodEffects", false);
        BOSSES_WITH_EFFECT_LEVEL = COMMON_BUILDER.comment("Define the effect level of food effects on Cataclysm Bosses (Actual level should add this value by 1)")
                .defineInRange("BossesFoodEffectLevel", 2, 0, 255);
        COMMON_BUILDER.pop();

        SPEC = COMMON_BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
    }
}
