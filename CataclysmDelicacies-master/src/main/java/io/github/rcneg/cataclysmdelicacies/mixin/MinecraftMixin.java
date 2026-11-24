package io.github.rcneg.cataclysmdelicacies.mixin;

import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    private int rightClickDelay;

    @Inject(method = "tick", at = @At("TAIL"))
    private void cataclysmdelicacies$reduceRightClickDelay(CallbackInfo ci) {
        Minecraft mc = (Minecraft)(Object)this;

        if (mc.player == null) {
            return;
        }
        ItemStack stack = mc.player.getMainHandItem();
        if (stack.is(ItemRegistry.WITHERITE_KNIFE.get())) {
            this.rightClickDelay = Math.min(this.rightClickDelay, 1);
        }
    }
}