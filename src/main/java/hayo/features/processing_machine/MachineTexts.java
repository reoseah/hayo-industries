package hayo.features.processing_machine;

import com.google.common.collect.Lists;
import hayo.Hayo;
import hayo.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class MachineTexts {
    public static @NonNull List<Component> createUpgradableMachineTooltip(UpgradableMachineData data, int defaultRecipeCost, int baseEnergyUse) {
        List<Component> components = Lists.newArrayList(
                EnergyTexts.amountAndPercentage(data.energy(), data.capacity()),
                EnergyTexts.maxAmount(data.capacity()).withStyle(ChatFormatting.GRAY),
                Component.translatable("hayo.machine.energy_use_with_base_and_bonus",
                        data.energyUseRate(),
                        baseEnergyUse,
                        String.format("%+.0f", data.extraCraftingSpeed())
                ).withStyle(ChatFormatting.GRAY)
        );
        if (data.hasInductionUpgrade()) {
            components.add(Component.translatable("hayo.machine.heat",
                    String.format("%.1f", data.inductionHeat() * 100F / UpgradableMachineBlockEntity.MAX_INDUCTION_HEAT),
                    data.getRecipeProgressPerTick()
            ).withStyle(ChatFormatting.GRAY));
        }

        components.add(Component.empty());
        if (data.matchesRecipe()) {
            components.add(Component.translatable("hayo.machine.current_recipe").withStyle(ChatFormatting.GRAY));
        } else {
            components.add(Component.translatable("hayo.machine.default_recipe").withStyle(ChatFormatting.GRAY));
        }
        components.add(Component.translatable("hayo.machine.recipe_cost",
                data.recipeCost(),
                defaultRecipeCost,
                String.format("%+.0f", data.extraRecipeCost())
        ).withStyle(ChatFormatting.GRAY));

        float duration = Mth.positiveCeilDiv(data.recipeCost(), data.getRecipeProgressPerTick()) / 20F;
        float defaultDuration = Mth.positiveCeilDiv(defaultRecipeCost, baseEnergyUse) / 20F;
        float relativeDuration = duration / defaultDuration * 100;
        components.add(Component.translatable("hayo.machine.recipe_duration_with_upgrades",
                String.format("%.0f", duration),
                data.getRecipeProgressPerTick(),
                String.format("%.0f", defaultDuration),
                String.format("%.0f", relativeDuration)
        ).withStyle(ChatFormatting.GRAY));

        return components;
    }

    public static void addUpgradeTooltip(ItemStack stack, List<Component> tooltip, AbstractContainerMenu menu, TagKey<Item> upgradeTag, int firstUpgrade, int lastUpgrade) {
        if (!stack.is(upgradeTag)) {
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("hayo.machine.not_compatible_uprade").withStyle(ChatFormatting.RED));
        } else if (stack.is(Hayo.ItemTags.NON_REPEATABLE_UPGRADES) || stack.is(Hayo.ItemTags.MUTUALLY_EXCLUSIVE_UPGRADES)) {
            boolean hoveringItself = false;
            boolean repeats = false;
            boolean conflicts = false;

            for (var slot = firstUpgrade; slot < lastUpgrade; slot++) {
                var installedUpgrade = menu.getSlot(slot).getItem();
                if (stack == installedUpgrade) {
                    hoveringItself = true;
                    break;
                }

                if (installedUpgrade.is(Hayo.ItemTags.NON_REPEATABLE_UPGRADES) && ItemStack.isSameItem(stack, installedUpgrade)) {
                    repeats = true;
                } else if (installedUpgrade.is(Hayo.ItemTags.MUTUALLY_EXCLUSIVE_UPGRADES)) {
                    conflicts = true;
                }
            }

            if (!hoveringItself) {
                if (repeats) {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("hayo.machine.already_installed_upgrade").withStyle(ChatFormatting.RED));
                } else if (conflicts) {
                    tooltip.add(Component.empty());
                    tooltip.add(Component.translatable("hayo.machine.conflicts_with_installed_upgrade").withStyle(ChatFormatting.RED));
                }
            }
        }
    }
}
