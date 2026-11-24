package io.github.rcneg.cataclysmdelicacies.events;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.init.BlockRegistry;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * 客户端初始化：把本 mod 的所有方块设为 cutout 渲染（方块模型带透明像素）。
 *
 * <p>1.21.1 的 DeferredRegister 条目类型是 {@code DeferredHolder<Block, ? extends Block>}，
 * 不再是 Forge 的 {@code RegistryObject}。
 */
@EventBusSubscriber(modid = CataclysmDelicacies.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {
    @SuppressWarnings("deprecation")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        for (DeferredHolder<Block, ? extends Block> blocks : BlockRegistry.BLOCKS.getEntries()) {
            ItemBlockRenderTypes.setRenderLayer(blocks.get(), RenderType.cutout());
        }
    }
}
