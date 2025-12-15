package io.github.reoseah.hayoind.block.entity;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public abstract class ProcessingMachineBlockEntity<R extends Recipe<I>, I extends RecipeInput> extends HayoElectricBlockEntity {
    protected final RecipeManager.CachedCheck<I, R> quickCheck;

    @Getter
    protected int recipeUsedEnergy;
    @Getter
    protected int recipeTotalEnergy;

    public ProcessingMachineBlockEntity(BlockEntityType<?> type, RecipeType<R> recipeType, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.quickCheck = RecipeManager.createCheck(recipeType);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_used_energy", this.recipeUsedEnergy);
        output.putInt("recipe_total_energy", this.recipeTotalEnergy);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeUsedEnergy = input.getIntOr("recipe_used_energy", 0);
        this.recipeTotalEnergy = input.getIntOr("recipe_total_energy", 0);
    }
}
