package se.mickelus.tetra.items;

import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;

public class ThermalGaugeItem extends TetraItem {
    private static final String unlocalizedName = "thermal_gauge";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ThermalGaugeItem instance;

    // TODO: better recipe
    public ThermalGaugeItem() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }
}
