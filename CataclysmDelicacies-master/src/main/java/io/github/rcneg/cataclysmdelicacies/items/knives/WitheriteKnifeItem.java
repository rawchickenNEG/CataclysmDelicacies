package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.Cataclysm;
import com.github.L_Ender.cataclysm.client.particle.Options.ParryParticleOptions;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.L_Ender.cataclysm.util.AttributeUtils;
import com.github.L_Ender.cataclysm.util.CMDamageTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.ToolActions;
import vectorwing.farmersdelight.common.item.KnifeItem;

import javax.annotation.Nullable;
import java.util.List;

public class WitheriteKnifeItem extends KnifeItem {
    public WitheriteKnifeItem(Properties properties) {
        super(new ForgeTier(3, 1750, 8.0F, 0.0F, 10,
                BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.WITHERITE_INGOT.get())),
                6.0F, -2.0F, properties);
    }

    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, -1);
    }

    //From Cataclysm Meat_Shreedder
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(item);
        } else if (hand == InteractionHand.MAIN_HAND) {
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offHandItem.canPerformAction(ToolActions.SHIELD_BLOCK) && !player.getCooldowns().isOnCooldown(offHandItem.getItem())) {
                return InteractionResultHolder.pass(item);
            } else {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(item);
            }
        } else {
            return InteractionResultHolder.pass(item);
        }
    }

    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int count) {
        Cataclysm.PROXY.playWorldSound(living, (byte)1);
        if (level instanceof ServerLevel serverLevel) {
            double range = (double)1.5F;
            Vec3 srcVec = living.getEyePosition();
            Vec3 lookVec = living.getViewVector(1.0F);
            Vec3 lookOffset = lookVec.scale(range);
            Vec3 destVec = srcVec.add(lookOffset);
            double padding = (double)1.0F;
            AABB searchArea = living.getBoundingBox().expandTowards(lookOffset).inflate(padding);
            List<Entity> possibleList = level.getEntities(living, searchArea);
            DamageSource shredderDamage = CMDamageTypes.causeShredderDamage(living);
            float basedmg = AttributeUtils.OriginDamage(living, stack);

            for(Entity entity : possibleList) {
                if (entity instanceof LivingEntity target) {
                    double borderSize = (double)0.5F;
                    AABB collisionBB = target.getBoundingBox().inflate(borderSize);
                    boolean isHit = collisionBB.contains(srcVec) || collisionBB.clip(srcVec, destVec).isPresent();
                    if (isHit) {
                        float bonusDamage = EnchantmentHelper.getDamageBonus(stack, target.getMobType());
                        float enchanteddmg = basedmg + bonusDamage;
                        float finaldmg = enchanteddmg / 8.5F;
                        if (target.hurt(shredderDamage, finaldmg)) {
                            serverLevel.sendParticles(new ParryParticleOptions(255, 106, 0), target.getX(), target.getY((double)0.5F), target.getZ(), 2, target.getDeltaMovement().x, target.getDeltaMovement().y, target.getDeltaMovement().z, (double)(level.getRandom().nextFloat() - 0.5F));
                        }
                    }
                }
            }
        }

    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int remainingUseTicks) {
        world.playSound((Player)null, living.getX(), living.getY(), living.getZ(), (SoundEvent) ModSounds.SHREDDER_END.get(), SoundSource.PLAYERS, 1.5F, 1.0F / (living.getRandom().nextFloat() * 0.4F + 0.8F));
        Cataclysm.PROXY.clearSoundCacheFor(living);
    }

    public int getUseDuration(ItemStack p_43419_) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem() + "_1").withStyle(ChatFormatting.DARK_GREEN));
    }
}
