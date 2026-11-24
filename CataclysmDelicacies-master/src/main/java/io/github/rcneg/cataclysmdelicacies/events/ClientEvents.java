package io.github.rcneg.cataclysmdelicacies.events;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.init.BlockRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("removal")
@Mod.EventBusSubscriber(modid = CataclysmDelicacies.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event)
    {
        for(RegistryObject<Block> blocks: BlockRegistry.BLOCKS.getEntries()){
            ItemBlockRenderTypes.setRenderLayer(blocks.get(), RenderType.cutout());
        }
    }
}
