package io.github.rcneg.cataclysmdelicacies.items.knives;

import com.github.L_Ender.cataclysm.init.ModItems;
import io.github.rcneg.cataclysmdelicacies.helper.ItemHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.SimpleTier;
import vectorwing.farmersdelight.common.item.KnifeItem;

import java.util.List;

/**
 * 这个 mod 里所有刀的公共基类。
 * <p>
 * 1.21.1 的两个必改点：
 * <ul>
 *     <li>FD 的 KnifeItem 只剩 {@code KnifeItem(Tier, Item.Properties)}，攻击力/攻速必须走
 *         {@code Item.Properties#attributes(KnifeItem.createAttributes(...))}。</li>
 *     <li>Item 上已经没有 {@code setDamage(stack, -1)}，改用 UNBREAKABLE 组件实现"永不磨损"。</li>
 * </ul>
 */
public abstract class CDKnifeItem extends KnifeItem {

    /** 原来的 ForgeTier(3, 1750, 8F, 0F, 10, NEEDS_DIAMOND_TOOL, witherite_ingot) */
    public static final Tier TIER = new SimpleTier(BlockTags.NEEDS_DIAMOND_TOOL, 1750, 8.0F, 0.0F, 10,
            () -> Ingredient.of(ModItems.WITHERITE_INGOT.get()));

    protected CDKnifeItem(float attackDamage, float attackSpeed, Item.Properties properties) {
        this(TIER, attackDamage, attackSpeed, properties);
    }

    protected CDKnifeItem(Tier tier, float attackDamage, float attackSpeed, Item.Properties properties) {
        super(tier, properties
                .attributes(KnifeItem.createAttributes(tier, attackDamage, attackSpeed))
                .component(DataComponents.UNBREAKABLE, new Unbreakable(true)));
    }

    /** 决定这个刀会显示几行 tooltip，默认两行（path + path_1）。 */
    protected String[] tooltipSuffixes() {
        return new String[]{"", "_1"};
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        String path = ItemHelper.itemPath(stack);
        for (String suffix : this.tooltipSuffixes()) {
            tooltip.add(Component.translatable("tooltip.cataclysm_delicacies." + path + suffix).withStyle(ChatFormatting.DARK_GREEN));
        }
    }
}
