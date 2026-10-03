package hayo.processing_machine;

import com.google.common.collect.Lists;
import hayo.energy.EnergyTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
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
}
