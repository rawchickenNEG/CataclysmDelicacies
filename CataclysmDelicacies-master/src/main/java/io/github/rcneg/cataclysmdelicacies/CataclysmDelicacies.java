package io.github.rcneg.cataclysmdelicacies;

import com.google.common.collect.ImmutableSet;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.*;
import io.github.rcneg.cataclysmdelicacies.mixin.BlockEntityTypeAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import vectorwing.farmersdelight.common.registry.ModBlockEntityTypes;

import java.util.Set;

// The value here should match an entry in the META-INF/mods.toml file
@SuppressWarnings("removal")
@Mod(CataclysmDelicacies.MODID)
public class CataclysmDelicacies
{
    public static final String MODID = "cataclysm_delicacies";

    public CataclysmDelicacies()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        ItemRegistry.ITEMS.register(modEventBus);
        BlockRegistry.BLOCKS.register(modEventBus);
        EffectRegistry.MOB_EFFECTS.register(modEventBus);
        LootModifierRegistry.LOOT_MODIFIER.register(modEventBus);
        TabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            applyBlockEntity(BlockRegistry.MECHANICAL_CUTTING_BOARD.get(), ModBlockEntityTypes.CUTTING_BOARD.get());
        });
    }

    private void clientSetup(FMLClientSetupEvent event) {
    }

    public void applyBlockEntity(Block block, BlockEntityType blockEntityType){
        BlockEntityTypeAccessor accessor = (BlockEntityTypeAccessor) blockEntityType;
        Set<Block> originalBlocks = accessor.cataclysmdelicacies$getValidBlocks();
        Set<Block> newBlocks = ImmutableSet.<Block>builder().addAll(originalBlocks).add(block).build();
        accessor.cataclysmdelicacies$setValidBlocks(newBlocks);
    }
}
