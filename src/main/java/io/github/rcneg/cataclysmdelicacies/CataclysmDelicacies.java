package io.github.rcneg.cataclysmdelicacies;

import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.BlockRegistry;
import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import io.github.rcneg.cataclysmdelicacies.init.ModBlockEntityTypes;
import io.github.rcneg.cataclysmdelicacies.init.ModDataComponents;
import io.github.rcneg.cataclysmdelicacies.init.TabRegistry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CataclysmDelicacies.MODID)
public class CataclysmDelicacies
{
    public static final String MODID = "cataclysm_delicacies";

    public CataclysmDelicacies(IEventBus modEventBus, ModContainer modContainer)
    {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(this::commonSetup);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        EffectRegistry.MOB_EFFECTS.register(modEventBus);
        ModBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        TabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        // The mechanical cutting board uses its own BlockEntityType now (see ModBlockEntityTypes),
        // so the old "patch Farmer's Delight's validBlocks set" hack is no longer needed.
    }
}
