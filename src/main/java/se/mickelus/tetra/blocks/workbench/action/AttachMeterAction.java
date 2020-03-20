package se.mickelus.tetra.blocks.workbench.action;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.blocks.workbench.WorkbenchTile;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ThermalGaugeItem;
import se.mickelus.tetra.items.cell.ItemCellMagmatic;

public class AttachMeterAction implements WorkbenchAction {

    public static final String key = "attach_meter_action";

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public boolean canPerformOn(PlayerEntity player, ItemStack itemStack) {
        return itemStack.getItem() == ItemCellMagmatic.instance &&
                !ItemCellMagmatic.instance.isMetered(itemStack) &&
                player.inventory.hasItemStack(new ItemStack(ThermalGaugeItem.instance));
    }

    @Override
    public Capability[] getRequiredCapabilitiesFor(ItemStack itemStack) {
        return new Capability[] { Capability.hammer };
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return 5;
    }

    @Override
    public void perform(PlayerEntity player, ItemStack itemStack, WorkbenchTile workbench) {
        if (!player.world.isRemote) {
            player.inventory.decrStackSize(player.inventory.findSlotMatchingUnusedItem(new ItemStack(ThermalGaugeItem.instance)), 1);
            ItemCellMagmatic.instance.attachMeter(itemStack);
            workbench.markDirty();
        }
    }

}
