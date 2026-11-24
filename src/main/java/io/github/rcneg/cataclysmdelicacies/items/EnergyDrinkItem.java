package io.github.rcneg.cataclysmdelicacies.items;

import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.L_Ender.cataclysm.util.CMDamageTypes;
import io.github.rcneg.cataclysmdelicacies.helper.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import java.util.List;

public class EnergyDrinkItem extends DrinkableItem {
    public EnergyDrinkItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        BlockPos pos = consumer.blockPosition().above();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ModParticle.EM_PULSE.get(), consumer.getX(), consumer.getY() + consumer.getBbHeight() / 2.0D, consumer.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        ScreenShake_Entity.ScreenShake(level, Vec3.atCenterOf(pos), 20.0F, 0.01F, 0, 20);
        level.playSound(null, pos, ModSounds.EMP_ACTIVATED.get(), SoundSource.BLOCKS, 4.0F, level.random.nextFloat() * 0.2F + 1.0F);
        AABB screamBox = new AABB(pos).inflate(5);

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, screamBox)) {
            // 1.21.1 的 getDamageSource 是可变参数（EntityType<?>...），不再需要显式传 new EntityType[0]
            entity.hurt(CMDamageTypes.getDamageSource(level, CMDamageTypes.EMP), 3 + entity.getRandom().nextInt(3));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + ItemHelper.itemPath(stack)).withStyle(ChatFormatting.BLUE));
    }
}
