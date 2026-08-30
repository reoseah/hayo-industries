package hayo.processing_machine.matter_generator;

import hayo.Hayo;
import hayo.processing_machine.MachineBlockEntity;
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

public class MatterGeneratorBlockEntity extends MachineBlockEntity<MatterGeneratingRecipe, MatterGeneratorRecipeInput> {
    public static final int CAPACITY = 10000, TRANSFER_LIMIT = 128, ENERGY_USE_RATE = 100;
    public static final int SLOTS = 4, BATTERY = 0, OUTPUT = 1, FIRST_UPGRADE = 2, UPGRADES = 2;

    public @Nullable Identifier selectedRecipeId;

    public MatterGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MATTER_GENERATOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MatterGeneratorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.tickRecipe((ServerLevel) level, pos, state);
        entity.resetEnergyPerTick();
    }

    @Override
    protected RecipeType<MatterGeneratingRecipe> getRecipeType() {
        return Hayo.RecipeTypes.MATTER_GENERATING;
    }

    @Override
    protected int getEnergyTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    public int getEnergyCapacity() {
        return CAPACITY;
    }

    @Override
    public int getEnergyUseRate() {
        return ENERGY_USE_RATE;
    }

    @Override
    public int getEnergyCost(RecipeHolder<MatterGeneratingRecipe> holder) {
        return holder == null ? 1_000_000 : holder.value().energyCost();
    }

    @Override
    protected boolean hasEnoughEnergyToProgress() {
        return this.storedEnergy >= 1;
    }

    @Override
    protected boolean isInputSlot(int slot) {
        return false;
    }

    @Override
    protected MatterGeneratorRecipeInput createRecipeInput() {
        return new MatterGeneratorRecipeInput(this.stacks.subList(FIRST_UPGRADE, FIRST_UPGRADE + UPGRADES));
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

    public void selectRecipe(Identifier id) {
        this.selectedRecipeId = id;
        this.recipeProgress = 0;
        this.setChanged();
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable RecipeHolder<MatterGeneratingRecipe> findMatchingRecipe(ServerLevel level, MatterGeneratorRecipeInput input) {
        if (this.selectedRecipeId == null) {
            return null;
        }
        if (this.lastRecipe != null && this.lastRecipe.value().matches(input, level)) {
            return this.lastRecipe;
        }
        return this.lastRecipe = (RecipeHolder<MatterGeneratingRecipe>) level.recipeAccess()
                .byKey(ResourceKey.create(Registries.RECIPE, this.selectedRecipeId))
                .filter(holder -> holder.value().getType() == Hayo.RecipeTypes.MATTER_GENERATING)
                .filter(holder -> ((MatterGeneratingRecipe) holder.value()).matches(input, level))
                .orElse(null);
    }

    @Override
    protected boolean canCraft(RegistryAccess registryAccess, @Nullable RecipeHolder<MatterGeneratingRecipe> recipe, MatterGeneratorRecipeInput input) {
        return recipe != null
                && recipe.value().matches(input, this.level)
                && this.canInsertToSlot(recipe.value().result().create(), OUTPUT);
    }

    @Override
    protected void craft(RegistryAccess registryAccess, RecipeHolder<MatterGeneratingRecipe> recipe, MatterGeneratorRecipeInput input) {
        var recipeOutput = recipe.value().assemble(input);
        var outputStack = this.stacks.get(OUTPUT);
        if (outputStack.isEmpty()) {
            this.stacks.set(OUTPUT, recipeOutput);
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
}
