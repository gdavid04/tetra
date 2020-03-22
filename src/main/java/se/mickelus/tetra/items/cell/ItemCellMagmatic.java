package se.mickelus.tetra.items.cell;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.TetraItemGroup;

import static se.mickelus.tetra.blocks.forged.ForgedBlockCommon.locationTooltip;

public class ItemCellMagmatic extends TetraItem {

    public final static String meterKey = "meter";

    private static final String unlocalizedName = "magmatic_cell";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemCellMagmatic instance;

    private final String chargedPropKey = "tetra:charged";
    private final String chargePropKey = "tetra:charge";
    private final String meteredPropKey = "tetra:metered";

    public static final int maxCharge = 128;

    public ItemCellMagmatic() {
        super(new Properties()
                .maxStackSize(1)
                .maxDamage(maxCharge)
                .group(TetraItemGroup.instance));

        setRegistryName(unlocalizedName);

        // is charged
        addPropertyOverride(new ResourceLocation(chargedPropKey), (itemStack, world, livingEntity) -> getCharge(itemStack) == 0 ? 0 : 1);
        // charge percentage
        addPropertyOverride(new ResourceLocation(chargePropKey), (itemStack, world, livingEntity) -> getCharge(itemStack) / (float) maxCharge);
        // is metered
        // TODO: visual indicator of thermal gauge attachment
        addPropertyOverride(new ResourceLocation(meteredPropKey), (itemStack, world, livingEntity) -> isMetered(itemStack) ? 1 : 0);
    }

    @Override
    public void addInformation(final ItemStack stack, @Nullable final World world, final List<ITextComponent> tooltip, final ITooltipFlag advanced) {
        int charge = getCharge(stack);

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
        if (isMetered(stack)) {
            tooltip.add(new StringTextComponent(""));
            tooltip.add(new TranslationTextComponent("attachments"));
            tooltip.add(new StringTextComponent("§8- ")
                    .appendSibling(new TranslationTextComponent("item.tetra.thermal_gauge")));
        }
        tooltip.add(new StringTextComponent(""));
        tooltip.add(locationTooltip);
    }

    @Override
    public void fillItemGroup(final ItemGroup itemGroup, final NonNullList<ItemStack> itemList) {
        if (isInGroup(itemGroup)) {
            itemList.add(new ItemStack(this));

            ItemStack emptyStack = new ItemStack(this);
            emptyStack.setDamage(maxCharge);
            itemList.add(emptyStack);
            
            ItemStack metered = new ItemStack(this);
            attachMeter(metered);
            itemList.add(metered);
        }
    }

    public int getCharge(ItemStack itemStack) {
        return itemStack.getMaxDamage() - itemStack.getDamage();
    }

    public int drainCharge(ItemStack itemStack, int amount) {
        if (itemStack.getDamage() + amount < itemStack.getMaxDamage()) {
            setDamage(itemStack, itemStack.getDamage() + amount);
            return amount;
        }

        int actualAmount = itemStack.getMaxDamage() - itemStack.getDamage();
        setDamage(itemStack, itemStack.getMaxDamage());
        return actualAmount;
    }

    public int recharge(ItemStack itemStack, int amount) {
        if (getDamage(itemStack) - amount >= 0) {
            setDamage(itemStack, getDamage(itemStack) - amount);
            return 0;
        }

        int overfill = amount - getDamage(itemStack);
        setDamage(itemStack, 0);
        return overfill;
    }

    public boolean isMetered(ItemStack stack) {
        return NBTHelper.getTag(stack)
                .getBoolean(meterKey);
    }

    public void attachMeter(ItemStack stack) {
        NBTHelper.getTag(stack)
         .putBoolean(meterKey, true);
    }

    public boolean showDurabilityBar(ItemStack stack) {
        return isMetered(stack);
    }

    public double getDurabilityForDisplay(ItemStack itemStack) {
        return super.getDurabilityForDisplay(itemStack);
    }

    public int getRGBDurabilityForDisplay(ItemStack itemStack) {
        return MathHelper.rgb(1, (1 - (float) getDurabilityForDisplay(itemStack)) / 2, 0);
    }

    public int getComparatorValue(ItemStack stack) {
        int charge = getCharge(stack);
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
}
