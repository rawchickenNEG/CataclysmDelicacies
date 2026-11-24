package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class TabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CataclysmDelicacies.MODID);
    private static final List<Supplier<Item>> NO_TAB_ITEMS = List.of(
            ItemRegistry.CROCODILE_TOOTH,
            ItemRegistry.LOBSTER_HEAD
            );

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register(CataclysmDelicacies.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.cataclysm_delicacies"))
            .icon(() -> new ItemStack(ItemRegistry.ENERGY_CURRY_OVER_RICE.get()))
            .displayItems((parameters, output) -> {
                for (DeferredHolder<Item, ? extends Item> item : ItemRegistry.ITEMS.getEntries()) {
                    if (!NO_TAB_ITEMS.contains(item)) output.accept(item.get());
                }
            }).build());
}
