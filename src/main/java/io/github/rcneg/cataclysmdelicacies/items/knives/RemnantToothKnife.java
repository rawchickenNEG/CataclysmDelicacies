package io.github.rcneg.cataclysmdelicacies.items.knives;

public class RemnantToothKnife extends CDKnifeItem {
    public RemnantToothKnife(Properties properties) {
        super(6.0F, -2.0F, properties);
    }

    @Override
    protected String[] tooltipSuffixes() {
        return new String[]{""};
    }
}
