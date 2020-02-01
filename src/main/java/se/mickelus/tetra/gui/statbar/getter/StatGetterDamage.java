package se.mickelus.tetra.gui.statbar.getter;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;

import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import se.mickelus.tetra.items.modular.ItemModular;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.util.CastOptional;

public class StatGetterDamage implements IStatGetter {
    public StatGetterDamage() { }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack) {
        return itemStack.getAttributeModifiers(EquipmentSlotType.MAINHAND).get(SharedMonsterAttributes.ATTACK_DAMAGE.getName()).stream()
                .map(AttributeModifier::getAmount)
                .reduce(0d, Double::sum) + player.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .map(item -> item.getModuleFromSlot(itemStack, slot))
                .map(module -> module.getDamageModifier(itemStack) + (module.getDamageMultiplierModifier(itemStack) - 1) * getValue(player, itemStack))
                .orElse(0d);
    }

    @Override
    public double getValue(PlayerEntity player, ItemStack itemStack, String slot, String improvement) {
        return CastOptional.cast(itemStack.getItem(), ItemModular.class)
                .flatMap(item -> CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class))
                .map(module -> {
                    ImprovementData data = module.getImprovement(itemStack, improvement);
                    return data.damage + (data.damageMultiplier - 1) * getValue(player, itemStack);
                })
                .orElse(0d);
    }
}
