# Cataclysm Delicacies：1.20.1 Forge → 1.21.1 NeoForge 移植要点

> 用途：这份文档是给"人和 AI"直接吃的工作底稿。它记录了源码结构、每一处需要改写的 API、数据包/资源路径变化、依赖 Mod 的 API 破坏点，以及建议的施工顺序。
> 所有"已验证"的结论都对照过真实文件（见文末"验证材料"），不是凭记忆写的。
> 旧源码：`CataclysmDelicacies-master/`（Forge 1.20.1-47.4.10，Java 17，39 个 Java 文件，约 2000 行）
> 目标工程：本目录（NeoForge 21.1.219 / MC 1.21.1 / Java 21 MDK 模板）

---

## 0. 一页速览

**总工作量估计**：39 个 Java 文件里，约 60% 只需要改 import 与方法名；真正需要重新设计的是 5 处：

| 优先级 | 位置 | 问题 | 处理方式 |
|---|---|---|---|
| P0 | `events/AttackEvents.java` | 1.21.1 的 NeoForge **删除了 `LivingAttackEvent` / `LivingHurtEvent`** | 改用 `LivingIncomingDamageEvent`（可取消、pre-mitigation）与 `LivingDamageEvent.Pre`（post-armor） |
| P0 | `items/knives/*`（5 个刀） | Forge 的 `ForgeTier(...)`、`getAttributeModifiers(EquipmentSlot, ItemStack)`、`EnchantmentHelper.getDamageBonus` 全部没了；FD 的 `KnifeItem` 只保留 `(Tier, Properties)` 构造 | 用 `SimpleTier` + `Item.Properties.attributes(DiggerItem.createAttributes(tier, dmg, speed))` |
| P0 | `items/knives/CursiumKnifeItem.java` | 用 `ItemStack#getTag()/setTag()` 存充能层数与起始 tick | 改成自定义 `DataComponentType<Integer>`（或 `DataComponents.CUSTOM_DATA`） |
| P1 | `loot/AddLootModifier.java` + `init/LootModifierRegistry.java` + 7 个 `loot_modifiers/*.json` | `IGlobalLootModifier#codec()` 返回类型从 `Codec` 变 `MapCodec`，JSON 字段名也不同 | **直接删除自定义实现**，改用 NeoForge 内置的 `neoforge:add_table`（字段 `table`，条件 `neoforge:loot_table_id`） |
| P1 | `blocks/MechanicalCuttingBoard.java` + `mixin/BlockEntityTypeAccessor.java` | 旧代码靠"改 FD 的 BlockEntityType.validBlocks"让自定义方块复用 FD 的方块实体 | 推荐改为**自己注册 BlockEntityType + 继承 FD 的 `CuttingBoardBlockEntity`**，从而删掉这个 mixin 和 `applyBlockEntity` hack |

**用户原始猜测的纠正**：源码里**没有**注册任何自定义 RecipeType / RecipeSerializer。它只是"消费"三种已有配方类型：`farmersdelight:cooking`、`farmersdelight:cutting`、`cataclysm:weapon_fusion`。所以"自定义配方类型的不同写法"这一项**不存在**；真正要改的是**配方 JSON 的字段格式**（见第 5 节）。

**最容易漏的非代码项**：
1. 数据包目录全部改成单数：`recipes/` → `recipe/`，`loot_tables/` → `loot_table/`，`tags/items/` → `tags/item/`，`tags/blocks/` → `tags/block/`，`tags/mob_effect/` 不变，`tags/entity_types/` → `tags/entity_type/`。
2. 所有 ItemStack JSON 由 `{"item": "...", "count": n}` 变成 `{"id": "...", "count": n}`（`item` 键名改成 `id`）。**这是全项目最容易批量踩坑的一处**。
3. `forge:` 命名空间 → `c:`（标签）与 `neoforge:`（条件/全局战利品修饰器文件位置）。
4. `global_loot_modifiers.json` 从 `data/forge/loot_modifiers/` 移到 `data/neoforge/loot_modifiers/`。

---

## 1. 目标环境（本工程已验证的版本组合）

| 组件 | 版本/形式 | 说明 |
|---|---|---|
| Minecraft | 1.21.1 | `minecraft_version_range=[1.21.1]` |
| NeoForge | 21.1.219（模板），可上 21.1.228 | `implementation "net.neoforged:neoforge:${neo_version}"` |
| Java | 21（toolchain） | 旧工程是 17 |
| 映射 | Parchment `1.21.1 / 2024.11.17`，方法名用**官方名**（mojmap） | 不再有 `mapping_channel=official` 这种 SRG 概念 |
| Gradle 插件 | `net.neoforged.gradle.userdev` 7.1.38 | 旧的是 `net.minecraftforge.gradle [6.0,6.2)` |
| Mixin | NeoForge 自带，**不需要** mixin gradle 插件，也不需要 annotationProcessor | 见 2.3 |
| MixinExtras | NeoForge 21.1.228 已内置 `META-INF/jarjar/mixinextras-neoforge-0.5.3.jar` | 所以 `@ModifyExpressionValue` 可以直接用，**删掉**旧 build.gradle 里的 `mixinextras-common` / `mixinextras-forge` 依赖 |

依赖 Mod（**已实际下载 jar 校验过 API**）：

| Mod | 目标版本 | 旧版本 |
|---|---|---|
| FarmersDelight | `farmers-delight-398521:8765184`（mod 版本 1.3.4，`neoforge` 依赖范围 `[21.1.219,)`，loader 4.x） | `8083474`（1.20.1） |
| Lendercataclysm | `lendercataclysm-551586:8931182` | `8246047` |
| Curios | `curios-309927:9694505` | `6418456` |
| Lionfish API | `lionfish-api-1001614:8345326` | `8095554` |
| JEI | 模板里被注释掉了；若要保留 JEI 信息页需自己加 `mezz.jei:jei-1.21.1-*`（19.x） | `15.20.0.129` |
| Jade | 旧工程 runtimeOnly，可选，1.21.1 对应 Jade 15.x | — |

> ⚠️ 模板的 `build.gradle` **目前没有 farmers-delight 依赖**，但整个 mod 都依赖 FD 的 `ConsumableItem`、`KnifeItem`、`FeastBlock`、`CuttingBoardBlockEntity`、`ModRecipeTypes` 等。移植第一步就要补上。

---

## 2. 工程骨架迁移

### 2.1 build.gradle

旧（ForgeGradle）关键片段：

```gradle
minecraft 'net.minecraftforge:forge:1.20.1-47.4.10'
mixin { add sourceSets.main, "cataclysm_delicacies.refmap.json"; config "cataclysm_delicacies.mixins.json" }
implementation fg.deobf("curse.maven:farmers-delight-398521:8083474")
```

新（NeoGradle userdev）对应写法（模板已有大半，补 FD 即可）：

```gradle
dependencies {
    implementation "net.neoforged:neoforge:${neo_version}"

    implementation "curse.maven:farmers-delight-398521:8765184"
    implementation "curse.maven:lendercataclysm-551586:8931182"
    implementation "curse.maven:curios-309927:9694505"
    implementation "curse.maven:lionfish-api-1001614:8345326"

    // 可选 JEI（模板中已注释，取消注释并按 1.21.1 版本改）
    // compileOnly "mezz.jei:jei-1.21.1-common-api:19.x"
    // compileOnly "mezz.jei:jei-1.21.1-neoforge-api:19.x"
    // runtimeOnly "mezz.jei:jei-1.21.1-neoforge:19.x"
}
```

要**删掉**的东西：`mixin` 插件块、`annotationProcessor` 的 mixin processor、`mixinextras-*` 依赖、`mapping_channel`/`forge_version` 等旧属性。

### 2.2 gradle.properties / 元数据

```properties
mod_id=cataclysm_delicacies
mod_name=Cataclysm Delicacies
mod_license=All Rights Reserved        # 沿用旧值
mod_version=1.0.1
mod_group_id=io.github.rcneg.cataclysmdelicacies
mod_authors=rawchickenNEG, BFMeow
```

### 2.3 neoforge.mods.toml（由 `META-INF/mods.toml` 改名并改格式）

旧 `mods.toml` 的 `modLoader="javafml"`、`loaderVersion`、`[[mods]]` 结构基本保留；改动点：

- 文件改名为 `META-INF/neoforge.mods.toml`
- 依赖块从 `[[dependencies.<modid>]]` + `mandatory=true` 改为 `type="required"` + `versionRange`：

```toml
[[dependencies.cataclysm_delicacies]]
modId="neoforge"
type="required"
versionRange="[${neo_version},)"
ordering="NONE"
side="BOTH"

[[dependencies.cataclysm_delicacies]]
modId="minecraft"
type="required"
versionRange="${minecraft_version_range}"

[[dependencies.cataclysm_delicacies]]
modId="farmersdelight"
type="required"
versionRange="[1.3.4,)"
ordering="AFTER"
side="BOTH"

[[dependencies.cataclysm_delicacies]]
modId="cataclysm"
type="required"
ordering="AFTER"
side="BOTH"
```

- 显式声明 mixin 配置与 AT 文件（NeoForge 靠这两块加载，模板里有注释示例）：

```toml
[[mixins]]
config="cataclysm_delicacies.mixins.json"

[[accessTransformers]]
file="META-INF/accesstransformer.cfg"
```

### 2.4 mixins.json

`cataclysm_delicacies.mixins.json` 保留，改两处：

```json
{
  "required": true,
  "package": "io.github.rcneg.cataclysmdelicacies.mixin",
  "compatibilityLevel": "JAVA_21",
  "minVersion": "0.8",
  "mixins": [ ... ],
  "client": [ "MinecraftMixin" ],
  "injectors": { "defaultRequire": 1 }
}
```

- `"refmap"` 字段可以删（NeoForge 21.1 用官方名运行，不需要 refmap；留着也不致命，但那样就得保留 mixin AP）。
- 目标仍然是 FD / MC / Cataclysm 的类，`@ModifyExpressionValue` 上的 `remap = false` **必须保留**（这些不是 MC 混淆名映射问题，而是"别按 MC 映射去重映射第三方类"）。

### 2.5 accesstransformer.cfg（改成官方名 + 新字段类型）

旧（SRG 名）：

```
public net.minecraft.world.level.storage.loot.LootPool f_79023_ # entries
public net.minecraft.world.level.storage.loot.entries.LootItem f_79564_ # item
public net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer f_79677_ # functions
```

新（官方名；字段仍然存在，但**类型变了**）：

```
public net.minecraft.world.level.storage.loot.LootPool entries # entries
public net.minecraft.world.level.storage.loot.entries.LootItem item # item: Holder<Item>
public net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer functions # functions
```

已验证 1.21.1 实际字段：
- `LootPool.entries` → `private final List<LootPoolEntryContainer> entries`
- `LootItem.item` → `private final Holder<Item> item`（旧版是 `Item`，这是 `AttackEvents` 里取物品堆时必须改的地方）
- `LootPoolSingletonContainer.functions` → `protected final List<LootItemFunction> functions`

> 若不想用 AT：这 3 处也可以写成 mixin `@Accessor`（旧工程本来就有 `LootTableAccessor`）。二选一，别两套并存。

### 2.6 pack.mcmeta

旧的是 `"pack_format": 15`。1.21.1 资源包格式是 **34**、数据包格式是 **48**。
但实测 FarmersDelight 1.21.1 的 jar 里**根本没有 pack.mcmeta**（NeoForge 会给 mod jar 生成 pack 元数据），所以最省事的是**直接删掉 pack.mcmeta**；若保留，写 `"pack_format": 34`。

---

## 3. Java API 对照总表

下面"旧"指 1.20.1 Forge，"新"指 1.21.1 NeoForge。**加 ⚠️ 的是会编译失败、必须改的点。**

### 3.1 注册（Registries / DeferredRegister）

| 旧 | 新 |
|---|---|
| `DeferredRegister.create(ForgeRegistries.ITEMS, MODID)` | `DeferredRegister.createItems(MODID)` → `DeferredRegister.Items`，返回 `DeferredItem<T>` |
| `DeferredRegister.create(ForgeRegistries.BLOCKS, MODID)` | `DeferredRegister.createBlocks(MODID)` → `DeferredBlock<T>` |
| `DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID)` | `DeferredRegister.create(Registries.MOB_EFFECT, MODID)`（`BuiltInRegistries.MOB_EFFECT` 也可） |
| `DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MODID)` | 删除（改用内置 `neoforge:add_table`） |
| `DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID)` | 不变 |
| `RegistryObject<Item>` | `DeferredItem<Item>` / `DeferredHolder<Item, Item>` |
| `RegistryObject<Block>` | `DeferredBlock<Block>` |
| `ITEMS.register("x", () -> new Item(...))` | `ITEMS.registerItem("x", Item::new, props)` 或 `ITEMS.register("x", () -> ...)`（仍可用） |
| 方块物品 | `ITEMS.registerSimpleBlockItem(BLOCK)` |
| 方块实体类型 | `DeferredRegister.createBlockEntityTypes(MODID)`；`BlockEntityType.Builder.of(MyBE::new, myBlock).build(null)` |

⚠️ `DeferredHolder` 实现了 `Holder<T>`，因此 `new MobEffectInstance(EffectRegistry.CURSED, 200, 0)` 可以直接传注册对象（旧版也类似，但 1.21 的 `MobEffectInstance` **所有构造器都要求 `Holder<MobEffect>`**，传 `MobEffect` 实例会编译失败）。

### 3.2 主类 / 事件总线 / 配置

| 旧 | 新 |
|---|---|
| `FMLJavaModLoadingContext.get().getModEventBus()` | 构造器参数注入：`public CataclysmDelicacies(IEventBus modEventBus, ModContainer modContainer)` |
| `ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_CONFIG)` | `modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC)` |
| `MinecraftForge.EVENT_BUS.register(this)` | `NeoForge.EVENT_BUS.register(this)` |
| `@Mod.EventBusSubscriber`（默认 FORGE 总线） | `@EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.GAME)`（`Bus.MOD` / `Bus.GAME`，**`FORGE` 已改名 `GAME`**） |
| `@SubscribeEvent` | 不变（`net.neoforged.bus.api.SubscribeEvent`） |
| `ModConfigEvent`（`net.minecraftforge.fml.event.config`） | `net.neoforged.fml.event.config.ModConfigEvent` |

### 3.3 食物 / 物品属性

| 旧 | 新 |
|---|---|
| `new FoodProperties.Builder().nutrition(n).saturationMod(s)` | `.nutrition(n).**saturationModifier**(s)` ⚠️ 方法改名 |
| `.effect(new MobEffectInstance(...), 1F)` | 不变（参数仍是 `MobEffectInstance` + `float`） |
| `.alwaysEat()` / `.meat()` / `.build()` | `.alwaysEdible()` /（`meat()` 已删除，1.21 改用 tag/组件语义，直接去掉）/ `.build()` |
| `usingConvertsTo(ItemLike)` | 1.21 新增，可替代部分 `craftRemainder` 语义 |
| `Item.Properties.craftRemainder(Item)` / `stacksTo` / `rarity` / `fireResistant` | 全部保留 |
| `Item.Properties.tab(...)` | 不存在，用 `CreativeModeTab` 的 `displayItems` |

旧 `CDFoodValues` 里用到 Cataclysm 的效果常量：
`ModEffect.EFFECTSTUN`、`EFFECTBLAZING_BRAND`、`EFFECTGHOST_SICKNESS` → **1.21.1 全部存在**（`DeferredHolder<MobEffect, MobEffect>`）。
FD 的 `ModEffects.NOURISHMENT` 现在是 `Holder<MobEffect>`，可直接进 `MobEffectInstance`。

### 3.4 NBT → 数据组件（重点）

1.21.1 的 `ItemStack` **没有** `getTag()` / `setTag()` / `hasTag()`。旧代码只有一处真正用 NBT：`CursiumKnifeItem`：

```java
// 旧
CompoundTag tag = itemStack.getTag();
tag.getInt("CDCursiumKnifeAddition");
tag.putInt("CDCursiumKnifeAddition", n);
tag.putLong("CDCursiumKnifeStartTick", tick);
```

推荐改成两个自定义组件（比塞 `CUSTOM_DATA` 更干净、能自动网络同步）：

```java
public static final DeferredRegister.DataComponents DATA_COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

public static final Supplier<DataComponentType<Integer>> KNIFE_CHARGE =
        DATA_COMPONENTS.registerComponentType("cursium_knife_charge",
                b -> b.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

public static final Supplier<DataComponentType<Long>> KNIFE_START_TICK =
        DATA_COMPONENTS.registerComponentType("cursium_knife_start_tick",
                b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

// 读：stack.getOrDefault(KNIFE_CHARGE.get(), 0)
// 写：stack.set(KNIFE_CHARGE.get(), n)
```

并把 `DATA_COMPONENTS.register(modEventBus)` 加进主类构造器。

如果只是想最小改动，也可以：

```java
CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
CompoundTag tag = data.copyTag();
stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
```

但注意：`CUSTOM_DATA` 的 `CompoundTag` 内容会随物品一起持久化/同步，性能与可读性都差一些；充能层数这类"玩法状态"强烈建议走自定义组件。

⚠️ 其余"看起来像 NBT"的地方其实是**实体持久数据**：`AttackEvents` 用 `entity.getPersistentData()` 存冷却时间戳（`CataclysmDelicaciesCursedDamageCD`、`CataclysmDelicaciesHasBeenLoot`、`CataclysmDelicaciesHasBeenAppliedBrand`）。**这个 API 在 1.21.1 NeoForge 仍然存在且签名不变**（返回 `CompoundTag`），可以原样保留。
更"NeoForge 风格"的替代是 `AttachmentType`，但不是必须。

### 3.5 事件（最容易出问题的一块）

⚠️ **NeoForge 1.21.1 删除了 `LivingAttackEvent` 和 `LivingHurtEvent`**（已在 `net.neoforged.neoforge.event.entity.living` 目录里逐个核对，只有 `LivingIncomingDamageEvent`、`LivingDamageEvent`、`LivingDeathEvent` 等）。

| 旧事件 | 新事件 | 差异 |
|---|---|---|
| `LivingAttackEvent`（可取消，最早） | `LivingIncomingDamageEvent`（可取消） | `getSource()`、`getAmount()`/`setAmount()`、`getOriginalAmount()`、`setCanceled(true)`；还带 `DamageContainer`、`addReductionModifier(...)`、`setInvulnerabilityTicks(int)` |
| `LivingHurtEvent`（pre-armor，可改数值） | 同上 `LivingIncomingDamageEvent` | 用 `event.setAmount(newAmount)` 累加/乘算伤害 |
| （无对应） | `LivingDamageEvent.Pre`（post-armor、pre-health） | `getNewDamage()` / `setNewDamage(float)`；`LivingDamageEvent.Post` 只读 |
| `LivingDeathEvent` | 不变 | `getSource()`、可 `setCanceled(true)`（复活类逻辑） |
| `LivingDropsEvent` | 不变 | `getDrops()` 返回 `Collection<ItemEntity>` |
| `PlayerInteractEvent.EntityInteract` | 不变 | `getTarget()` / `getItemStack()` / `getEntity()` / `setCancellationResult(...)` |
| `MobEffectEvent.Applicable` | 不变但结果类型变了 | ⚠️ `event.setResult(Event.Result.DENY)` → `event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY)`（枚举：`APPLY` / `DEFAULT` / `DO_NOT_APPLY`） |

`AttackEvents` 的具体映射建议：

1. `onLivingAttack`（烈焰灼烧把火焰伤害转成魔法伤害）→ `LivingIncomingDamageEvent`：
   ```java
   if (event.getSource().is(DamageTypeTags.IS_FIRE)) {
       event.setCanceled(true);
       entity.hurt(entity.level().damageSources().magic(), event.getAmount());
       // 播放音效...
   }
   ```
   注意：1.21 里"取消后再 hurt"要防止递归/无敌帧，建议用 `entity.invulnerableTime = 0` 或直接 `setAmount` + 改 source 实现（更稳）。
2. `onLivingHurt`（诅咒增伤、BlazeScorching 倍率、炼狱吐息施加灼烧、复生之牙掉落、末渊珍珠刀掉落）→ 全部搬到 `LivingIncomingDamageEvent`，用 `event.setAmount(event.getAmount() + extra)` / `* (amp+1)`。
3. `onEntityLoot`（Abyssal/Ignitium 刀的额外掉落）→ 保留在 `LivingDropsEvent`。
4. `LivingDeathEvent` 的"玛勒迪克图斯化身复活"→ 保留，`event.setCanceled(true)` 语义不变。

### 3.6 伤害 / 附魔 / 属性 / Tier

| 旧 | 新 |
|---|---|
| `new ForgeTier(3, 1750, 8.0F, 0.0F, 10, BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(...))` | `new SimpleTier(BlockTags.NEEDS_DIAMOND_TOOL, 1750, 8.0F, 0.0F, 10, () -> Ingredient.of(...))`（`net.neoforged.neoforge.common.SimpleTier`，**参数顺序变了**：tag 提前到第一个，等级 `level` 参数被删除） |
| `Tier` 是 class（`ForgeTier`） | `Tier` 是 **interface**；`getIncorrectBlocksForDrops()` 取代 `getLevel()` 语义 |
| `EnchantmentHelper.getDamageBonus(stack, target.getMobType())` | `EnchantmentHelper.modifyDamage(ServerLevel level, ItemStack stack, Entity target, DamageSource source, float baseDamage)` ⚠️ |
| `getAttributeModifiers(EquipmentSlot, ItemStack)`（返回 `Multimap`） | `public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack)`（NeoForge 新签名，旧无参版 deprecated） |
| `AttributeModifier(UUID, name, amount, Operation)` | `new AttributeModifier(ResourceLocation id, double amount, AttributeModifier.Operation op)` ⚠️ ID 从 UUID 变 `ResourceLocation`（1.21 全局改动） |
| `ToolActions.SHIELD_BLOCK` | `ItemAbilities.SHIELD_BLOCK` |
| `ItemStack.canPerformAction` | 不变 |

Cursium 刀的属性加成示例：

```java
private static final ResourceLocation CRITICAL_ID =
        ResourceLocation.fromNamespaceAndPath(MODID, "cursium_knife_critical");

@Override
public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
    ItemAttributeModifiers.Builder b = ItemAttributeModifiers.builder();
    super.getDefaultAttributeModifiers(stack).modifiers()
         .forEach(e -> b.add(e.attribute(), e.modifier(), e.slot()));
    b.add(ModAttribute.ADDITIONAL_CRITICAL_DAMAGE,
          new AttributeModifier(CRITICAL_ID, getCharge(stack) * POWER_ADD, AttributeModifier.Operation.ADD_VALUE),
          EquipmentSlotGroup.MAINHAND);
    return b.build();
}
```

> 旧代码 `import static com.github.L_Ender.cataclysm.items.Cataclysm_Weapon_Item.BASE_CRITICAL_ID;` **必须删**：Cataclysm 1.21.1 里 `items/Cataclysm_Weapon_Item.class` 已不存在（改名 `Cataclysm_Weapon.class`），也没有 `BASE_CRITICAL_ID` 常量（全 jar 搜过）。自己定义 `ResourceLocation` 即可，注意每把武器 ID 唯一，否则属性修改器会互相覆盖。

**刀的伤害/攻速怎么给**（FD 1.3.4 的 `KnifeItem` 只有 `(Tier, Properties)` 构造，旧的 `(Tier, 6.0F, -2.2F, props)` 没了）：

```java
public static final Tier WITHERITE = new SimpleTier(BlockTags.NEEDS_DIAMOND_TOOL, 1750, 8.0F, 0.0F, 10,
        () -> Ingredient.of(ModItems.WITHERITE_INGOT.get()));

new WitheriteKnifeItem(WITHERITE, new Item.Properties()
        .attributes(KnifeItem.createAttributes(WITHERITE, 6.0F, -2.0F)));
```

`DiggerItem.createAttributes(Tier, float attackDamage, float attackSpeed)` 在 1.21.1 是 `public static`（已验证），内部会加 `ATTACK_DAMAGE = attackDamage + tier.getAttackDamageBonus()` 与 `ATTACK_SPEED`。旧代码里 `ForgeTier` 的 `attackDamageBonus` 是 0，所以数值等价。

### 3.7 Block / BlockEntity / 交互

⚠️ 旧的 `Block#use(BlockState, Level, BlockPos, Player, InteractionHand, BlockHitResult)` **被拆成两个方法**：

```java
// 手持物品右键方块
protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
        BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit);

// 空手右键方块（`RemnantSkullWithSteakBlock` 取餐走这里）
protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
        Player player, BlockHitResult hit);
```

`InteractionResult` 枚举在 1.21.1 变成：`SUCCESS`、`SUCCESS_NO_ITEM_USED`、`CONSUME`、`CONSUME_PARTIAL`、`PASS`、`FAIL`；另有一套 `ItemInteractionResult`：`SUCCESS`、`SUCCESS_SERVER`、`CONSUME`、`FAIL`、`PASS`。旧代码里 `InteractionResult.SUCCESS`/`PASS` 仍存在，但**方块交互方法的返回值类型**要对上（`useItemOn` → `ItemInteractionResult`）。

| 旧 | 新 |
|---|---|
| `BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK)` | `Properties.ofFullCopy(Blocks.IRON_BLOCK)` ⚠️（另有 `ofLegacyCopy`） |
| `BlockEntityType` 的 `validBlocks` 修改（`BlockEntityTypeAccessor` mixin） | 建议**改成自建 BE 类型**（见第 6 节），从而删掉该 mixin |
| `getTicker(Level, BlockState, BlockEntityType<T>)` | 不变（`EntityBlock` 接口方法签名未变） |
| `createTickerHelper(type, myType, ticker)` | 不变 |
| `updateShape(...)` / `playerWillDestroy(...)` / `getStateForPlacement(...)` / `PushReaction.DESTROY` / `Block.UPDATE_ALL` / `Block.UPDATE_SUPPRESS_DROPS` | 均不变（`playerWillDestroy` 仍是 `public BlockState playerWillDestroy(Level, BlockPos, BlockState, Player)`） |
| `HorizontalDirectionalBlock.FACING` | 不变 |
| `Block.box(...)`、`VoxelShape` | 不变 |

### 3.8 杂项

| 旧 | 新 |
|---|---|
| `new ResourceLocation(ns, path)` / `new ResourceLocation(str)` | ⚠️ **没有构造器**：`ResourceLocation.fromNamespaceAndPath(ns, path)`、`ResourceLocation.parse(str)`、`ResourceLocation.withDefaultNamespace(str)` |
| `MobEffectUtil.formatDuration(effect, 1.0F)` | `formatDuration(MobEffectInstance, float durationFactor, float ticksPerSecond)`（3 参）⚠️；FD 的 `TextUtils.addFoodEffectTooltip(ItemStack, Consumer<Component>, float, float)` 可以省事 |
| `MathUtils.RAND`（FD） | 1.21.1 是 `java.util.Random`（不是 `RandomSource`），`nextInt` 等用法一致 |
| `TextUtils.tooltip(...)` / `TextUtils.block(...)` | 不变 |
| `Item.getUseDuration(ItemStack)` | `getUseDuration(ItemStack, LivingEntity)` ⚠️ |
| `onUseTick(Level, LivingEntity, ItemStack, int)` / `releaseUsing(...)` / `getUseAnimation(ItemStack)` / `use(Level, Player, InteractionHand)` | 签名不变（`use` 仍返回 `InteractionResultHolder<ItemStack>`） |
| `FakePlayerFactory.getMinecraft(ServerLevel)` | 不变（`net.neoforged.neoforge.common.util.FakePlayerFactory`） |
| `level.getRecipeManager().getRecipesFor(type, wrapper, level)` | 仍存在（`RecipeManager#getRecipesFor`）；单结果用 `getRecipeFor(type, input, level)` |
| `recipe.getResultItem(level.registryAccess())` | `getResultItem(HolderLookup.Provider)`，`level.registryAccess()` 仍可用 |
| 熔炼查询 `new SimpleContainer(stack)` | 仍可用（`SimpleContainer` 还在）；更规范的是 `new SingleRecipeInput(stack)` |
| `ItemStack.isSameItem` / `getHoverName` / `is(TagKey)` | 不变 |
| `ServerLevel.getLootData().getLootTable(id)` | ⚠️ 改为 `level.getServer().reloadableRegistries().getLootTable(ResourceKey<LootTable>)` |
| `LivingEntity.getLootTable()` | 返回 `ResourceKey<LootTable>`（旧是 `ResourceLocation`）⚠️ |
| `level.getServer().getLootData()` | 不存在了；用 `reloadableRegistries()` |
| `Entity.level().damageSources()` | 不变 |
| `DamageTypeTags.IS_FIRE` / `WITCH_RESISTANT_TO` / `BYPASSES_INVULNERABILITY` | 不变 |
| `MobEffects.*` | 字段类型是 `Holder<MobEffect>`（旧是 `MobEffect`）⚠️ 传参处要注意 |
| `entity.getActiveEffectsMap()` | 仍返回 `Map<Holder<MobEffect>, MobEffectInstance>`；移除单个效果用 `entity.removeEffect(Holder<MobEffect>)` |

---

## 4. 依赖 Mod 的 API 变化

### 4.1 FarmersDelight 1.3.4（1.21.1）— 全部实际核对过

| 用到的 FD API | 1.21.1 状态 |
|---|---|
| `ConsumableItem(Properties)`、`(Properties, boolean)`、`(Properties, boolean, boolean)` | ✅ 三个构造都在 |
| `ConsumableItem.finishUsingItem / affectConsumer / appendHoverText(ItemStack, Item.TooltipContext, List<Component>, TooltipFlag)` | ✅（`appendHoverText` 第二参是 `TooltipContext`，不是 `Level`） |
| `DrinkableItem` 同构造 + `getUseAnimation(ItemStack)`、`getUseDuration(ItemStack, LivingEntity)`、`use(...)` | ✅ |
| `KnifeItem(Tier, Properties)` | ✅ 只剩这一个构造；旧的 `(Tier, float, float, Properties)` ❌ |
| `KnifeItem.KNIFE_DIG / KNIFE_HARVEST / KNIFE_ACTIONS` | ✅（类型 `ItemAbility` / `Set<ItemAbility>`） |
| `KnifeItem.hurtEnemy / postHurtEnemy / canPerformAction / useOn / canAttackBlock / supportsEnchantment / isPrimaryItemFor` | ✅ |
| `PlaceableItem(Block, Properties)` | ✅ |
| `FeastBlock(Properties, Supplier<Item>, boolean)` 与 4 参版 | ✅ 两个构造都在（`FeastBlock` 仍是 `Supplier<Item>`） |
| `CuttingBoardBlock.useItemOn(...) -> ItemInteractionResult` | ✅ |
| `CuttingBoardBlockEntity(BlockPos, BlockState)`、`processStoredItemUsingTool(ItemStack, Player)` | ✅ |
| `CuttingBoardBlockEntity` 内部 `lambda$processStoredItemUsingTool$2(ItemStack, Player, RecipeHolder)` | ✅ 仍存在（`CuttingBoardBlockEntityMixin` 目标还有效） |
| `CuttingBoardRecipe.rollResults(RandomSource, int, RecipeWrapper)` | ✅ 描述符不变（`net.neoforged.neoforge.items.wrapper.RecipeWrapper`） |
| `CookingPotBlockEntity.processCooking(RecipeHolder<CookingPotRecipe>, ...)` + `CookingPotRecipe.getCookTime()` | ✅（`CookingPotBlockEntityMixin` 目标有效） |
| `SkilletBlock.getSkilletCookingTime(int, int)` | ✅ static（`SkilletBlockEntityMixin` 目标有效） |
| `ModBlockEntityTypes.CUTTING_BOARD`、`CABINET` | ✅（`Supplier<...>`） |
| `ModItems.CUTTING_BOARD / IRON_KNIFE / DOG_FOOD`、`ModBlocks.CUTTING_BOARD` | ✅ |
| `ModRecipeTypes.CUTTING` | ✅ |
| `ModTags.Blocks.MINEABLE_WITH_KNIFE`、`ModTags.EntityTypes.DOG_FOOD_USERS` | ✅（`vectorwing.farmersdelight.common.tag.ModTags`） |
| `Configuration.ENABLE_FOOD_EFFECT_TOOLTIP` | ✅ `Supplier<Boolean>` |
| `ModEffects.NOURISHMENT` | ✅ `Holder<MobEffect>` |
| `TextUtils.tooltip/block/addFoodEffectTooltip` | ✅ |
| `MathUtils.RAND` | ✅ 但是 `java.util.Random` |
| `FakePlayer`（NeoForge）| `net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(ServerLevel)` |
| `net.minecraftforge.items.wrapper.RecipeWrapper` | `net.neoforged.neoforge.items.wrapper.RecipeWrapper` |
| `net.minecraftforge.items.IItemHandler(Modifiable)` | `net.neoforged.neoforge.items.IItemHandler(Modifiable)` |

### 4.2 Lendercataclysm 1.21.1（build 8931182）— 逐符号核对

| 旧符号 | 1.21.1 状态 |
|---|---|
| `ModEffect.EFFECTSTUN / EFFECTBLAZING_BRAND / EFFECTGHOST_SICKNESS / EFFECTGHOST_FORM / EFFECTABYSSAL_FEAR` | ✅ 全部存在，类型 `DeferredHolder<MobEffect, MobEffect>` |
| `ModParticle.PHANTOM_EMITTER / CURSED_ALGIZ / PHANTOM_WING_FLAME` | ✅ 存在，`DeferredHolder<ParticleType<?>, SimpleParticleType>`（可直接当 `ParticleOptions` 用 `.get()`） |
| `ModSounds.PHANTOM_SPEAR / EMP_ACTIVATED / SHREDDER_END / MALEDICTUS_SHORT_ROAR` | ✅ 存在，`DeferredHolder<SoundEvent, SoundEvent>` |
| `ModAttribute.ADDITIONAL_CRITICAL_DAMAGE` | ✅ `DeferredHolder<Attribute, Attribute>` |
| `CMDamageTypes.EMP`（`ResourceKey<DamageType>`）、`getDamageSource(Level, ResourceKey<DamageType>, EntityType<?>...)`、`causeShredderDamage(LivingEntity)` | ✅ 全部存在 |
| `AttributeUtils.OriginDamage(LivingEntity, ItemStack) -> float` | ✅ |
| `ScreenShake_Entity.ScreenShake(Level, Vec3, float radius, float magnitude, int duration, int fade)` | ✅ 签名不变 |
| `RingParticleOptions(float,float,int,int,int,int,float,float,boolean,int)` | ✅ 10 参构造还在（旧调用那串 10 个实参可以直接用） |
| `ParryParticleOptions(float,float,float)` | ✅ |
| `IABoss_monster.DamageCap() -> float`、`hurt(...)` | ✅ |
| `LLibrary_Boss_Monster.hurt / DamageCap()` | ✅ |
| `Maledictus_Entity.getRageMeter()/setRageMeter(int)` | ✅ |
| `Cataclysm.PROXY.playWorldSound(Object, byte)`、`clearSoundCacheFor(Entity)` | ✅（`PROXY` 是 `ServerProxy`） |
| `ModItems.WITHERITE_INGOT / CURSIUM_INGOT`、`ModBlocks.ALTAR_OF_FIRE` | ✅ |
| `items.Cataclysm_Weapon_Item.BASE_CRITICAL_ID` | ❌ **类被删/改名**（`Cataclysm_Weapon`），常量不存在 → 自己定义 `ResourceLocation` |
| 实体类 `Ignis_Entity` / `Ancient_Remnant_Entity` / `The_Harbinger_Entity` / `Maledictus_Entity` | ✅ 包路径仍为 `com.github.L_Ender.cataclysm.entity...`（`IABoss_monsterMixin` 里的 `instanceof` 判断要按新包名核对） |

> Cataclysm 1.21.1 的武器属性有现成工具：`Cataclysm_Weapon.createAttributes(float, float, AttributeContainer...)` / `createAttributes(int, float)`，如果想让自定义刀和 Cataclysm 武器表现一致，可以优先用它们而不是 `DiggerItem.createAttributes`。

---

## 5. 数据包 / 资源迁移（最容易被漏，必须逐项过）

### 5.1 目录改名

| 旧路径 | 1.21.1 路径 |
|---|---|
| `data/<ns>/recipes/**` | `data/<ns>/recipe/**` |
| `data/<ns>/loot_tables/**` | `data/<ns>/loot_table/**` |
| `data/<ns>/tags/items/**` | `data/<ns>/tags/item/**` |
| `data/<ns>/tags/blocks/**` | `data/<ns>/tags/block/**` |
| `data/<ns>/tags/entity_types/**` | `data/<ns>/tags/entity_type/**` |
| `data/<ns>/tags/mob_effect/**` | 不变 |
| `data/forge/loot_modifiers/global_loot_modifiers.json` | `data/neoforge/loot_modifiers/global_loot_modifiers.json` |
| `data/<ns>/loot_modifiers/*.json` | 不变（但见 5.4） |

### 5.2 ItemStack JSON：`item` → `id` ⚠️

旧：

```json
"result": { "item": "cataclysm_delicacies:high_energy_stew", "count": 1 }
```

新：

```json
"result": { "id": "cataclysm_delicacies:high_energy_stew", "count": 1 }
```

凡出现"物品堆"的地方都要改（合成结果、熔炉/烟熏结果、`cutting` 的 `result`、战利品表的 `LootItem` 等）。
注意**配方原料**仍是 `{"item": "..."}` / `{"tag": "..."}`（Ingredient 的格式没变），别把原料也改了。

### 5.3 各配方类型的具体格式（对照 FD 1.21.1 的真实文件）

**合成（shaped）**

```json
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "key": { "#": { "item": "cataclysm:black_steel_ingot" }, "K": { "tag": "c:ingots/cursium" } },
  "pattern": [" #", "#K", " I"],
  "result": { "count": 1, "id": "cataclysm_delicacies:cursium_knife" }
}
```

- 新增可选 `category`（`equipment`/`misc`/`building`/`redstone` 等）。
- `group`、`show_notification` 仍可用。

**FD 烹饪（`farmersdelight:cooking`）**——旧格式基本兼容，只有 result 的 `item`→`id`：

```json
{
  "type": "farmersdelight:cooking",
  "container": { "item": "minecraft:bowl" },
  "cookingtime": 400,
  "experience": 5.0,
  "ingredients": [
    { "item": "cataclysm_delicacies:energy_storage_tank" },
    { "tag": "c:foods/raw_bacon" }
  ],
  "recipe_book_tab": "meals",
  "result": { "count": 1, "id": "cataclysm_delicacies:energy_curry_over_rice" }
}
```

⚠️ 标签全部从 `forge:*` 改 `c:*`：`forge:crops/rice` → `c:crops/rice`、`forge:raw_fishes/salmon` → `c:foods/raw_fish/salmon`（NeoForge 1.21 的 common tag 命名规则变化较大，**以 FD/Cataclysm 1.21.1 jar 里实际使用的标签为准**，逐个 grep 替换）。

**FD 切割（`farmersdelight:cutting`）**——结构变化最大：

旧：

```json
{
  "type": "farmersdelight:cutting",
  "ingredients": [{ "item": "..." }],
  "result": [{ "count": 4, "item": "..." }],
  "tool": { "tag": "forge:tools/knives" }
}
```

新（FD 1.21.1 真实文件）：

```json
{
  "type": "farmersdelight:cutting",
  "ingredients": [{ "item": "cataclysm_delicacies:cursed_ectoplasm_block" }],
  "result": [
    { "item": { "count": 4, "id": "cataclysm_delicacies:cursed_ectoplasm" } }
  ],
  "tool": [
    { "type": "farmersdelight:item_ability", "action": "knife_dig" },
    { "tag": "farmersdelight:tools/knives" }
  ]
}
```

要点：
1. `result` 的元素是 `{"item": <ItemStack>}`（ItemStack 内部才是 `id`/`count`），**不是** `{"item": "...", "count": 4}`。
2. `tool` 从单对象变成**数组**，可混用 `{"tag": ...}` 与 `{"type":"farmersdelight:item_ability","action":"..."}`。
3. 新增可选 `sound`（`{"sound_id": "minecraft:item.axe.strip"}`）与 `chance` 支持。

**Cataclysm 武器融合（`cataclysm:weapon_fusion`）**：仍存在（Cataclysm 1.21.1 的 `recipe/weapon_infusion/` 目录里能查到），只有 result 的 `item`→`id`：

```json
{
  "type": "cataclysm:weapon_fusion",
  "base": { "item": "farmersdelight:cutting_board" },
  "addition": { "item": "cataclysm_delicacies:witherite_knife" },
  "result": { "id": "cataclysm_delicacies:mechanical_cutting_board" }
}
```

**熔炼/烟熏**：`{"type":"minecraft:smelting","ingredient":{...},"result":{"id":"...","count":1},"experience":0.35,"cookingtime":200}`（result 用 `id`）。

### 5.4 全局战利品修饰器（建议直接删代码）

旧：7 个 `data/cataclysm_delicacies/loot_modifiers/add_to_*.json` + 自定义 `add_loot_table` codec + `data/forge/loot_modifiers/global_loot_modifiers.json`。

新：**改用 NeoForge 内置 `neoforge:add_table`**，字段 `table`（不再是 `lootTable`），条件 `neoforge:loot_table_id`：

```json
{
  "type": "neoforge:add_table",
  "conditions": [
    { "condition": "neoforge:loot_table_id", "loot_table_id": "cataclysm:entities/ignis" }
  ],
  "table": "cataclysm_delicacies:ignis_delight_loot"
}
```

`data/neoforge/loot_modifiers/global_loot_modifiers.json`：

```json
{ "replace": false, "entries": ["cataclysm_delicacies:add_to_ignis", "..."] }
```

于是可以删除：`loot/AddLootModifier.java`、`loot/LootModifier.java`、`init/LootModifierRegistry.java`。

> 如果以后确实需要自定义 loot modifier：`IGlobalLootModifier#codec()` 现在返回 `MapCodec<? extends IGlobalLootModifier>`，基类 `LootModifier.codecStart(RecordCodecBuilder.Instance<T>)` 已经内置 `conditions` 字段，`apply()` 是 `final`。

### 5.5 标签

| 旧文件 | 新文件 | 内容改动 |
|---|---|---|
| `data/cataclysm/tags/items/explosion_immune_item.json` | `data/cataclysm/tags/item/explosion_immune_item.json` | 只改路径（Cataclysm 1.21.1 仍有 `cataclysm:explosion_immune_item` 标签） |
| `data/cataclysm/tags/mob_effect/effective_for_bosses.json` | 同路径不变 | — |
| `data/farmersdelight/tags/blocks/heat_sources.json` | `data/farmersdelight/tags/block/heat_sources.json` | 追加 `cataclysm_delicacies:mechanical_cutting_board` 等（如需） |
| `data/farmersdelight/tags/items/tools/knives.json` | `data/farmersdelight/tags/item/tools/knives.json` | 把 5 把刀加进去；注意 FD 1.21.1 该标签只列了 FD 自己的 5 把刀，`replace:false` 追加即可 |
| `data/forge/tags/items/tools/knives.json` | `data/c/tags/item/tools/knife.json`（NeoForge common tag） | `forge:` → `c:`，且路径单数化 |
| `data/minecraft/tags/blocks/mineable/pickaxe.json` | `data/minecraft/tags/block/mineable/pickaxe.json` | — |
| `data/cataclysm_delicacies/tags/mob_effect/ankh_blessing_immune.json` | 不变 | — |

### 5.6 模型 / 贴图 / 语言文件

- 方块模型 `_0.._4` 的 feast 风格模型结构 1.21 未变（`farmersdelight:template_feast_stage` 之类的 parent 是否还存在，需对照 FD 1.21.1 的 `assets/farmersdelight/models/block/template_*`，若改名要跟着改）。
- `textures/item/witherite_knife.png.mcmeta`（动态贴图）格式未变。
- `lang/en_us.json`、`zh_cn.json`：1.21 新增了一些通用键（如 `item.cataclysm_delicacies.x` 仍是同一套），**`itemGroup.<modid>` 保持不变**；效果描述键 `effect.<modid>.<name>` 也不变。
- 1.21 起 `assets/<ns>/models/item/*.json` 里 `"parent": "item/generated"` 仍有效。

---

## 6. Mixin 迁移清单

| 文件 | 目标是否仍存在 | 处理 |
|---|---|---|
| `mixin/BlockEntityTypeAccessor.java` | `BlockEntityType.validBlocks` 仍是 `private final Set<Block>` | **建议删除**：改为自定义 `BlockEntityType` + 继承 FD `CuttingBoardBlockEntity`（见 6.1） |
| `mixin/LootTableAccessor.java` | `LootTable.pools` 仍是 `private final List<LootPool>` | 保留（或换 AT） |
| `mixin/CuttingBoardBlockEntityMixin.java` | `lambda$processStoredItemUsingTool$2` 与 `CuttingBoardRecipe.rollResults(RandomSource,int,RecipeWrapper)` 均存在 | 保留；`remap = false` 保留 |
| `mixin/CookingPotBlockEntityMixin.java` | `processCooking` 与 `CookingPotRecipe.getCookTime()I` 存在 | 保留（注意 `processCooking` 现在参数是 `RecipeHolder<CookingPotRecipe>`） |
| `mixin/SkilletBlockEntityMixin.java` | `SkilletBlock.getSkilletCookingTime(II)I` 存在 | 保留 |
| `mixin/LivingEntityMixin.java` | `getDamageAfterArmorAbsorb(DamageSource, float)` 仍是 LivingEntity 的私有方法 | 保留；但 1.21 的伤害流程改成了 `DamageContainer`，**需确认该方法仍被调用**（若不调用，改为在 `LivingIncomingDamageEvent.addReductionModifier` 里实现，语义更好） |
| `mixin/MinecraftMixin.java` | `Minecraft.rightClickDelay`（`private int`）存在 | 保留 |
| `mixin/IABoss_monsterMixin.java` | `IABoss_monster.DamageCap()F`、`hurt`、`tick` 都在 | 保留；`DamageSource.is(TagKey)` 的 ordinal 需按新源码重新确认 |
| `mixin/LLibraryBossMonsterMixin.java` | `LLibrary_Boss_Monster.hurt/DamageCap` 在 | 保留，同上 |

### 6.1 推荐去掉 `BlockEntityTypeAccessor`（机械切菜板）

旧做法：在 `commonSetup` 里 `applyBlockEntity(FD 的 CUTTING_BOARD 类型, 自己的方块)`，靠 `@Mutable @Accessor("validBlocks")` 往 `validBlocks` 里塞方块。

1.21.1 更干净的做法：

```java
// 自己注册 BE 类型
public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.createBlockEntityTypes(MODID);

public static final Supplier<BlockEntityType<MechanicalCuttingBoardEntity>> CUTTING_BOARD =
        BLOCK_ENTITY_TYPES.register("mechanical_cutting_board",
                () -> BlockEntityType.Builder
                        .of(MechanicalCuttingBoardEntity::new, BlockRegistry.MECHANICAL_CUTTING_BOARD.get())
                        .build(null));

// BE 继承 FD 的实现，复用 processStoredItemUsingTool
public class MechanicalCuttingBoardEntity extends CuttingBoardBlockEntity {
    public MechanicalCuttingBoardEntity(BlockPos pos, BlockState state) { super(pos, state); }
}

// 方块里覆写
@Override
public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new MechanicalCuttingBoardEntity(pos, state);
}
```

⚠️ 风险点：`CuttingBoardBlockEntity` 的构造器只有 `(BlockPos, BlockState)`（BE 类型由注册表反查），所以子类必须保持这个构造；FD 的 `processStoredItemUsingTool` 是 public，可复用。若 FD 内部对 BE 类型做强转（`(CuttingBoardBlockEntity) this.level.getBlockEntity(pos)` 之类），一般兼容。

---

## 7. 建议施工顺序（每步都能 `runClient` 验证）

**阶段 0：工程骨架**
1. `build.gradle` 加 FD / Cataclysm / Curios / Lionfish 依赖；删 mixin 插件与 mixinextras 依赖；开 JEI（可选）。
2. `gradle.properties` 改 mod 元数据；`neoforge.mods.toml` 写好 4 个依赖 + `[[mixins]]` + `[[accessTransformers]]`。
3. 把旧源码整包拷进 `src/main/java/io/github/rcneg/cataclysmdelicacies/`，先让 `gradle compileJava` 报错清单出来当 TODO 列表（这一步能一次性暴露 80% 的 API 变更）。

**阶段 1：注册层**
4. `ItemRegistry`/`BlockRegistry`/`EffectRegistry`/`TabRegistry` 改 `DeferredRegister.*` 与 `DeferredItem/DeferredBlock`。
5. `CDFoodValues` 改 `saturationModifier`、去掉 `meat()`、效果用 `Holder`。
6. 删 `LootModifierRegistry`、`loot/*`，改 7 个 JSON 为 `neoforge:add_table`。
7. `CataclysmDelicacies` 主类构造器化；`Config` 改 `ModConfigSpec` + `modContainer.registerConfig`。

**阶段 2：物品**
8. 5 把刀：`SimpleTier` + `Item.Properties.attributes(KnifeItem.createAttributes(...))`；`getDefaultAttributeModifiers(ItemStack)`；`EnchantmentHelper.modifyDamage`。
9. `CursiumKnifeItem`：自定义组件替换 NBT；删 `Cataclysm_Weapon_Item.BASE_CRITICAL_ID`。
10. `AncientDogFoodItem` / `EnergyDrinkItem` / `CursedDrinkItem`：`appendHoverText` 的 `TooltipContext`、`MobEffectEvent.Applicable.Result`、`MobEffectUtil.formatDuration` 3 参。

**阶段 3：方块与 BE**
11. `MechanicalCuttingBoard`：自建 BE 类型（6.1），删 `applyBlockEntity` hack。
12. `RemnantSkullWithSteakBlock`：旧 `use(...)` 拆成 `useWithoutItem` / `useItemOn`；`Properties.ofFullCopy`。
13. `ClientEvents` 的 `ItemBlockRendererTypes.setRenderLayer` → 1.21 的 `ItemBlockRenderTypes.setRenderLayer`（类名大小写变了：`ItemBlockRenderTypes`）。

**阶段 4：事件**
14. `AttackEvents`：`LivingAttackEvent`/`LivingHurtEvent` → `LivingIncomingDamageEvent`；战利品表查询改 `reloadableRegistries()`；`LootItem.item` 变 `Holder<Item>`。
15. `EffectEvents`：`Applicable.Result`；`removeEffect(Holder)`。

**阶段 5：Mixin**
16. 逐个确认目标（第 6 节表），去掉可获得替代的 mixin（`BlockEntityTypeAccessor`）。
17. `mixins.json` 改 `JAVA_21`、去 refmap。

**阶段 6：数据包**
18. 批量目录改名（5.1）。
19. 批量 `item`→`id`（5.2）——建议写个脚本对所有 `data/**/*.json` 做正则替换，只改 `"result"`/`"item": {` 里作为物品堆的位置，人工复核。
20. `cutting` 配方改 `result`/`tool` 结构（5.3）。
21. 标签 `forge:`→`c:`、`tools/knives` 追加 5 把刀、`global_loot_modifiers` 移位。

**阶段 7：资源与收尾**
22. 删 `pack.mcmeta`（或改 34）。
23. 语言文件补新键；JEI 插件（若启用）改 1.21.1 JEI 19 API。
24. `runData`（如需要）核对生成物；`runClient` 实测：切菜板自动化、骷髅头取餐、5 把刀被动、4 个自定义效果、7 个 Boss 额外掉落。

---

## 8. 需要在编译/运行时确认的点（不要盲信本文的推测）

1. `ItemBlockRenderTypes`（旧 `ItemBlockRendererTypes`）的确切类名与 `setRenderLayer` 签名。
2. `LivingEntityMixin` 里 `getDamageAfterArmorAbsorb` 在 1.21.1 是否仍被调用（伤害流程已改 `DamageContainer`）；更稳的实现是 `LivingIncomingDamageEvent#addReductionModifier` 或 `LivingDamageEvent.Pre`。
3. `IABoss_monsterMixin` 中 `DamageSource.is(TagKey)` 的 `ordinal` 索引（1.21.1 的 `hurt` 实现变了）。
4. `FeastBlock` 相关模型 parent（`farmersdelight:block/template_feast_stage` 是否改名）。
5. `c:` 标签的准确子路径（`c:foods/raw_fish/salmon` vs `c:foods/raw_fish` 之类的层级），以 FD 1.21.1 jar 内的 `data/*/tags/item/**` 为准。
6. JEI 19（1.21.1）的 `IRecipeRegistration.addIngredientInfo` 签名是否仍接受 `(ItemStack, IIngredientType, Component...)`。
7. `CuttingBoardBlockEntity` 子类化是否与 FD 内部逻辑冲突（若冲突，回退到 `BlockEntityTypeAccessor` mixin 方案）。

---

## 9. 本机验证材料（下次直接读，不用重新下载）

| 路径 | 内容 |
|---|---|
| `.tmp_mc/` | 1.21.1 Minecraft 反编译源码（`net/minecraft/...`），用来查任何 vanilla API |
| `.tmp_ns/` | NeoForge 21.1.228 源码（`net/neoforged/neoforge/...`），查事件/注册/网络 |
| `.tmp_fd/` | FarmersDelight 1.3.4 jar 解包（`data/farmersdelight/**` 是最好的 1.21.1 数据包格式参考） |
| `.tmp_fd_api.txt` | FD 关键类的 `javap` 签名转储 |
| `.tmp_cat.jar` / `.tmp_cat_dir/` | Lendercataclysm 1.21.1 build 8931182 的 jar / 解包（`data/cataclysm/**` 可查武器融合格式与标签） |
| `.tmp_cat_api1.txt` | Cataclysm `ModEffect/ModParticle/ModSounds/ModAttribute/CMDamageTypes/AttributeUtils/ScreenShake_Entity/RingParticleOptions/ParryParticleOptions` 的 `javap` 转储 |
| `CataclysmDelicacies-master/` | 旧 1.20.1 源码（翻译对照用） |

常用查询命令（PowerShell）：

```powershell
# 查某个类的方法签名
javap -classpath .tmp_cat.jar -p com.github.L_Ender.cataclysm.init.ModEffect

# 查 1.21.1 某个 vanilla 方法
Select-String -Path .tmp_mc\net\minecraft\world\item\Item.java -Pattern "getUseDuration"

# 查 FD 的 1.21.1 JSON 写法
Get-ChildItem .tmp_fd\data\farmersdelight\recipe -Recurse -Filter *.json | Select-Object -First 5
```

---

## 附：给 AI 的使用提示

把这份文档 + `CataclysmDelicacies-master/` 源码一起喂给模型时，建议按以下顺序提问，效果最好：

1. "按第 7 节的阶段 0，先给我改好的 `build.gradle` 和 `neoforge.mods.toml`。"
2. "按第 3.1/3.2 节，重写 `ItemRegistry`、`BlockRegistry`、`EffectRegistry`、`TabRegistry`、`CataclysmDelicacies` 主类。"
3. "按第 3.4/3.6 节，重写 5 把刀。"
4. "按第 3.5 节，重写 `AttackEvents`。"
5. "按第 5 节，给我一个能把旧 `data/` 批量转成 1.21.1 格式的脚本。"
6. 遇到编译错误："我在 1.21.1 编译报错 XXX，请对照 `.tmp_mc`/`.tmp_fd_api.txt` 里已验证的签名修正。"

---

## 10. 实际移植记录（本工作区已完成，可当"踩坑清单"复用）

上面第 1~9 节是**动手前**的静态分析；本节是**真正改完之后**补的记录，只写"文档里没预料到 / 预料错了"的部分。

### 10.1 版本与工具链（三个硬坑）

| 问题 | 现象 | 解决 |
| --- | --- | --- |
| NeoForge 版本太低 | `neo_version=21.1.219` 启动即 FATAL：`Mod curios requires neoforge 21.1.265 or above`（curios-309927:9694505 的 mods.toml 版本区间） | 改成 `neo_version=21.1.267`（forgecdn 上 21.1.x 最新） |
| gradle wrapper 镜像 404 | `distributionUrl` 指向腾讯镜像，返回 989 字节 HTML，报 `zip END header not found` | 改成 `https://mirrors.huaweicloud.com/gradle/gradle-8.14.3-bin.zip`，并手动解压到 `~/.gradle/wrapper/dists/gradle-8.14.3-bin/<hash>/` |
| 客户端缺 assets | `DownloadAssets` 任务卡住 / 客户端 `NoSuchFileException: .../assets/objects/xx/<sha1>` | 从 `~/.gradle/caches/neoformruntime/assets/objects` **robocopy** 到 `~/.gradle/caches/minecraft/assets/objects`（同为 SHA1 目录结构），然后 `gradlew runClient -x "neoFormJoined1.21.1-20240808.144430DownloadAssets"` |

### 10.2 编译期的 63 个错误 → 6 类

| 错误 | 真正原因 | 修法 |
| --- | --- | --- |
| `非法字符: '\ufeff'` | Windows PowerShell `Set-Content -Encoding utf8` 会写 BOM | 用 python `io.open(..., 'w', encoding='utf-8', newline='\n')` 重写文件 |
| `找不到符号: getResultItem` | 1.21 `getRecipeFor` 返回 `Optional<RecipeHolder<T>>`，不是配方本体 | `holder.value().getResultItem(level.registryAccess()).copy()` |
| `Holder<SoundEvent> 无法转换为 SoundEvent` | `SoundEvents.ARMOR_EQUIP_GENERIC`、`SoundEvents.GENERIC_EXPLODE` 在 1.21 是 `Holder<SoundEvent>`（并非全部 SoundEvents 都变了） | 这两个加 `.value()`；`WOOD_BREAK / PLAYER_HURT_ON_FIRE / TOTEM_USE / GENERIC_EAT` 仍是普通 `SoundEvent`，不要加 |
| `找不到符号: setDamage` | `Item` 已无 `setDamage`；"不毁" 必须走组件 | `properties.component(DataComponents.UNBREAKABLE, new Unbreakable(true))` |
| `不兼容的类型: MobEffect 无法转换为 Holder<MobEffect>` | `MobEffectInstance` 全部构造器都吃 `Holder<MobEffect>` | `DeferredHolder` 本身就是 `Holder`，**不要 `.get()`**（`hasEffect` / `addEffect` / `removeEffect` / `getEffect` 同理） |
| `不兼容的类型: Object 无法转换为 ItemStack` | 变量声明成泛型擦除后的 `Object`（`RecipeHolder` 泛型推断失败） | 显式写 `RecipeHolder<SmeltingRecipe>` 或 lambda 形参改用 `holder.value()` |

补充两条**不会报错但会静默出错**的：

- `Item.toString()` 在 1.21 返回**带命名空间**的注册名（`cataclysm_delicacies:witherite_knife`），旧代码 `"tooltip.cataclysm_delicacies." + stack.getItem()` 拼出来的 key 找不到翻译。统一改成 `BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()`（本工作区放在 `ItemHelper.itemPath`）。
- `CMDamageTypes.getDamageSource(level, key, EntityType<?>...)` 是变参，旧代码的 `new EntityType[0]` 可以直接删。

### 10.3 运行期真实报错（编译期发现不了的）

| 现象 | 原因 | 修法 |
| --- | --- | --- |
| 启动 FATAL `class ...CataclysmDelicacies has no @SubscribeEvent methods, but register was called anyway` | NeoForge 的事件总线**拒绝**注册没有 `@SubscribeEvent` 的类（Forge 时代允许） | 主类里删掉 `NeoForge.EVENT_BUS.register(this)`（本 mod 主类确实没有任何事件方法） |
| 战利品表解析失败 `Unknown element name:killer` | 1.21 把 `LootContext.EntityTarget` 从 `killer` 改名为 `attacker`（现为 `this` / `attacker` / `direct_attacker` / `attacking_player`） | `"entity": "killer"` → `"attacker"` |
| 烹饪配方解析失败 `Not a JSON object: [{...},{...}]` | 1.21 `Ingredient.LIST_CODEC = MAP_CODEC_NONEMPTY.codec().listOf()`，**列表里的每个 ingredient 只能是对象**，1.20.1 那种"数组=多选一"写法被禁用 | 用 NeoForge 内置 `{"type":"neoforge:compound","children":[{...},{...}]}`（`CompoundIngredient.CODEC`，字段名 `children`，兼容别名 `ingredients`） |
| 战利品函数解析失败 | `minecraft:looting_enchant` 在 1.21 改名为 `minecraft:enchanted_count_increase`，且必须有 `enchantment` 字段 | `{"function":"minecraft:enchanted_count_increase","enchantment":"minecraft:looting","count":{...}}` |

顺带：启动日志里的 `Dev warning - Legacy Tags detected ... 'forge'` 不是本 mod 的问题（本工作区已删掉 `data/forge/`），是 Cataclysm/FD 自己还在带旧标签，可以忽略。

### 10.4 Mixin 实测：目标函数与序号**全都还在**

逐个用 `javap -c` 核对过字节码（jar 见第 9 节的 `.tmp_cat.jar` / FD 1.3.4），结论：

| Mixin | 目标 | 结论 / 需要的改动 |
| --- | --- | --- |
| `CuttingBoardBlockEntityMixin` | `lambda$processStoredItemUsingTool$2(ItemStack, Player, RecipeHolder)` | 函数还在；`rollResults` 描述符里的 `net/minecraftforge/items/wrapper/RecipeWrapper` 要改成 `net/neoforged/neoforge/items/wrapper/RecipeWrapper` |
| `CookingPotBlockEntityMixin` | `processCooking` → `CookingPotRecipe.getCookTime()I` | 原样可用 |
| `SkilletBlockEntityMixin` | `addItemToCook` → `SkilletBlock.getSkilletCookingTime(II)I` | 原样可用 |
| `LivingEntityMixin` | `getDamageAfterArmorAbsorb(DamageSource, float)` | 仍在（`LivingEntity.java:1713`）；只需去掉 `.get()` |
| `MinecraftMixin` | `rightClickDelay`（`Minecraft.java:356`）、`tick()`（`:1794`） | 原样可用 |
| `IABoss_monsterMixin` | `hurt` 里的 `DamageSource.is(TagKey)` 与 `DamageCap()F` | **ordinal 不用改**：`is` 出现 3 次（offset 14 = BYPASSES_INVULNERABILITY、137 = BYPASSES_HURT_TIME、227 = BLOCK_SELF_REGEN），`DamageCap()` 出现 2 次（offset 28 的 `Math.min`、152 的 bucket 逻辑）→ 旧 ordinal 0/1 依然对得上 |
| `LLibraryBossMonsterMixin` | 同上 | `hurt` 字节码布局与 IABoss 完全一致（14/28/137/152），ordinal 同样不变 |

两个 accessor mixin 直接删掉：

- `LootTableAccessor` → 改用 `accesstransformer.cfg` 里的 `public ...loot.LootTable pools`
- `BlockEntityTypeAccessor` → 改成"自己注册一个 `BlockEntityType`，让 `MechanicalCuttingBoardEntity.getType()` 返回它"，比改 FD 的 `validBlocks` 集合干净（1.21 的 `CuttingBoardBlockEntity.getInventory()` 已返回 `IItemHandler`，旧代码里 `(IItemHandlerModifiable)` 强转会直接抛 `ClassCastException`）

### 10.5 数据包迁移的最终形态

- 目录：`recipes→recipe`、`loot_tables→loot_table`、`tags/items→tags/item`、`tags/blocks→tags/block`（其余如 `tags/mob_effect`、`tags/entity_type` 本就是单数）
- 配方 `result` 一律 `{"id":"...","count":n}`；FD 切割 `result` 每项 `{"item":{"count":n,"id":"..."}}`（可选同级 `chance`），`tool` 变数组
- `forge:` → `c:` 标签映射（从 FD 1.21.1 自己的配方里读出来的真实映射）：`bacon→c:foods/raw_bacon`、`raw_fishes/salmon→c:foods/raw_salmon`、`raw_meat→c:foods/raw_meat`、`raw_mutton→c:foods/raw_mutton`、`crops/{onion,rice,tomato}→c:crops/*`、`eggs→c:eggs`、`milk→c:milk`、`mushrooms→c:mushrooms`、`bones→c:bones`、`pasta→c:foods/pasta`、`salad_ingredients→c:salad_ingredients`、`slimeballs→c:slime_balls`、`tools/knives→c:tools/knife`
- 战利品修饰器：`{"type":"neoforge:add_table","conditions":[{"condition":"neoforge:loot_table_id","loot_table_id":"..."}],"table":"..."}`，全局清单放 `data/neoforge/loot_modifiers/global_loot_modifiers.json`（NeoForge 只认这个路径，旧的 `data/forge/...` 不再被读）

### 10.6 验证套路（下次照抄）

1. `.\gradlew.bat compileJava --console=plain` → 必须 `BUILD SUCCESSFUL`
2. `.\gradlew.bat runServer --console=plain -x "neoFormJoined1.21.1-20240808.144430DownloadAssets"` → 日志里搜 `ERROR`：配方/战利品/tag 的解析错误都在这一步暴露；`Done (x.xxxs)!` 出现即注册表与数据包通过
3. `.\gradlew.bat runClient ...` → 日志里搜 `Unable to load model` / `Mixin apply failed` / `Critical injection failure`；`Created: 2048x1024x4 .../blocks.png-atlas` 出现说明方块模型全部烘焙成功
4. 注意：**同一个日志文件不能被两个 gradle run 同时写**（`Tee-Object` 会报 `文件正由另一进程使用`），每次验证换文件名或先确认旧进程退出

本次最终状态：`compileJava` 通过，服务端 `Done (6.143s)` 零数据包错误，客户端仅剩 `crocodile_tooth`、`lobster_head` 两个**原 mod 就没有模型/贴图/翻译**的遗留物品报 `Unable to load model`（它们在注册表里但不在创造标签页，也不被任何配方引用，可安全删除或补素材）。

### 10.7 与原文档结论的差异修正

- 原文档说"`LivingAttackEvent`/`LivingHurtEvent` 被删是最致命的问题" —— 属实，但真正改起来最省事的路径是：`LivingAttackEvent → LivingIncomingDamageEvent`（`setCanceled` + `setAmount`），`LivingHurtEvent → LivingDamageEvent.Pre`（`getNewDamage`/`setNewDamage`），逻辑可 1:1 平移，`AttackEvents` 的 241 行没有结构性改写。
- 原文档把 `InteractionResult.SUCCESS_NO_ITEM_USED` 列为 1.21 常量 —— **错**，1.21.1 的 `ItemInteractionResult` 只有 `SUCCESS / CONSUME / CONSUME_PARTIAL / PASS_TO_DEFAULT_BLOCK_INTERACTION / SKIP_DEFAULT_BLOCK_INTERACTION / FAIL`；方块交互用 `useItemOn`（返回 `ItemInteractionResult`），空手交互用 `useWithoutItem`（返回 `InteractionResult`）。
- 原文档没有提到的两个真实崩溃点：NeoForge 拒绝注册无 `@SubscribeEvent` 的类、`LootContext.EntityTarget` 的 `killer→attacker` 改名。

### 10.8 在 IDEA 里点 Run 起不来：两个 Gradle user home 打架

症状（IDEA 直接 Run 生成的 `BootstrapLauncher.main()` 配置）：

```
java.lang.IllegalStateException: Module named cpw.mods.bootstraplauncher was already on the JVMs
module path loaded from D:\new\.gradle\caches\...\bootstraplauncher-2.0.2.jar
but class-path contains it at location C:\Users\<user>\.gradle\caches\...\bootstraplauncher-2.0.2.jar
```

原因链条：

1. IDEA 的 **Gradle user home** 是 `D:\new\.gradle`（它生成的 run 配置里 `-p` 与 `--assetsDir` 全是 D:），而命令行 `gradlew` 没设 `GRADLE_USER_HOME`，默认用 `C:\Users\<user>\.gradle` —— 两套缓存各有同一个 jar，路径不同、内容相同。
2. NeoGradle 把"运行时 legacy classpath"写进 `build\neoForm\<...>\steps\writeMinecraftClasspathClient\classpath.txt`，再通过 `-DlegacyClassPath.file=` 传给 BootstrapLauncher。
3. 这个任务的 **up-to-date 校验只看输入文件的内容哈希，不看绝对路径**：换了一个 user home 后 jar 内容没变 → 任务被判 UP-TO-DATE 而跳过，文件里保留的是上一次那套 home 的绝对路径。
4. 于是 JVM 同时拿到"新的 `-p`（D:）"和"旧的 classpath.txt（C:）"，同一个模块出现在 module-path 与 class-path 两处 → 崩。

修法（二选一，**关键是让两边用同一个 home**）：

- 方案 A（推荐，跟着 IDE 走）：让命令行也用 IDE 的 home 重新生成一次
  ```powershell
  # 用 IDE 的 home 跑一次，classpath.txt 就会写成 D: 的路径
  .\gradlew.bat -g D:\new\.gradle runClient
  ```
  如果它仍然报同样的错，先删掉 `build\neoForm\<version>\steps\writeMinecraftClasspath*` 这几个目录，强制重新生成。
- 方案 B：把 IDEA 的 Gradle user home 改成 `C:\Users\<user>\.gradle`（Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle user home），然后 `Reload Gradle Project`。

**根治**：在这台机器上统一成一个 home。该工作区所有其他 NeoForge 工程都在用 `D:\new\.gradle`，所以建议命令行也加上 `-g D:\new\.gradle`，或者直接设用户环境变量 `GRADLE_USER_HOME=D:\new\.gradle`，这样 IDE 与命令行永远一致，第 3 条那种"内容没变所以跳过"的坑不会再出现。

顺带确认：这个 home 里已经有 `net.neoforged:neoforge:21.1.267`、`curse.maven:farmers-delight-398521:8765184`、`org.parchmentmc.data:parchment-1.21.1` 和完整的 `caches\minecraft\assets`，所以切过去不需要重新下载大文件。
