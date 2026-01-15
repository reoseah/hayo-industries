package io.github.reoseah.hayo.feature.processing_machines.matter_generator;

import io.github.reoseah.hayo.Hayo;
import io.github.reoseah.hayo.feature.processing_machines.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class MatterGeneratorBlockEntity extends MachineBlockEntity<MatterGeneratingRecipe, EmptyRecipeInput> {
    public static final int CAPACITY = 10000, TRANSFER_RATE = 128, ENERGY_USE_RATE = 100, SLOTS = 2, BATTERY_SLOT = 0, OUTPUT_SLOT = 1;

    protected @Nullable Identifier selectedRecipeId;

    public MatterGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MATTER_GENERATOR, pos, state);
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MatterGeneratorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        tickProcessing((ServerLevel) level, pos, state, entity);
        entity.onTickEnd();
    }

    @Override
    protected RecipeType<MatterGeneratingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MATTER_GENERATING;
    }

    @Override
    protected int getDefaultCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getDefaultEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    protected int getDefaultEnergyCost(RecipeHolder<MatterGeneratingRecipe> holder) {
        return holder.value().getEnergyCost();
    }

    @Override
    protected int getSlotCount() {
        return SLOTS;
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return false;
    }

    @Override
    protected int getFirstUpgradeSlot() {
        return 0;
    }

    @Override
    protected int getLastUpgradeSlot() {
        return -1;
    }

    @Override
    protected int getMinEnergyUseRate() {
        return 0;
    }

    @Override
    protected int getEnergyTransferRate() {
        return TRANSFER_RATE;
    }

    @Override
    protected EmptyRecipeInput getRecipeInput(NonNullList<ItemStack> items) {
        return EmptyRecipeInput.INSTANCE;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("selected_recipe", Identifier.CODEC, this.selectedRecipeId);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.selectedRecipeId = input.read("selected_recipe", Identifier.CODEC).orElse(null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable RecipeHolder<MatterGeneratingRecipe> findMatchingRecipe(ServerLevel level, EmptyRecipeInput input) {
        if (this.selectedRecipeId == null) {
            return null;
        }
        return (RecipeHolder<MatterGeneratingRecipe>) level.recipeAccess() //
                .byKey(ResourceKey.create(Registries.RECIPE, this.selectedRecipeId)) //
                .filter(holder -> holder.value().getType() == Hayo.RecipeTypes.MATTER_GENERATING) //
                .orElse(null);
    }

    @Override
    protected boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<MatterGeneratingRecipe> recipe, EmptyRecipeInput recipeInput, NonNullList<ItemStack> items) {
        return recipe != null && canInsertToSlot(items, recipe.value().result(), OUTPUT_SLOT);
    }

    @Override
    protected void craft(RegistryAccess registryAccess, RecipeHolder<MatterGeneratingRecipe> recipe, EmptyRecipeInput input, NonNullList<ItemStack> items) {
        var recipeOutput = recipe.value().assemble(input, registryAccess);
        var outputStack = items.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            items.set(OUTPUT_SLOT, recipeOutput);
        } else {
            outputStack.grow(recipeOutput.getCount());
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.hayo.matter_generator");
    }

    @Override
    public AbstractContainerMenu createMenu(int menuId, Inventory inventory, Player player) {
        return new MatterGeneratorMenu(menuId, this, inventory);
    }

    public void selectRecipe(Identifier id) {
        this.selectedRecipeId = id;
        this.resetRecipeProgress();
        this.setChanged();
    }
}
