package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.Cataclysm;
import com.github.L_Ender.cataclysm.client.particle.Options.ParryParticleOptions;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.L_Ender.cataclysm.util.AttributeUtils;
import com.github.L_Ender.cataclysm.util.CMDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;

import java.util.List;

public class WitheriteKnifeItem extends CDKnifeItem {
    public WitheriteKnifeItem(Properties properties) {
        super(6.0F, -2.0F, properties);
    }

    //From Cataclysm Meat_Shreedder
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        if (hand == InteractionHand.OFF_HAND) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(item);
        } else if (hand == InteractionHand.MAIN_HAND) {
            ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
            if (offHandItem.canPerformAction(ItemAbilities.SHIELD_BLOCK) && !player.getCooldowns().isOnCooldown(offHandItem.getItem())) {
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
        Cataclysm.PROXY.playWorldSound(living, (byte) 1);
        if (level instanceof ServerLevel serverLevel) {
            double range = 1.5F;
            Vec3 srcVec = living.getEyePosition();
            Vec3 lookVec = living.getViewVector(1.0F);
            Vec3 lookOffset = lookVec.scale(range);
            Vec3 destVec = srcVec.add(lookOffset);
            double padding = 1.0F;
            AABB searchArea = living.getBoundingBox().expandTowards(lookOffset).inflate(padding);
            List<Entity> possibleList = level.getEntities(living, searchArea);
            DamageSource shredderDamage = CMDamageTypes.causeShredderDamage(living);
            float basedmg = AttributeUtils.OriginDamage(living, stack);

            for (Entity entity : possibleList) {
                if (entity instanceof LivingEntity target) {
                    double borderSize = 0.5F;
                    AABB collisionBB = target.getBoundingBox().inflate(borderSize);
                    boolean isHit = collisionBB.contains(srcVec) || collisionBB.clip(srcVec, destVec).isPresent();
                    if (isHit) {
                        // 1.21.1 里 EnchantmentHelper.getDamageBonus(stack, mobType) 没了，
                        // 用 modifyDamage 一次算完"基础伤害 + 附魔加成"
                        float enchanteddmg = EnchantmentHelper.modifyDamage(serverLevel, stack, target, shredderDamage, basedmg);
                        float finaldmg = enchanteddmg / 8.5F;
                        if (target.hurt(shredderDamage, finaldmg)) {
                            serverLevel.sendParticles(new ParryParticleOptions(255, 106, 0), target.getX(), target.getY(0.5F), target.getZ(), 2, target.getDeltaMovement().x, target.getDeltaMovement().y, target.getDeltaMovement().z, level.getRandom().nextFloat() - 0.5F);
                        }
                    }
                }
            }
        }
    }

    public void releaseUsing(ItemStack stack, Level world, LivingEntity living, int remainingUseTicks) {
        world.playSound((Player) null, living.getX(), living.getY(), living.getZ(), (SoundEvent) ModSounds.SHREDDER_END.get(), SoundSource.PLAYERS, 1.5F, 1.0F / (living.getRandom().nextFloat() * 0.4F + 0.8F));
        Cataclysm.PROXY.clearSoundCacheFor(living);
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }
}
