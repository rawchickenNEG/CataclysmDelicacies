package io.github.rcneg.cataclysmdelicacies.integration.jei;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@JeiPlugin
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
@SuppressWarnings("unused")
public class CDJeiPlugin implements IModPlugin {
    private static final ResourceLocation UID = new ResourceLocation(CataclysmDelicacies.MODID, "jei_plugin");
    private static final List<RegistryObject<Item>> INFO_ITEMS = List.of(
            ItemRegistry.BREATH_OF_IGNIS,
            ItemRegistry.ENERGY_STORAGE_TANK,
            ItemRegistry.REMNANT_TOOTH,
            ItemRegistry.CURSED_ECTOPLASM,
            ItemRegistry.CURSED_JELLY,
            ItemRegistry.KOBOLETON_BONE_MARROW,
            ItemRegistry.CURSED_WING,
            ItemRegistry.CHIP_COOKIE
    );
    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        System.out.println("[CD] registering JEI info");
        for(RegistryObject<Item> registryObject: INFO_ITEMS){
            Item item = registryObject.get();
            registration.addIngredientInfo(
                    new ItemStack(item),
                    VanillaTypes.ITEM_STACK,
                    Component.translatable("cataclysm_delicacies.jei.info." + item)
            );
        }
    }
}
