package io.github.rcneg.cataclysmdelicacies.init;

import com.mojang.serialization.Codec;
import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 1.20.1 里用 ItemStack NBT（"CDCursiumKnifeAddition" / "CDCursiumKnifeStartTick"）存的数据，
 * 1.21.1 全部改成数据组件。
 */
public class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CataclysmDelicacies.MODID);

    /** 冥晶刀叠加的充能层数（0~5） */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> KNIFE_CHARGE =
            DATA_COMPONENTS.registerComponentType("knife_charge",
                    builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** 上一次充能的时间点，用于按 CURSIUM_KNIFE_POWER_DURATION 衰减 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Long>> KNIFE_START_TICK =
            DATA_COMPONENTS.registerComponentType("knife_start_tick",
                    builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));
}
