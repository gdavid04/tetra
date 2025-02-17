package se.mickelus.tetra.items.modular.impl;

import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.ConfigHandler;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.ItemModularHandheld;
import se.mickelus.tetra.module.SchemaRegistry;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.module.schema.RepairSchema;
import se.mickelus.tetra.network.PacketHandler;


public class ModularSingleHeadItem extends ItemModularHandheld {

    public final static String headKey = "single/head";
    public final static String handleKey = "single/handle";

    public final static String bindingKey = "single/binding";

    private static final String unlocalizedName = "modular_single";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ModularSingleHeadItem instance;

    public ModularSingleHeadItem() {
        super(new Properties().maxStackSize(1));
        setRegistryName(unlocalizedName);

        entityHitDamage = 1;

        majorModuleKeys = new String[] { headKey, handleKey };
        minorModuleKeys = new String[] { bindingKey };

        requiredModules = new String[] { handleKey, headKey };

        updateConfig(ConfigHandler.honeSingleBase.get(), ConfigHandler.honeSingleIntegrityMultiplier.get());


        SchemaRegistry.instance.registerSchema(new RepairSchema(this));
        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    public void init(PacketHandler packetHandler) {
        DataManager.synergyData.onReload(() -> synergies = DataManager.instance.getSynergyData("single"));
    }

    public void updateConfig(int honeBase, int honeIntegrityMultiplier) {
        this.honeBase = honeBase;
        this.honeIntegrityMultiplier = honeIntegrityMultiplier;
    }
}


