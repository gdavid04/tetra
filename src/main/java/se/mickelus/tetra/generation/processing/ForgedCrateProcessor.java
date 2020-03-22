package se.mickelus.tetra.generation.processing;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import se.mickelus.tetra.blocks.forged.BlockForgedCrate;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitTile;
import se.mickelus.tetra.items.modular.impl.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedCrateProcessor extends StructureProcessor {
    public ForgedCrateProcessor() {}


    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof BlockForgedCrate) {
            Random random = placementSettings.getRandom(blockInfo.pos);

            BlockState blockState = blockInfo.state
                    .with(BlockForgedCrate.propIntegrity, random.nextInt(4))
                    .with(BlockForgedCrate.propFacing, Direction.byHorizontalIndex(random.nextInt(4)));

            return new Template.BlockInfo(blockInfo.pos, blockState, blockInfo.nbt);
        }

        return blockInfo;
    }

    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedCrate;
    }

    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return new Dynamic<>(ops);
    }
}