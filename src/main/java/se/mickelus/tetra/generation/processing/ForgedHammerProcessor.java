package se.mickelus.tetra.generation.processing;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.forged.hammer.EnumHammerConfig;
import se.mickelus.tetra.blocks.forged.hammer.EnumHammerPlate;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseBlock;
import se.mickelus.tetra.blocks.forged.hammer.HammerBaseTile;
import se.mickelus.tetra.items.modular.impl.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedHammerProcessor extends StructureProcessor {
    public ForgedHammerProcessor() { }


    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof HammerBaseBlock) {
            Random random = placementSettings.getRandom(blockInfo.pos);
            CompoundNBT newCompound = blockInfo.nbt.copy();

            // randomize cells
            ItemStack cell1 = null;
            ItemStack cell2 = null;

            float charge1 = random.nextFloat();
            if (random.nextBoolean()) {
                cell1 = ItemCellMagmatic.instance.make(charge1);
            } else if (random.nextBoolean()) {
                cell1 = ItemCellMagmatic.instance.make(0);
            }

            float charge2 = random.nextFloat();
            if (random.nextBoolean()) {
                cell2 = ItemCellMagmatic.instance.make(charge2);
            } else if (random.nextBoolean()) {
                cell2 = ItemCellMagmatic.instance.make(0);
            }

            HammerBaseTile.writeCells(newCompound, cell1, cell2);

            EnumHammerConfig[] configs =  EnumHammerConfig.values();
            BlockState newState = blockInfo.state
                    .with(HammerBaseBlock.propCell1, cell1 != null)
                    .with(HammerBaseBlock.propCell1Charged, cell1 != null && charge1 > 0)
                    .with(HammerBaseBlock.propCell2, cell2 != null)
                    .with(HammerBaseBlock.propCell2Charged, cell2 != null && charge2 > 0)
                    .with(EnumHammerConfig.eastProp, configs[random.nextInt(configs.length)])
                    .with(EnumHammerConfig.eastProp, configs[random.nextInt(configs.length)])
                    .with(EnumHammerConfig.eastProp, configs[random.nextInt(configs.length)])
                    .with(EnumHammerConfig.eastProp, configs[random.nextInt(configs.length)])
                    .with(EnumHammerConfig.westProp, configs[random.nextInt(configs.length)])
                    .with(EnumHammerPlate.east.prop, random.nextBoolean())
                    .with(EnumHammerPlate.west.prop, random.nextBoolean());

            return new Template.BlockInfo(blockInfo.pos, newState, newCompound);
        }
        return blockInfo;
    }

    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedHammer;
    }

    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return new Dynamic<>(ops);
    }
}
