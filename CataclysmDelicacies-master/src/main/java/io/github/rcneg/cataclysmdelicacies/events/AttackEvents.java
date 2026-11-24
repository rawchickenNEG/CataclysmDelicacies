package io.github.rcneg.cataclysmdelicacies.events;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.ibm.icu.impl.Pair;
import io.github.rcneg.cataclysmdelicacies.config.Config;
import io.github.rcneg.cataclysmdelicacies.init.EffectRegistry;
import io.github.rcneg.cataclysmdelicacies.init.ItemRegistry;
import io.github.rcneg.cataclysmdelicacies.mixin.LootTableAccessor;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber
public class AttackEvents {
    public static final String CURSED_CD = "CataclysmDelicaciesCursedDamageCD";
    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event){
        LivingEntity entity = event.getEntity();
        Level level = event.getEntity().level();
        if(!level.isClientSide()){
            if(entity.hasEffect(EffectRegistry.BLAZE_SCORCHING.get()) && event.getSource().is(DamageTypeTags.IS_FIRE)){
                float amount = event.getAmount();
                event.setCanceled(true);
                entity.hurt(entity.level().damageSources().magic(), amount);
                level.playSound(null, entity.getOnPos(), SoundEvents.PLAYER_HURT_ON_FIRE, SoundSource.HOSTILE, 4.0F, level.random.nextFloat() * 0.2F + 1.0F);
            }
        }
    }
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event){
        LivingEntity entity = event.getEntity();
        RandomSource random = entity.getRandom();
        Level level = event.getEntity().level();
        if(!level.isClientSide()){
            //中咒附带百分比伤害
            if(entity.hasEffect(EffectRegistry.CURSED.get())){
                if(canTrigger(entity, CURSED_CD)){
                    int amp = entity.getEffect(EffectRegistry.CURSED.get()).getAmplifier();
                    float extraDamage = (float) (entity.getMaxHealth() * (Config.CURSED_PERCENTAGE_BASE.get() + Config.CURSED_PERCENTAGE_EXTRA.get() * amp) * 0.01F);
                    event.setAmount(event.getAmount() + extraDamage);
                    setCooldown(entity, CURSED_CD, Config.CURSED_CD.get());
                    if(level instanceof ServerLevel serverLevel){
                        serverLevel.sendParticles(ModParticle.PHANTOM_EMITTER.get(), entity.getX(), entity.getY() + entity.getBbHeight() / 2.0D, entity.getZ(), 16, entity.getBbWidth(), entity.getBbHeight(), entity.getBbWidth(), 0.0D);
                    }
                    level.playSound(null, entity.getOnPos(), ModSounds.PHANTOM_SPEAR.get(), SoundSource.HOSTILE, 4.0F, level.random.nextFloat() * 0.2F + 1.0F);
                }
            }
            //恶火增加爆炸、火焰和魔法伤害
            if(entity.hasEffect(EffectRegistry.BLAZE_SCORCHING.get())){
                DamageSource source = event.getSource();
                if(source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION) || source.is(DamageTypeTags.WITCH_RESISTANT_TO)){
                    int amp = entity.getEffect(EffectRegistry.BLAZE_SCORCHING.get()).getAmplifier();
                    float extraDamageMulti = amp + 1;
                    event.setAmount(event.getAmount() * extraDamageMulti);
                }
            }
            if(event.getSource().getEntity() instanceof LivingEntity attacker){
                //烈焚灼息附带恶焰
                if(attacker.hasEffect(EffectRegistry.INFERNO_BREATHING.get())){
                    int amp = attacker.getEffect(EffectRegistry.INFERNO_BREATHING.get()).getAmplifier();
                    entity.addEffect(new MobEffectInstance(EffectRegistry.BLAZE_SCORCHING.get(), 200, amp));
                    //提升炽热烙印等级
                    if(entity.hasEffect(ModEffect.EFFECTBLAZING_BRAND.get())){
                        int durB = entity.getEffect(ModEffect.EFFECTBLAZING_BRAND.get()).getDuration();
                        int ampB = entity.getEffect(ModEffect.EFFECTBLAZING_BRAND.get()).getAmplifier();
                        entity.addEffect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND.get(), durB, Math.min(ampB + 1, amp)));
                    }
                    int ampB = entity.hasEffect(ModEffect.EFFECTBLAZING_BRAND.get()) ? entity.getEffect(ModEffect.EFFECTBLAZING_BRAND.get()).getAmplifier() + 1 : 0;
                    int ampS = entity.hasEffect(EffectRegistry.BLAZE_SCORCHING.get()) ? entity.getEffect(EffectRegistry.BLAZE_SCORCHING.get()).getAmplifier() + 1 : 0;
                    if(!(attacker instanceof Ignis_Entity)){
                        attacker.heal(Math.max(attacker.getMaxHealth() * 0.05F, 5F) * Math.max((ampB + ampS), 1));
                    }
                }
                //咒魂化身附带中咒
                if(attacker.hasEffect(EffectRegistry.MALEDICTUS_AVATAR.get())){
                    int amp = attacker.getEffect(EffectRegistry.MALEDICTUS_AVATAR.get()).getAmplifier();
                    entity.addEffect(new MobEffectInstance(EffectRegistry.CURSED.get(), Config.CURSED_DURATION.get(), amp));
                }
            }
        }
        if (level instanceof ServerLevel serverLevel && event.getSource().getEntity() instanceof LivingEntity attacker) {
            if(attacker.getMainHandItem().is(ItemRegistry.REMNANT_TOOTH_KNIFE.get()) && !entity.getPersistentData().contains("CataclysmDelicaciesHasBeenLoot")){
                if(random.nextFloat() <= Config.REVENANT_KNIFE_CHANCE.get() * 0.01F){
                    if(entity.getAttributeBaseValue(Attributes.MAX_HEALTH) < Config.REVENANT_KNIFE_HIGH_HP.get() || entity.getHealth() <= entity.getMaxHealth() / 2){
                        ResourceLocation lootId = entity.getLootTable();
                        LootParams ctx = new LootParams.Builder(serverLevel)
                                .withParameter(LootContextParams.THIS_ENTITY, entity)
                                .withParameter(LootContextParams.ORIGIN, entity.position())
                                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                                .withOptionalParameter(LootContextParams.KILLER_ENTITY, attacker)
                                .create(LootContextParamSets.ENTITY);
                        List<ItemStack> drops = serverLevel.getServer().getLootData().getLootTable(lootId).getRandomItems(ctx);
                        if (!drops.isEmpty()) {
                            ItemStack drop = drops.get(attacker.getRandom().nextInt(drops.size())).copy();
                            drop.setCount(1);
                            entity.spawnAtLocation(drop);
                            entity.getPersistentData().putBoolean("CataclysmDelicaciesHasBeenLoot", true);
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityLoot(LivingDropsEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity entity = event.getEntity();
            //From AlexsMobsDelight
            if (event.getEntity().level() instanceof ServerLevel level && event.getSource().getEntity() instanceof LivingEntity attacker) {
                ResourceLocation lootId = entity.getLootTable();
                LootTable lootTable = level.getServer().getLootData().getLootTable(lootId);
                List<ItemStack> allDrops = new ArrayList<>();
                List<Item> actualDrops = new ArrayList<>();
                LootContext ctx = new LootContext.Builder(
                        new LootParams.Builder(level)
                                .withParameter(LootContextParams.THIS_ENTITY, entity)
                                .withParameter(LootContextParams.ORIGIN, entity.position())
                                .withParameter(LootContextParams.DAMAGE_SOURCE, event.getSource())
                                .withOptionalParameter(LootContextParams.KILLER_ENTITY, attacker)
                                .create(LootContextParamSets.ENTITY)
                ).create(null);
                LootTableAccessor accessor = (LootTableAccessor) lootTable;
                List<LootPool> pools = accessor.cataclysmdelicacies$getLootTablePools();

                if(attacker.getMainHandItem().is(ItemRegistry.ABYSSAL_EGG_SHELL_KNIFE.get())){
                    for (LootPool pool : pools) {
                        for (LootPoolEntryContainer entry : pool.entries) {
                            if (entry instanceof LootPoolSingletonContainer singleton) {
                                if(singleton instanceof LootItem lootItem){
                                    ItemStack baseStack = new ItemStack(lootItem.item);
                                    for (LootItemFunction function : singleton.functions) {
                                        baseStack = function.apply(baseStack, ctx);
                                    }
                                    if (baseStack.getCount() <= 0) {
                                        baseStack.setCount(1);
                                    }
                                    allDrops.add(baseStack.copy());
                                }

                            }
                        }
                    }
                    for(ItemEntity actualDrop : event.getDrops()){
                        actualDrops.add(actualDrop.getItem().getItem());
                    }
                    for(ItemStack drop : allDrops){
                        drop.setCount(1);
                        if(!actualDrops.contains(drop.getItem())){
                            addEntityDrops(event, drop);
                        }
                    }
                }
                if(attacker.getMainHandItem().is(ItemRegistry.IGNITIUM_KNIFE.get()) && entity.hasEffect(ModEffect.EFFECTBLAZING_BRAND.get())){
                    int amp = entity.getEffect(ModEffect.EFFECTBLAZING_BRAND.get()).getAmplifier();
                    int maxNumber = Math.min(amp, 4);
                    int minNumber = Math.max(amp - 3, 1);
                    int number = minNumber >= maxNumber ? maxNumber : entity.getRandom().nextInt(minNumber, maxNumber);
                    for(int i = 0; i < number; i++){
                        Objects.requireNonNull(level.getServer()).getLootData().getLootTable(lootId).getRandomItems(ctx, s -> entity.spawnAtLocation(s, 1.0F));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void LivingDeathEvent(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && tryMaledictusAvatarRebirth(event.getEntity())) {
            event.setCanceled(true);
        }

    }

    //咒灵化身触发鬼魂形态
    private static boolean tryMaledictusAvatarRebirth(LivingEntity living) {
        Level var4 = living.level();
        if(living instanceof Maledictus_Entity) return false;
        if (var4 instanceof ServerLevel serverLevel) {
            if (living.hasEffect(EffectRegistry.MALEDICTUS_AVATAR.get()) && !living.hasEffect(ModEffect.EFFECTGHOST_SICKNESS.get()) && !living.hasEffect(ModEffect.EFFECTGHOST_FORM.get())) {
                living.setHealth(5.0F);
                serverLevel.playSound(null, living.getX(), living.getY(), living.getZ(), SoundEvents.TOTEM_USE, living.getSoundSource(), 1.25F, 1.0F);
                living.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200, 0));
                living.addEffect(new MobEffectInstance(ModEffect.EFFECTGHOST_FORM.get(), 100, 0, false, true, true));
                double d0 = living.getX();
                double d1 = living.getY() + (double)3.0F;
                double d2 = living.getZ();
                serverLevel.sendParticles(ModParticle.CURSED_ALGIZ.get(), d0, d1, d2, 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
                return true;
            }
        }

        return false;
    }

    public static void addEntityDrops(LivingDropsEvent event, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), itemStack);
        itemEntity.setPickUpDelay(10);
        event.getDrops().add(itemEntity);
    }

    public static boolean canTrigger(LivingEntity entity, String key) {
        CompoundTag data = entity.getPersistentData();

        long cooldownAdded = data.getLong(key);
        long currentTime = entity.level().getGameTime();

        return currentTime >= cooldownAdded;
    }

    public static void setCooldown(LivingEntity entity, String key, int cooldownTicks) {
        entity.getPersistentData().putLong(key, entity.level().getGameTime() + cooldownTicks);
    }
}
