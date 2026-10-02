package hayo.processing_machine.matter_generator;

import hayo.Hayo;
import hayo.common.blockentity.EnergyReceiverBlockEntity;
import hayo.processing_machine.RecipeState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class MatterGeneratorBlockEntity extends EnergyReceiverBlockEntity implements RecipeState.Context<MatterGeneratingRecipe, MatterGeneratorRecipeInput> {
    public static final int CAPACITY = 10000, TRANSFER_LIMIT = 128, ENERGY_USE_RATE = 100;
    public static final int SLOTS = 4, BATTERY = 0, OUTPUT = 1, UPGRADE_1 = 2, UPGRADES = 2;

    public @Nullable Identifier selectedRecipeId;

    protected final RecipeState<MatterGeneratingRecipe, MatterGeneratorRecipeInput> recipeState = new RecipeState<>() {
        @SuppressWarnings("unchecked")
        @Override
        protected @Nullable RecipeHolder<MatterGeneratingRecipe> getMatchingRecipe(RecipeType<MatterGeneratingRecipe> recipeType, MatterGeneratorRecipeInput input, ServerLevel level) {
            if (MatterGeneratorBlockEntity.this.selectedRecipeId == null) {
                return null;
            }
            if (this.lastMatch != null && this.lastMatch.value().matches(input, level)) {
                return this.lastMatch;
            }
            return this.lastMatch = (RecipeHolder<MatterGeneratingRecipe>) level.recipeAccess()
                    .byKey(ResourceKey.create(Registries.RECIPE, MatterGeneratorBlockEntity.this.selectedRecipeId))
                    .filter(holder -> holder.value().getType() == Hayo.RecipeTypes.MATTER_GENERATING && ((MatterGeneratingRecipe) holder.value()).matches(input, level))
                    .orElse(null);
        }
    };

    public MatterGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Hayo.BlockEntityTypes.MATTER_GENERATOR, pos, state, NonNullList.withSize(SLOTS, ItemStack.EMPTY));
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, MatterGeneratorBlockEntity entity) {
        entity.chargeFromSlot(BATTERY);
        entity.resetEnergyPerTick();

        boolean wasProcessing = entity.recipeState.progress > 0;

        int energyUsed = entity.recipeState.tick(
                Hayo.RecipeTypes.MATTER_GENERATING,
                new MatterGeneratorRecipeInput(entity.stacks.subList(UPGRADE_1, UPGRADE_1 + UPGRADES)),
                (ServerLevel) level,
                entity
        );
        entity.storedEnergy = Math.clamp(entity.storedEnergy - energyUsed, 0, CAPACITY);

        boolean isProcessing = entity.recipeState.progress > 0;
        if (wasProcessing != isProcessing) {
            level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, isProcessing));
        }
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
    public int getRecipeCost(MatterGeneratingRecipe holder) {
        return holder != null ? holder.energyCost() : 1_000_000;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("recipe_progress", this.recipeState.progress);
        output.storeNullable("selected_recipe", Identifier.CODEC, this.selectedRecipeId);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.recipeState.progress = input.getIntOr("recipe_progress", 0);
        this.selectedRecipeId = input.read("selected_recipe", Identifier.CODEC).orElse(null);
    }

    public void selectRecipe(Identifier id) {
        this.selectedRecipeId = id;
        this.recipeState.progress = 0;
        this.setChanged();
    }

    @Override
    public RecipeState.EnergyState getEnergyState() {
        return this.storedEnergy >= 1
                ? new RecipeState.EnergyState.Sufficient(Math.min(this.storedEnergy, ENERGY_USE_RATE))
                : new RecipeState.EnergyState.NotSufficient(-2 * ENERGY_USE_RATE);
    }

    @Override
    public boolean canCraft(@Nullable RecipeHolder<MatterGeneratingRecipe> holder, MatterGeneratorRecipeInput input) {
        return holder != null
                && holder.value().matches(input, this.level)
                && this.canInsertToSlot(holder.value().result().create(), OUTPUT);
    }

    @Override
    public void craft(RecipeHolder<MatterGeneratingRecipe> holder, MatterGeneratorRecipeInput input) {
        var recipeOutput = holder.value().assemble(input);
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
