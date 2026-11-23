package io.github.rcneg.cataclysmdelicacies.items;

import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.L_Ender.cataclysm.util.CMDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import vectorwing.farmersdelight.common.item.DrinkableItem;

import javax.annotation.Nullable;
import java.util.List;

public class EnergyDrinkItem extends DrinkableItem {
    public EnergyDrinkItem(Properties p_41383_) {
        super(p_41383_);
    }

    public void affectConsumer(ItemStack stack, Level level, LivingEntity consumer) {
        BlockPos pos = consumer.blockPosition().above();
        if(level instanceof ServerLevel serverLevel){
            serverLevel.sendParticles(ModParticle.EM_PULSE.get(), consumer.getX(), consumer.getY() + consumer.getBbHeight() / 2.0D, consumer.getZ(), 1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
        ScreenShake_Entity.ScreenShake(level, Vec3.atCenterOf(pos), 20.0F, 0.01F, 0, 20);
        level.playSound(null, pos, ModSounds.EMP_ACTIVATED.get(), SoundSource.BLOCKS, 4.0F, level.random.nextFloat() * 0.2F + 1.0F);
        AABB screamBox = new AABB(pos).inflate(5);

        for(LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, screamBox)) {
            entity.hurt(CMDamageTypes.getDamageSource(level, CMDamageTypes.EMP, new EntityType[0]), (float)(3 + entity.getRandom().nextInt(3)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.BLUE));
    }
}
