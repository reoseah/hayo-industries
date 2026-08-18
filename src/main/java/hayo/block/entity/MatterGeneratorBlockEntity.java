package hayo.block.entity;

import hayo.Hayo;
import hayo.menu.MatterGeneratorMenu;
import hayo.recipe.EmptyRecipeInput;
import hayo.recipe.MatterGeneratingRecipe;
import hayo.util.IntRange;
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
import org.jspecify.annotations.Nullable;

public class MatterGeneratorBlockEntity extends MachineBlockEntity<MatterGeneratingRecipe, EmptyRecipeInput> {
    public static final int CAPACITY = 10000, TRANSFER_LIMIT = 128, ENERGY_USE_RATE = 100, SLOTS = 2, BATTERY_SLOT = 0, OUTPUT_SLOT = 1;

    public @Nullable Identifier selectedRecipeId;

    public MatterGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MATTER_GENERATOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MatterGeneratorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY_SLOT);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @Override
    protected RecipeType<MatterGeneratingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MATTER_GENERATING;
    }

    @Override
    protected int getBaseEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    protected int getBaseEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    protected int getBaseEnergyCost(RecipeHolder<MatterGeneratingRecipe> holder) {
        return holder == null ? 1_000_000 : holder.value().energyCost();
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return false;
    }

    @Override
    protected IntRange getUpgradeSlots() {
        return new IntRange(0, 0);
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    protected boolean hasEnoughEnergyToProgress() {
        return this.storedEnergy >= 1;
    }

    @Override
    protected EmptyRecipeInput createRecipeInput() {
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
    public @Nullable RecipeHolder<MatterGeneratingRecipe> updateMatchingRecipe(ServerLevel level, EmptyRecipeInput input) {
        if (this.selectedRecipeId == null) {
            return null;
        }
        if (this.lastRecipe != null && this.lastRecipe.value().getType() == Hayo.RecipeTypes.MATTER_GENERATING) {
            if (this.lastRecipe.value().matches(input, level)) {
                return this.lastRecipe;
            }
        }
        return this.lastRecipe = (RecipeHolder<MatterGeneratingRecipe>) level.recipeAccess() //
                .byKey(ResourceKey.create(Registries.RECIPE, this.selectedRecipeId)) //
                .filter(holder -> holder.value().getType() == Hayo.RecipeTypes.MATTER_GENERATING) //
                .orElse(null);
    }

    @Override
    protected boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<MatterGeneratingRecipe> recipe, EmptyRecipeInput recipeInput) {
        return recipe != null && canInsertToSlot(this.stacks, recipe.value().result().create(), OUTPUT_SLOT);
    }

    @Override
    protected void craft(RegistryAccess registryAccess, RecipeHolder<MatterGeneratingRecipe> recipe, EmptyRecipeInput input) {
        var recipeOutput = recipe.value().assemble(input);
        var outputStack = this.stacks.get(OUTPUT_SLOT);
        if (outputStack.isEmpty()) {
            this.stacks.set(OUTPUT_SLOT, recipeOutput);
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
