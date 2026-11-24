package io.github.rcneg.cataclysmdelicacies.init;

import io.github.rcneg.cataclysmdelicacies.CataclysmDelicacies;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;

public class CDTags {
    public static final TagKey<MobEffect> ANKH_BLESSING_IMMUNE = registerEffectTag("ankh_blessing_immune");

    private static TagKey<MobEffect> registerEffectTag(String name) {
        return TagKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(CataclysmDelicacies.MODID, name));
    }
}
