package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.client.particle.Options.RingParticleOptions;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.L_Ender.cataclysm.init.ModAttribute;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import io.github.rcneg.cataclysmdelicacies.init.ModDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;

public class CursiumKnifeItem extends CDKnifeItem {

    /**
     * Cataclysm 1.21.1 删掉了 items.Cataclysm_Weapon_Item（连同它的 BASE_CRITICAL_ID 常量），
     * 所以暴击伤害修饰符的 id 只能自己定义。
     */
    public static final ResourceLocation BASE_CRITICAL_ID =
            ResourceLocation.fromNamespaceAndPath(CataclysmDelicacies.MODID, "cursium_knife_critical");

    public CursiumKnifeItem(Properties properties) {
        super(6.0F, -2.2F, properties);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers base = super.getDefaultAttributeModifiers(stack);
        int count = getCount(stack);
        if (count <= 0) {
            return base;
        }
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            builder.add(entry.attribute(), entry.modifier(), entry.slot());
        }
        builder.add(ModAttribute.ADDITIONAL_CRITICAL_DAMAGE,
                new AttributeModifier(BASE_CRITICAL_ID, count * Config.CURSIUM_KNIFE_POWER_ADD.get(), AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);
        return builder.build();
    }

    public static int getCount(ItemStack itemStack) {
        Integer charge = itemStack.get(ModDataComponents.KNIFE_CHARGE.get());
        return charge == null ? 0 : charge;
    }

    public static void setCount(ItemStack itemStack, int count) {
        itemStack.set(ModDataComponents.KNIFE_CHARGE.get(), Math.max(0, count));
    }

    public boolean isBarVisible(ItemStack stack) {
        return getCount(stack) != 0;
    }

    public int getBarWidth(ItemStack stack) {
        return Math.round((float) getCount(stack) * 13.0F / 5);
    }

    public int getBarColor(ItemStack stack) {
        return 0x3DD3B4;
    }

    //From The Annihilator
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remainingUseTicks) {
        if (living instanceof Player player) {
            int i = this.getUseDuration(stack, living) - remainingUseTicks;
            if (i >= 40) {
                this.yall(level, living, stack);
                if (!level.isClientSide) {
                    player.getCooldowns().addCooldown(this, 100);
                }
            }
        }
    }

    public void onUseTick(Level worldIn, LivingEntity livingEntityIn, ItemStack stack, int count) {
        int i = this.getUseDuration(stack, livingEntityIn) - count;
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
        if (!world.isClientSide()) {
            Integer charge = itemstack.get(ModDataComponents.KNIFE_CHARGE.get());
            if (charge != null && charge > 0) {
                long startTick = itemstack.getOrDefault(ModDataComponents.KNIFE_START_TICK.get(), 0L);
                long currentTick = world.getGameTime();
                if ((currentTick - startTick) % Config.CURSIUM_KNIFE_POWER_DURATION.get() == 0) {
                    setCount(itemstack, charge - 1);
                }
            }
        }
    }

    private void yall(Level world, LivingEntity caster, ItemStack stack) {
        double radius = 6.0F;
        ScreenShake_Entity.ScreenShake(world, caster.position(), 30.0F, 0.1F, 0, 30);
        world.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 1.0F / (caster.getRandom().nextFloat() * 0.4F + 0.8F));

        for (Entity entity : world.getEntities(caster, caster.getBoundingBox().inflate(radius, radius, radius))) {
            if (entity instanceof LivingEntity) {
                boolean hurtsuccess = entity.hurt(world.damageSources().mobAttack(caster), (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2.0F);
                if (hurtsuccess) {
                    int rage = getCount(stack);
                    int newRage = Math.min(rage + 1, 5);
                    setCount(stack, newRage);
                    stack.set(ModDataComponents.KNIFE_START_TICK.get(), world.getGameTime());
                    if (caster.getOffhandItem().is(ItemRegistry.CURSIUM_KNIFE.get())) {
                        ItemStack anotherKnife = caster.getOffhandItem();
                        setCount(anotherKnife, newRage);
                        anotherKnife.set(ModDataComponents.KNIFE_START_TICK.get(), world.getGameTime());
                    }
                }
            }
        }

        if (world.isClientSide) {
            world.addParticle(new RingParticleOptions(0.0F, ((float) Math.PI / 2F), 30, 86, 236, 204, 1.0F, 85.0F, false, 0), caster.getX(), caster.getY() + 0.03F, caster.getZ(), 0.0F, 0.0F, 0.0F);
        }
    }

    private void masseffectParticle(Level world, LivingEntity caster, float radius) {
        if (world.isClientSide) {
            for (int j = 0; j < 70; ++j) {
                float angle = (float) (Math.random() * 2.0F * Math.PI);
                double distance = Math.sqrt(Math.random()) * radius;
                double extraX = caster.getX() + distance * Mth.cos(angle);
                double extraY = caster.getY() + 0.3F;
                double extraZ = caster.getZ() + distance * Mth.sin(angle);
                world.addParticle(ModParticle.PHANTOM_WING_FLAME.get(), extraX, extraY, extraZ, 0.0F, world.random.nextGaussian() * 0.04, 0.0F);
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
}
