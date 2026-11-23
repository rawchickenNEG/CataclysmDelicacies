package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModAttribute;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeTier;
import vectorwing.farmersdelight.common.item.KnifeItem;

import javax.annotation.Nullable;
import java.util.List;

import static com.github.L_Ender.cataclysm.items.Cataclysm_Weapon_Item.BASE_CRITICAL_ID;

public class CursiumKnifeItem extends KnifeItem {
    public CursiumKnifeItem(Properties properties) {
        super(new ForgeTier(3, 1750, 8.0F, 0.0F, 10,
                BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.WITHERITE_INGOT.get())),
                6.0F, -2.2F, properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
    {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.putAll(this.getDefaultAttributeModifiers(slot));
        double addition = getCount(stack) * Config.CURSIUM_KNIFE_POWER_ADD.get();
        if (slot == EquipmentSlot.MAINHAND) {
            builder.put(ModAttribute.ADDITIONAL_CRITICAL_DAMAGE.get(), new AttributeModifier(BASE_CRITICAL_ID, "Tool modifier", addition, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

    public static int getCount(ItemStack itemStack) {
        CompoundTag tag = itemStack.getTag();
        if (tag == null || !tag.contains("CDCursiumKnifeAddition"))
            return 0;
        return tag.getInt("CDCursiumKnifeAddition");
    }

    public boolean isBarVisible(ItemStack p_150899_) {
        return getCount(p_150899_) != 0;
    }

    public int getBarWidth(ItemStack p_150900_) {
        return Math.round((float)getCount(p_150900_) * 13.0F / 5);
    }

    public int getBarColor(ItemStack p_150901_) {
        return 0x3DD3B4;
    }

    public void setDamage(ItemStack stack, int damage) {
        super.setDamage(stack, -1);
    }

    //From The Annihilator
    public UseAnim getUseAnimation(ItemStack p_77661_1_) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack p_77626_1_) {
        return 72000;
    }

    public void releaseUsing(ItemStack p_43394_, Level p_43395_, LivingEntity p_43396_, int p_43397_) {
        if (p_43396_ instanceof Player player) {
            int i = this.getUseDuration(p_43394_) - p_43397_;
            if (i >= 40) {
                this.yall(p_43395_, p_43396_, p_43394_);
                if (!p_43395_.isClientSide) {
                    player.getCooldowns().addCooldown(this, 100);
                }
            }
        }

    }

    public void onUseTick(Level worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        int i = this.getUseDuration(stack) - count;
        if (i == 10) {
            this.masseffectParticle(worldIn, livingEntityIn, 2.0F);
        }

        if (i == 20) {
            this.masseffectParticle(worldIn, livingEntityIn, 3.5F);
        }

        if (i == 30) {
            this.masseffectParticle(worldIn, livingEntityIn, 5.0F);
        }

        if (i == 40) {
            livingEntityIn.playSound(ModSounds.MALEDICTUS_SHORT_ROAR.get(), 1.0F, 1.0F);
        }

    }

    public void inventoryTick(ItemStack itemstack, Level world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(itemstack, world, entity, slot, selected);
        if(!world.isClientSide()){
            CompoundTag tag = itemstack.getTag();
            if(tag != null){
                int rage = tag.getInt("CDCursiumKnifeAddition");
                long startTick = tag.getLong("CDCursiumKnifeStartTick");
                long currentTick = world.getGameTime();
                if ((currentTick - startTick) % Config.CURSIUM_KNIFE_POWER_DURATION.get() == 0) {
                    tag.putInt("CDCursiumKnifeAddition", Math.max(rage - 1, 0));
                }
            }
        }

    }

    private void yall(Level world, LivingEntity caster, ItemStack stack) {
        double radius = 6.0F;
        ScreenShake_Entity.ScreenShake(world, caster.position(), 30.0F, 0.1F, 0, 30);
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.5F, 1.0F / (caster.getRandom().nextFloat() * 0.4F + 0.8F));

        for(Entity entity : world.getEntities(caster, caster.getBoundingBox().inflate(radius, radius, radius))) {
            if (entity instanceof LivingEntity) {
                boolean hurtsuccess = entity.hurt(world.damageSources().mobAttack(caster), (float)caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
                if(hurtsuccess){
                    CompoundTag tag = stack.getOrCreateTag();
                    int rage = tag.getInt("CDCursiumKnifeAddition");
                    tag.putInt("CDCursiumKnifeAddition", Math.min(rage + 1, 5));
                    tag.putLong("CDCursiumKnifeStartTick", world.getGameTime());
                    if(caster.getOffhandItem().is(ItemRegistry.CURSIUM_KNIFE.get())){
                        ItemStack anotherKnife = caster.getOffhandItem();
                        CompoundTag tag2 = anotherKnife.getOrCreateTag();
                        tag2.putInt("CDCursiumKnifeAddition", Math.min(rage + 1, 5));
                        tag2.putLong("CDCursiumKnifeStartTick", world.getGameTime());
                    }
                }
            }
        }

        if (world.isClientSide) {
            world.addParticle(new RingParticleOptions(0.0F, ((float)Math.PI / 2F), 30, 86, 236, 204, 1.0F, 85.0F, false, 0), caster.getX(), caster.getY() + (double)0.03F, caster.getZ(), (double)0.0F, (double)0.0F, (double)0.0F);
        }

    }

    private void masseffectParticle(Level world, LivingEntity caster, float radius) {
        if (world.isClientSide) {
            for(int j = 0; j < 70; ++j) {
                float angle = (float)(Math.random() * (double)2.0F * Math.PI);
                double distance = Math.sqrt(Math.random()) * (double)radius;
                double extraX = caster.getX() + distance * (double) Mth.cos(angle);
                double extraY = caster.getY() + (double)0.3F;
                double extraZ = caster.getZ() + distance * (double)Mth.sin(angle);
                world.addParticle(ModParticle.PHANTOM_WING_FLAME.get(), extraX, extraY, extraZ, (double)0.0F, world.random.nextGaussian() * 0.04, (double)0.0F);
            }
        }

    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        ItemStack otherHand = hand == InteractionHand.MAIN_HAND ? player.getItemInHand(InteractionHand.OFF_HAND) : player.getItemInHand(InteractionHand.MAIN_HAND);
        if (otherHand.is(ItemRegistry.CURSIUM_KNIFE.get())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem()).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + stack.getItem() + "_1").withStyle(ChatFormatting.DARK_GREEN));
    }
}
