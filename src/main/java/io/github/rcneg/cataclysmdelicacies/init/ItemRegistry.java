package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.items.*;
import io.github.rcneg.cataclysmdelicacies.items.knives.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.common.item.ConsumableItem;
import vectorwing.farmersdelight.common.item.PlaceableItem;

public class ItemRegistry {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CataclysmDelicacies.MODID);
    //材料
    public static final DeferredItem<Item> CROCODILE_TOOTH = ITEMS.register("crocodile_tooth", () -> new Item(defaultBuilder()));
    public static final DeferredItem<Item> LOBSTER_HEAD = ITEMS.register("lobster_head", () -> new Item(defaultBuilder()));

    //刀
    public static final DeferredItem<Item> WITHERITE_KNIFE = ITEMS.register("witherite_knife", () -> new WitheriteKnifeItem(defaultBuilder().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CURSIUM_KNIFE = ITEMS.register("cursium_knife", () -> new CursiumKnifeItem(defaultBuilder().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> REMNANT_TOOTH_KNIFE = ITEMS.register("remnant_tooth_knife", () -> new RemnantToothKnife(defaultBuilder().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> IGNITIUM_KNIFE = ITEMS.register("ignitium_knife", () -> new IgnitiumKnifeItem(defaultBuilder().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> ABYSSAL_EGG_SHELL_KNIFE = ITEMS.register("abyssal_egg_shell_knife", () -> new AbyssalEggShellKnife(defaultBuilder().rarity(Rarity.EPIC)));

    //厨具
    public static final DeferredItem<Item> MECHANICAL_CUTTING_BOARD = ITEMS.register("mechanical_cutting_board", () -> new MechanicalCuttingBoardBlockItem(BlockRegistry.MECHANICAL_CUTTING_BOARD.get(), defaultBuilder()));

    public static final DeferredItem<Item> ENERGY_STORAGE_TANK = ITEMS.register("energy_storage_tank", () -> new Item(defaultBuilder().fireResistant()));
    public static final DeferredItem<Item> REMNANT_TOOTH = ITEMS.register("remnant_tooth", () -> new Item(defaultBuilder()));
    //食材or材料
    public static final DeferredItem<Item> BREATH_OF_IGNIS = ITEMS.register("breath_of_ignis", () -> new ConsumableItem(foodBuilder(CDFoodValues.BREATH_OF_IGNIS).fireResistant(), true));
    public static final DeferredItem<Item> CURSED_ECTOPLASM = ITEMS.register("cursed_ectoplasm", () -> new ConsumableItem(foodBuilder(CDFoodValues.CURSED_ECTOPLASM), true));
    public static final DeferredItem<Item> CURSED_WING = ITEMS.register("cursed_wing", () -> new ConsumableItem(foodBuilder(CDFoodValues.CURSED_WING), true));
    public static final DeferredItem<Item> KOBOLETON_BONE_MARROW = ITEMS.register("koboleton_bone_marrow", () -> new ConsumableItem(foodBuilder(CDFoodValues.KOBOLETON_BONE_MARROW), true));
    public static final DeferredItem<Item> ROASTED_KOBOLETON_BONE_MARROW = ITEMS.register("roasted_koboleton_bone_marrow", () -> new ConsumableItem(foodBuilder(CDFoodValues.ROASTED_KOBOLETON_BONE_MARROW), true));

    //先驱者
    public static final DeferredItem<Item> HIGH_ENERGY_STEW = ITEMS.register("high_energy_stew", () -> new PlaceableItem(BlockRegistry.HIGH_ENERGY_STEW.get(), defaultBuilder().stacksTo(1)));
    public static final DeferredItem<Item> BOWL_OF_HIGH_ENERGY_STEW = ITEMS.register("bowl_of_high_energy_stew", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.BOWL_OF_HIGH_ENERGY_STEW), true));
    public static final DeferredItem<Item> ENERGY_CURRY_OVER_RICE = ITEMS.register("energy_curry_over_rice", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.ENERGY_CURRY_OVER_RICE), true));
    public static final DeferredItem<Item> ENERGY_DRINK = ITEMS.register("energy_drink", () -> new EnergyDrinkItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final DeferredItem<Item> ENERGY_JERRYCAN = ITEMS.register("energy_jerrycan", () -> new ConsumableItem(bucketFoodBuilder(CDFoodValues.ENERGY_JERRYCAN), true));
    public static final DeferredItem<Item> HIGH_ENERGY_PAGODA_MEAT = ITEMS.register("high_energy_pagoda_meat", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.HIGH_ENERGY_PAGODA_MEAT), true));
    public static final DeferredItem<Item> REDSTONE_FRIED_RICE = ITEMS.register("redstone_fried_rice", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.REDSTONE_FRIED_RICE), true));
    public static final DeferredItem<Item> CABLE_PASTA = ITEMS.register("cable_pasta", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.CABLE_PASTA), true));
    public static final DeferredItem<Item> CHIP_COOKIE = ITEMS.register("chip_cookie", () -> new ConsumableItem(foodBuilder(CDFoodValues.CHIP_COOKIE), true));

    //咒翼
    public static final DeferredItem<Item> CURSED_ECTOPLASM_FROZEN_MEAT = ITEMS.register("cursed_ectoplasm_frozen_meat", () -> new PlaceableItem(BlockRegistry.CURSED_ECTOPLASM_FROZEN_MEAT.get(), defaultBuilder().stacksTo(1)));
    public static final DeferredItem<Item> BOWL_OF_CURSED_ECTOPLASM_FROZEN_MEAT = ITEMS.register("bowl_of_cursed_ectoplasm_frozen_meat", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.BOWL_OF_CURSED_ECTOPLASM_FROZEN_MEAT), true));
    public static final DeferredItem<Item> CURSED_CURRY_STEW = ITEMS.register("cursed_curry_stew", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.CURSED_CURRY_STEW), true));
    public static final DeferredItem<Item> CURSED_DRINK = ITEMS.register("cursed_drink", () -> new CursedDrinkItem(new Item.Properties().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16)));
    public static final DeferredItem<Item> CURSED_CUSTARD = ITEMS.register("cursed_custard", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.CURSED_CUSTARD), true));
    public static final DeferredItem<Item> CURSED_JELLY = ITEMS.register("cursed_jelly", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.CURSED_JELLY), true));
    public static final DeferredItem<Item> CURSED_POPSICLE = ITEMS.register("cursed_popsicle", () -> new ConsumableItem(foodBuilder(CDFoodValues.CURSED_POPSICLE), true));
    public static final DeferredItem<Item> EORGETFULNESS_SOUP = ITEMS.register("eorgetfulness_soup", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.EORGETFULNESS_SOUP), true));
    public static final DeferredItem<Item> ORLEANS_GRILLED_CURSED_WING = ITEMS.register("orleans_grilled_cursed_wing", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.ORLEANS_GRILLED_CURSED_WING), true));

    //移魂
    public static final DeferredItem<Item> REMNANT_SKULL_WITH_STEAK = ITEMS.register("remnant_skull_with_steak", () -> new PlaceableItem(BlockRegistry.REMNANT_SKULL_WITH_STEAK.get(), defaultBuilder().stacksTo(1)));
    public static final DeferredItem<Item> BOWL_OF_REMNANT_SKULL_WITH_STEAK = ITEMS.register("bowl_of_remnant_skull_with_steak", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.BOWL_OF_REMNANT_SKULL_WITH_STEAK), true));
    public static final DeferredItem<Item> CRISPY_KOBOLETON_BONE_MARROW = ITEMS.register("crispy_koboleton_bone_marrow", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.CRISPY_KOBOLETON_BONE_MARROW), true));
    public static final DeferredItem<Item> KOBOLETON_BONE_BROTH = ITEMS.register("koboleton_bone_broth", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.KOBOLETON_BONE_BROTH), true));
    public static final DeferredItem<Item> KOBOLETON_SKEWERED_RABBIT_MEAT = ITEMS.register("koboleton_skewered_rabbit_meat", () -> new ConsumableItem(bowlFoodBuilder(CDFoodValues.KOBOLETON_SKEWERED_RABBIT_MEAT), true));
    public static final DeferredItem<Item> PREMIUM_DOG_FOOD = ITEMS.register("premium_dog_food", () -> new AncientDogFoodItem(foodBuilder(CDFoodValues.PREMIUM_DOG_FOOD)));

    private static Item.Properties defaultBuilder() {
        return new Item.Properties();
    }

    public static Item.Properties foodBuilder(FoodProperties food) {
        return defaultBuilder().food(food);
    }

    private static Item.Properties bowlFoodBuilder(FoodProperties food) {
        return defaultBuilder().craftRemainder(Items.BOWL).stacksTo(16).food(food);
    }

    private static Item.Properties drinkBuilder(FoodProperties food) {
        return defaultBuilder().craftRemainder(Items.GLASS_BOTTLE).stacksTo(16).food(food);
    }

    private static Item.Properties bucketFoodBuilder(FoodProperties food) {
        return defaultBuilder().craftRemainder(Items.BUCKET).stacksTo(1).food(food);
    }

    private static Item.Properties customContainerFoodBuilder(FoodProperties food, Item item) {
        return defaultBuilder().craftRemainder(item).stacksTo(16).food(food);
    }
}
