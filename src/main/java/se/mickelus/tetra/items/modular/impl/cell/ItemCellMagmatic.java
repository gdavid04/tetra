package se.mickelus.tetra.items.modular.impl.cell;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.ItemEffect;
import se.mickelus.tetra.module.schema.RemoveSchema;
import se.mickelus.tetra.network.PacketHandler;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class ItemCellMagmatic extends ItemModular {

    public final static String meterKey = "meter";

    public final static String unlocalizedName = "magmatic_cell";

    public final static String casingKey = "cell/casing";
    public final static String canisterKey = "cell/canister";
    public final static String gadgetKey = "cell/gadget";

    private final String chargedPropKey = "tetra:charged";
    private final String chargePropKey = "tetra:charge";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;

    public ItemCellMagmatic() {
        super(new Properties()
            .maxStackSize(1)
            .group(TetraItemGroup.instance));

        setRegistryName(unlocalizedName);

        majorModuleKeys = new String[] { casingKey, canisterKey };
        minorModuleKeys = new String[] { gadgetKey };

        requiredModules = new String[] { casingKey, canisterKey };

        addPropertyOverride(new ResourceLocation(chargedPropKey), (itemStack, world, livingEntity) -> getCharge(itemStack) > 0 ? 1 : 0);
        addPropertyOverride(new ResourceLocation(chargePropKey), (itemStack, world, livingEntity) -> getCharge(itemStack) / (float) getMaxCharge(itemStack));
    }

    @Override
    public void init(PacketHandler packetHandler) {
        RemoveSchema.registerRemoveSchemas(this);
    }

    @Override
    public void clientInit() {
        // We don't need modular renderer for magmatic cell
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (isInGroup(group)) {
            items.add(setupCellStack("basic", "regular", 0));
            items.add(setupCellStack("basic", "regular", 128));
            items.add(setupCellStack("basic", "regular", "gauge", 0));
            items.add(setupCellStack("basic", "regular", "gauge", 128));
        }
    }

    private ItemStack setupCellStack(String casing, String canister, int charge) {
        ItemStack itemStack = new ItemStack(this);

        putModuleInSlot(itemStack, casingKey, "cell/casing", "cell/casing_material", "casing/" + casing);
        putModuleInSlot(itemStack, canisterKey, "cell/canister", "cell/canister_material", "canister/" + canister);
        setCharge(itemStack, charge);
        
        return itemStack;
    }

    private ItemStack setupCellStack(String casing, String canister, String gadget, int charge) {
        ItemStack itemStack = setupCellStack(casing, canister, charge);

        putModuleInSlot(itemStack, gadgetKey, "cell/gadget", "cell/gadget_material", "gadget/" + gadget);

        return itemStack;
    }

    public ItemStack make(float chargePercent) {
        return setupCellStack("basic", "regular", Math.round(chargePercent * 128));
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        return new TranslationTextComponent("item.tetra.magmatic_cell");
    }
    
    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        int charge = getCharge(stack);
        int maxCharge = getMaxCharge(stack);

        TextComponent chargeLine = new TranslationTextComponent("item.tetra.magmatic_cell.charge");

        if (charge == maxCharge) {
            chargeLine.appendSibling(new TranslationTextComponent("item.tetra.magmatic_cell.charge_full"));
        } else if (charge > maxCharge * 0.4) {
            chargeLine.appendSibling(new TranslationTextComponent("item.tetra.magmatic_cell.charge_good"));
        } else if (charge > 0) {
            chargeLine.appendSibling(new TranslationTextComponent("item.tetra.magmatic_cell.charge_low"));
        } else {
            chargeLine.appendSibling(new TranslationTextComponent("item.tetra.magmatic_cell.charge_empty"));
        }
        
        if (isMetered(stack) && charge != 0 && charge != maxCharge) {
            chargeLine.appendSibling(new StringTextComponent(String.format(" §8(§7%.0f%%§8)", charge * 100f / maxCharge)));
        }

        tooltip.add(chargeLine);
        tooltip.add(new StringTextComponent(""));
        tooltip.add(locationTooltip);
    }

    public int getCharge(ItemStack itemStack) {
        CompoundNBT tag = itemStack.getTag();
        if (tag == null) {
            return 0;
        }
        return tag.getInt("charge");
    }
    
    public int getMaxCharge(ItemStack itemStack) {
        return getEffectLevel(itemStack, ItemEffect.magmaCapacity);
    }
    
    public void setCharge(ItemStack stack, int amount) {
        stack.getOrCreateTag().putInt("charge", amount);
    }

    public int drainCharge(ItemStack itemStack, int amount) {
        if (getCharge(itemStack) - amount >= 0) {
            setCharge(itemStack, getCharge(itemStack) - amount);
            return amount;
        }

        int actualAmount = getCharge(itemStack);
        setCharge(itemStack, 0);
        return actualAmount;
    }

    public int recharge(ItemStack itemStack, int amount) {
        if (getCharge(itemStack) + amount <= getMaxCharge(itemStack)) {
            setCharge(itemStack, getCharge(itemStack) + amount);
            return 0;
        }

        int overfill = getMaxCharge(itemStack) - getCharge(itemStack);
        setCharge(itemStack, getMaxCharge(itemStack));
        return overfill;
    }

    public boolean isMetered(ItemStack stack) {
        return getAllModules(stack).stream()
                .anyMatch(module -> module.getEffects(stack).contains(ItemEffect.metered));
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return isMetered(stack);
    }

    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - (getCharge(itemStack) / (float) getMaxCharge(itemStack));
    }

    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return MathHelper.rgb(1, (1 - (float) getDurabilityForDisplay(itemStack)) / 2, 0);
    }

    public int getComparatorValue(ItemStack stack) {
        int charge = getCharge(stack);
        int maxCharge = getMaxCharge(stack);
        if (charge == maxCharge) {
            return 15;
        } else if (charge == 0) {
            return 0;
        } else if (isMetered(stack)) {
            // 0              -> 0
            // 1..maxCharge-1 -> 1..14
            // maxCharge      -> 15
            return Math.round((charge - 1) / (maxCharge - 2f) * 14) + 1;
        } else if (charge > maxCharge * 0.4) {
            return 10;
        } else {
            return 5;
        }
    }
    
    @Override
    public void assemble(ItemStack itemStack, World world, float severity) {
        super.assemble(itemStack, world, severity);
        if (getMaxCharge(itemStack) < getCharge(itemStack)) {
            setCharge(itemStack, getMaxCharge(itemStack));
        }
    }
}
