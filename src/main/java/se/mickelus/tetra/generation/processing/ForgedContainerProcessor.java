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
import se.mickelus.tetra.blocks.forged.container.ForgedContainerBlock;
import se.mickelus.tetra.blocks.forged.container.ForgedContainerTile;
import se.mickelus.tetra.blocks.forged.transfer.EnumTransferConfig;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitBlock;
import se.mickelus.tetra.blocks.forged.transfer.TransferUnitTile;
import se.mickelus.tetra.items.modular.impl.cell.ItemCellMagmatic;

import javax.annotation.Nullable;
import java.util.Random;

public class ForgedContainerProcessor extends StructureProcessor {
    public ForgedContainerProcessor() {}

    @Nullable
    @Override
    public Template.BlockInfo process(IWorldReader world, BlockPos pos, Template.BlockInfo $, Template.BlockInfo blockInfo,
            PlacementSettings placementSettings, @Nullable Template template) {
        if (blockInfo.state.getBlock() instanceof ForgedContainerBlock) {
            Random random;

            // this ensures that both blockstates for the container gets generated from the same seed, then there's no need to sync later
            if (blockInfo.state.get(ForgedContainerBlock.flippedProp)) {
                random = placementSettings.getRandom(blockInfo.pos.offset(blockInfo.state.get(ForgedContainerBlock.facingProp).rotateYCCW()));
            } else {
                random = placementSettings.getRandom(blockInfo.pos);
            }

            CompoundNBT newCompound = blockInfo.nbt.copy();

            int[] lockIntegrity = new int[ForgedContainerTile.lockCount];
            for (int i = 0; i < lockIntegrity.length; i++) {
                lockIntegrity[i] = 1 + random.nextInt(ForgedContainerTile.lockIntegrityMax - 1);
            }
            ForgedContainerTile.writeLockData(newCompound, lockIntegrity);

            int lidIntegrity = 1 + random.nextInt(ForgedContainerTile.lidIntegrityMax - 1);
            ForgedContainerTile.writeLidData(newCompound, lidIntegrity);

            BlockState newState = ForgedContainerTile.getUpdatedBlockState(blockInfo.state, lockIntegrity, lidIntegrity);

            return new Template.BlockInfo(blockInfo.pos, newState, newCompound);
        }

        return blockInfo;
    }

    protected IStructureProcessorType getType() {
        return ProcessorTypes.forgedContainer;
    }

    protected <T> Dynamic<T> serialize0(DynamicOps<T> ops) {
        return new Dynamic<>(ops);
    }
}