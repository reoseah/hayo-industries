package hayo;

import com.mojang.serialization.MapCodec;
import hayo.battery_box.BatteryBoxBlock;
import hayo.battery_box.BatteryBoxBlockEntity;
import hayo.cable.CableBlock;
import hayo.common.item.BlockItemWithTooltip;
import hayo.common.item.ItemWithTooltip;
import hayo.common.item.SimpleElectricItem;
import hayo.energy.EnergyTexts;
import hayo.energy.item.*;
import hayo.energy_armor.EnergyArmorOverlayRenderer;
import hayo.energy_storage.*;
import hayo.generator.GeneratorBlock;
import hayo.generator.GeneratorBlockEntity;
import hayo.generator.GeneratorMenu;
import hayo.generator.GeneratorScreen;
import hayo.old_menus.UniversalContainerMenu;
import hayo.old_menus.UniversalContainerScreen;
import hayo.processing_machine.classic.*;
import hayo.processing_machine.matter_generator.*;
import hayo.rubber_tree.ResinProducingLogBlock;
import hayo.rubber_tree.RubberFoliagePlacer;
import hayo.wrench.WrenchItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.equipment.*;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.world.level.block.Blocks.leavesProperties;
import static net.minecraft.world.level.block.Blocks.logProperties;

// TODO: consider making drills and what not "bundle-like" holders of the battery or energy crystal
public class Hayo {
    public static final String MOD_ID = "hayo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CreativeModeTab TAB = FabricCreativeModeTab.builder().title(Component.translatable("itemGroup.hayo")).icon(() -> new ItemStack(Blocks.ELECTRIC_FURNACE)).build();

    public static final TreeGrower RUBBER_TREE = new TreeGrower( //
            "hayo:rubber_tree", //
            0F, //
            Optional.empty(), //
            Optional.empty(), //
            Optional.of(modKey(Registries.CONFIGURED_FEATURE, "rubber_tree")), //
            Optional.empty(), //
            Optional.empty(), //
            Optional.empty());

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modId("main"), TAB);

        Blocks.initialize();
        Items.initialize();
        BlockEntityTypes.initialize();
        MenuTypes.initialize();
        FoliagePlacerTypes.initialize();
        RecipeTypes.initialize();
        RecipeSerializers.initialize();
        SoundEvents.initialize();

        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MACERATING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.COMPRESSING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.EXTRACTING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MATTER_GENERATING);

        BiomeModifications.create(modId("rubber_trees")) //
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(BiomeTags.IS_FOREST) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE)) //
                                .or(BiomeSelectors.includeByKey(Biomes.SWAMP)), //
                        (_, modification) -> {
                            var feature = modKey(Registries.PLACED_FEATURE, "rubber_tree_patch");
                            modification.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature);
                        });
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        BlockColorRegistry.register((_, level, pos, tintValues) -> tintValues.add(level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : 0xff48b518), Blocks.RUBBER_LEAVES);

        MenuTypes.initializeClient();

        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.NANO, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Items.NANO_HELMET, Items.NANO_CHESTPLATE, Items.NANO_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_NANO, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Items.NANO_LEGGINGS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.QUANTUM, EnergyArmorOverlayRenderer.QUANTUM_OVERLAY), Items.QUANTUM_HELMET, Items.QUANTUM_CHESTPLATE, Items.QUANTUM_BOOTS);
        ArmorRenderer.register(ctx -> new EnergyArmorOverlayRenderer(ctx, EnergyArmorOverlayRenderer.LEGS_QUANTUM, EnergyArmorOverlayRenderer.LEGS_QUANTUM_OVERLAY), Items.QUANTUM_LEGGINGS);
    }

    public static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> modKey(ResourceKey<? extends Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, modId(location));
    }

    public static class Blocks {
        private static final BlockBehaviour.Properties MACHINE_PROPS = BlockBehaviour.Properties.of().strength(5F).sound(SoundType.METAL).mapColor(MapColor.METAL);
        private static final BlockBehaviour.Properties LIT_MACHINE_PROPS = BlockBehaviour.Properties.of().strength(5F).sound(SoundType.METAL).mapColor(MapColor.METAL).lightLevel(state -> state.getValue(BlockStateProperties.LIT) ? 11 : 0);
        public static final Block GENERATOR = register("generator", GeneratorBlock::new, LIT_MACHINE_PROPS);
        public static final Block ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlock::new, LIT_MACHINE_PROPS);
        public static final Block MACERATOR = register("macerator", MaceratorBlock::new, LIT_MACHINE_PROPS);
        public static final Block COMPRESSOR = register("compressor", CompressorBlock::new, LIT_MACHINE_PROPS);
        public static final Block EXTRACTOR = register("extractor", ExtractorBlock::new, LIT_MACHINE_PROPS);
        public static final Block MATTER_GENERATOR = register("matter_generator", MatterGeneratorBlock::new, LIT_MACHINE_PROPS);
        public static final Block BATTERY_BOX = register("battery_box", BatteryBoxBlock::new, BlockBehaviour.Properties.of().strength(5F).sound(SoundType.WOOD).mapColor(MapColor.WOOD));
        public static final Block CRYSTAL_ENERGY_STORAGE = register("crystal_energy_storage", CrystalEnergyStorageBlock::new, MACHINE_PROPS);
        public static final Block LAPOTRON_ENERGY_STORAGE = register("lapotron_energy_storage", LapotronEnergyStorageBlock::new, MACHINE_PROPS);

        public static final CableBlock POWER_CABLE = register("power_cable", properties -> new CableBlock(32, 2, properties), BlockBehaviour.Properties.of().strength(.5F, 3).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final CableBlock QUADRUPLE_POWER_CABLE = register("quadruple_power_cable", properties -> new CableBlock(128, 3, properties), BlockBehaviour.Properties.of().strength(.75F, 6).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final CableBlock ENERGY_BUS = register("energy_bus", properties -> new CableBlock(512, 5, properties), BlockBehaviour.Properties.of().strength(1F, 15).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY));

        public static final Block RUBBER_LOG = register("rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD));
        public static final Block RESIN_PRODUCING_RUBBER_LOG = register("resin_producing_rubber_log", ResinProducingLogBlock::new, BlockBehaviour.Properties.of().randomTicks().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
        public static final Block RUBBER_WOOD = register("rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_LOG = register("stripped_rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_WOOD = register("stripped_rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block RUBBER_LEAVES = register("rubber_leaves", properties -> new TintedParticleLeavesBlock(0.01F, properties), leavesProperties(SoundType.GRASS));
        public static final Block RUBBER_SAPLING = register("rubber_sapling", properties -> new SaplingBlock(RUBBER_TREE, properties), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));

        public static final BlockBehaviour.Properties RUBBER_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
        public static final Block RUBBER_PLANKS = register("rubber_planks", Block::new, RUBBER_PROPERTIES);
        public static final Block RUBBER_STAIRS = register("rubber_stairs", props -> new StairBlock(RUBBER_PLANKS.defaultBlockState(), props), RUBBER_PROPERTIES);
        public static final Block RUBBER_SLAB = register("rubber_slab", SlabBlock::new, RUBBER_PROPERTIES);
        public static final Block RUBBER_DOOR = register("rubber_door", props -> new DoorBlock(BlockSetType.OAK, props), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, MACHINE_PROPS);
        public static final Block ADVANCED_MACHINE_BLOCK = register("advanced_machine_block", Block::new, BlockBehaviour.Properties.of().strength(15F).sound(SoundType.METAL).mapColor(MapColor.METAL));
        public static final Block SILICON_BRONZE_BLOCK = register("silicon_bronze_block", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).mapColor(MapColor.COLOR_ORANGE));
        public static final Block COMPOSITE_PLATE_BLOCK = register("composite_plate_block", Block::new, BlockBehaviour.Properties.of().strength(10F, 60F).sound(SoundType.METAL).mapColor(MapColor.COLOR_GREEN));
        public static final Block RAW_SILICON_BLOCK = register("raw_silicon_block", Block::new, BlockBehaviour.Properties.of().strength(3F).mapColor(MapColor.COLOR_BLACK));

        public static final Block CHIPBOARD = register("chipboard", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.WOOD).mapColor(MapColor.WOOD));
        public static final Block CHIPBOARD_DOOR = register("chipboard_door", props -> new DoorBlock(BlockSetType.OAK, props), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

        private static final BlockBehaviour.Properties REINFORCED_BLOCKS = BlockBehaviour.Properties.of().strength(3F, 30F).sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE);
        public static final Block REINFORCED_STONE = register("reinforced_stone", Block::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_GLASS = register("reinforced_glass", TransparentBlock::new, BlockBehaviour.Properties.of().strength(3F, 20F).noOcclusion().sound(SoundType.GLASS));
        public static final Block REINFORCED_STONE_STAIRS = register("reinforced_stone_stairs", props -> new StairBlock(REINFORCED_STONE.defaultBlockState(), props), REINFORCED_BLOCKS);
        public static final Block REINFORCED_STONE_SLAB = register("reinforced_stone_slab", SlabBlock::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_DOOR = register("reinforced_door", props -> new DoorBlock(BlockSetType.IRON, props), BlockBehaviour.Properties.of().strength(3F, 20F).noOcclusion().sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE));
        public static final Block REINFORCED_TRAPDOOR = register("reinforced_trapdoor", props -> new TrapDoorBlock(BlockSetType.IRON, props), BlockBehaviour.Properties.of().strength(3F, 20F).noOcclusion().sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE));

        public static void initialize() {
            StrippableBlockRegistry.register(RUBBER_LOG, STRIPPED_RUBBER_LOG);
            StrippableBlockRegistry.register(RUBBER_WOOD, STRIPPED_RUBBER_WOOD);
        }

        private static <T extends Block> T register(String name, Function<BlockBehaviour.Properties, T> constructor, BlockBehaviour.Properties properties) {
            var key = ResourceKey.create(Registries.BLOCK, modId(name));

            return Registry.register(BuiltInRegistries.BLOCK, key, constructor.apply(properties.setId(key)));
        }
    }

    public static class HBlockTags {
        public static final TagKey<Block> WRENCH_MINEABLE = TagKey.create(Registries.BLOCK, modId("mineable/wrench"));
        public static final TagKey<Block> CHAINSAW_MINEABLE = TagKey.create(Registries.BLOCK, modId("mineable/chainsaw"));
        public static final TagKey<Block> DRILL_MINEABLE = TagKey.create(Registries.BLOCK, modId("mineable/drill"));
        public static final TagKey<Block> ROTATABLE_WITH_WRENCH = TagKey.create(Registries.BLOCK, modId("rotatable_with_wrench"));
    }

    public static class Components {
        public static final DataComponentType<Unit> QUANTUM_ARMOR = register("quantum_armor",
                DataComponentType.<Unit>builder() //
                        .persistent(Unit.CODEC) //
                        .networkSynchronized(Unit.STREAM_CODEC));

        public static <T> DataComponentType<T> register(String name, DataComponentType.Builder<T> builder) {
            return Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId(name), builder.build());
        }
    }

    public static class Items {
        private static final HolderGetter<Block> BLOCK_LOOKUP = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        public static final Item ELECTRIC_FURNACE = registerBlock(Blocks.ELECTRIC_FURNACE);
        public static final Item MACERATOR = registerBlock(Blocks.MACERATOR);
        public static final Item COMPRESSOR = registerBlock(Blocks.COMPRESSOR);
        public static final Item EXTRACTOR = registerBlock(Blocks.EXTRACTOR);
        public static final Item MATTER_GENERATOR = registerBlock(Blocks.MATTER_GENERATOR, new Item.Properties().rarity(Rarity.EPIC));

        public static final Item GENERATOR = registerBlock(Blocks.GENERATOR);

        public static final Item BATTERY_BOX = registerBlock(Blocks.BATTERY_BOX);
        public static final Item CRYSTAL_ENERGY_STORAGE = registerBlock(Blocks.CRYSTAL_ENERGY_STORAGE, new Item.Properties().rarity(Rarity.RARE));
        public static final Item LAPOTRON_ENERGY_STORAGE = registerBlock(Blocks.LAPOTRON_ENERGY_STORAGE, new Item.Properties().rarity(Rarity.RARE));

        public static final Item CABLE = registerBlock(Blocks.POWER_CABLE, (block, properties) -> new BlockItemWithTooltip(block, EnergyTexts.maxAmountPerTick(block.transferLimit).withStyle(ChatFormatting.GRAY), properties));
        public static final Item POWER_CABLE = registerBlock(Blocks.QUADRUPLE_POWER_CABLE, (block, properties) -> new BlockItemWithTooltip(block, EnergyTexts.maxAmountPerTick(block.transferLimit).withStyle(ChatFormatting.GRAY), properties));
        public static final Item ENERGY_BUS = registerBlock(Blocks.ENERGY_BUS, (block, properties) -> new BlockItemWithTooltip(block, EnergyTexts.maxAmountPerTick(block.transferLimit).withStyle(ChatFormatting.GRAY), properties));

        public static final Item RUBBER_LOG = registerBlock(Blocks.RUBBER_LOG);
        public static final Item RESIN_PRODUCING_RUBBER_LOG = registerBlock(Blocks.RESIN_PRODUCING_RUBBER_LOG);
        public static final Item RUBBER_WOOD = registerBlock(Blocks.RUBBER_WOOD);
        public static final Item STRIPPED_RUBBER_LOG = registerBlock(Blocks.STRIPPED_RUBBER_LOG);
        public static final Item STRIPPED_RUBBER_WOOD = registerBlock(Blocks.STRIPPED_RUBBER_WOOD);
        public static final Item RUBBER_LEAVES = registerBlock(Blocks.RUBBER_LEAVES);
        public static final Item RUBBER_SAPLING = registerBlock(Blocks.RUBBER_SAPLING);
        public static final Item RUBBER_PLANKS = registerBlock(Blocks.RUBBER_PLANKS);
        public static final Item RUBBER_STAIRS = registerBlock(Blocks.RUBBER_STAIRS);
        public static final Item RUBBER_SLAB = registerBlock(Blocks.RUBBER_SLAB);
        public static final Item RUBBER_DOOR = registerBlock(Blocks.RUBBER_DOOR);

        public static final Item MACHINE_BLOCK = registerBlock(Blocks.MACHINE_BLOCK);
        public static final Item ADVANCED_MACHINE_BLOCK = registerBlock(Blocks.ADVANCED_MACHINE_BLOCK);
        public static final Item SILICON_BRONZE_BLOCK = registerBlock(Blocks.SILICON_BRONZE_BLOCK);
        public static final Item COMPOSITE_PLATE_BLOCK = registerBlock(Blocks.COMPOSITE_PLATE_BLOCK);
        public static final Item RAW_SILICON_BLOCK = registerBlock(Blocks.RAW_SILICON_BLOCK);

        public static final Item CHIPBOARD = registerBlock(Blocks.CHIPBOARD);
        public static final Item CHIPBOARD_DOOR = registerBlock(Blocks.CHIPBOARD_DOOR);

        public static final Item REINFORCED_STONE = registerBlock(Blocks.REINFORCED_STONE);
        public static final Item REINFORCED_GLASS = registerBlock(Blocks.REINFORCED_GLASS);
        public static final Item REINFORCED_STONE_STAIRS = registerBlock(Blocks.REINFORCED_STONE_STAIRS);
        public static final Item REINFORCED_STONE_SLAB = registerBlock(Blocks.REINFORCED_STONE_SLAB);
        public static final Item REINFORCED_DOOR = registerBlock(Blocks.REINFORCED_DOOR);
        public static final Item REINFORCED_TRAPDOOR = registerBlock(Blocks.REINFORCED_TRAPDOOR);

        private static final TagKey<Item> COPPER_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/copper"));
        public static final Item WRENCH = registerItem("wrench", WrenchItem::new, new Item.Properties() //
                .stacksTo(1) //
                .equippable(EquipmentSlot.MAINHAND) //
                .durability(256) //
                .enchantable(10) //
                .repairable(COPPER_INGOTS) //
                .component(DataComponents.TOOL, new Tool( //
                        List.of( //
                                Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)), //
                                Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(HBlockTags.WRENCH_MINEABLE), 20F) //
                        ), 1F, 1, true) //
                ));

        private static final ToolMaterial SILICON_BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, ToolMaterial.DIAMOND.durability(), 7, 2.0F, 10, ItemTags.SILICON_BRONZE_MATERIALS);
        public static final Item SILICON_BRONZE_SWORD = registerItem("silicon_bronze_sword", new Item.Properties().sword(SILICON_BRONZE, 3.0F, -2.4F));
        public static final Item SILICON_BRONZE_SHOVEL = registerItem("silicon_bronze_shovel", properties -> new ShovelItem(SILICON_BRONZE, 1.5F, -3.0F, properties));
        public static final Item SILICON_BRONZE_PICKAXE = registerItem("silicon_bronze_pickaxe", new Item.Properties().pickaxe(SILICON_BRONZE, 1.0F, -2.8F));
        public static final Item SILICON_BRONZE_AXE = registerItem("silicon_bronze_axe", properties -> new AxeItem(SILICON_BRONZE, 6.0F, -3.1F, properties));
        public static final Item SILICON_BRONZE_HOE = registerItem("silicon_bronze_hoe", properties -> new HoeItem(SILICON_BRONZE, -2.0F, -1.0F, properties));

        private static final ArmorMaterial FLAK_ARMOR = new ArmorMaterial( //
                33, ArmorMaterials.makeDefense(3, 6, 8, 3, 11), 10, net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC, 2.0F, 0.0F, ItemTags.FLAK_MATERIALS, modKey(EquipmentAssets.ROOT_ID, "flak") //
        );
        public static final Item FLAK_CHESTPLATE = registerItem("flak_chestplate", Item::new, new Item.Properties().humanoidArmor(FLAK_ARMOR, ArmorType.CHESTPLATE).stacksTo(1));

        private static Item.Properties createBatteryProperties(int capacity, int transferLimit) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(capacity, transferLimit)) //
                    .component(EnergyComponents.CAN_CHARGE_BLOCKS, Unit.INSTANCE);
        }

        public static final Item BATTERY = registerItem("battery", SimpleElectricItem::new, createBatteryProperties(10_000, 32));
        public static final Item ENERGY_CRYSTAL = registerItem("energy_crystal", SimpleElectricItem::new, createBatteryProperties(100_000, 128));
        public static final Item LAPOTRON_CRYSTAL = registerItem("lapotron_crystal", SimpleElectricItem::new, createBatteryProperties(1_000_000, 512).rarity(Rarity.RARE));

        public static final Item CHAINSAW = registerItem("chainsaw", SimpleElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, new Tool( //
                        List.of( //
                                Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)), //
                                Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(HBlockTags.CHAINSAW_MINEABLE), 0.5F) //
                        ), 0.5F, 0, false) //
                ) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(10_000, 32)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(9F, 50, 100)) //
                .component(EnergyComponents.ATTRIBUTES_WHEN_CHARGED, AttributesWhenCharged.tool(10, -3, 100)) //
        );

        private static Tool drillTool(TagKey<Block> incorrectBlocks) {
            return new Tool(List.of( //
                    Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(incorrectBlocks)), //
                    Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(HBlockTags.DRILL_MINEABLE), 0.5F) //
            ), 0.5F, 0, true);
        }

        public static final Item DRILL = registerItem("drill", SimpleElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, drillTool(BlockTags.INCORRECT_FOR_IRON_TOOL)) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(10_000, 32)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(7F, 50, 100)) //
                .component(EnergyComponents.ATTRIBUTES_WHEN_CHARGED, AttributesWhenCharged.tool(6, -3, 100)));
        public static final Item DIAMOND_DRILL = registerItem("diamond_drill", SimpleElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .rarity(Rarity.RARE) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, drillTool(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(10_000, 32)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(9F, 80, 160)) //
                .component(EnergyComponents.ATTRIBUTES_WHEN_CHARGED, AttributesWhenCharged.tool(8, -3, 160)));

        private static Item.Properties createBatteryPackProperties(int capacity, int transferLimit) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .component(DataComponents.EQUIPPABLE, //
                            Equippable.builder(ArmorType.CHESTPLATE.getSlot()) //
                                    .setAsset(modKey(EquipmentAssets.ROOT_ID, "battery_pack")) //
                                    .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(capacity, transferLimit)) //
                    .component(EnergyComponents.CHARGES_INVENTORY, Unit.INSTANCE);
        }

        public static final Item BATTERY_PACK = registerItem("battery_pack", SimpleElectricItem::new, createBatteryPackProperties(60_000, 32));
        public static final Item ADVANCED_BATTERY_PACK = registerItem("advanced_battery_pack", SimpleElectricItem::new, createBatteryPackProperties(300_000, 128));

        private static Item.Properties nanoArmorProperties(ArmorType type, int armor) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .rarity(Rarity.RARE) //
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()) //
                            .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                            .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(100_000, 128)) //
                    .component(EnergyComponents.ATTRIBUTES_WHEN_CHARGED, AttributesWhenCharged.armor(type, armor, 3, 100)) //
                    .component(EnergyComponents.ENERGY_ARMOR, new EnergyArmor(100));
        }

        public static final Item NANO_HELMET = registerItem("nano_helmet", SimpleElectricItem::new, nanoArmorProperties(ArmorType.HELMET, 3));
        public static final Item NANO_CHESTPLATE = registerItem("nano_chestplate", SimpleElectricItem::new, nanoArmorProperties(ArmorType.CHESTPLATE, 8));
        public static final Item NANO_LEGGINGS = registerItem("nano_leggings", SimpleElectricItem::new, nanoArmorProperties(ArmorType.LEGGINGS, 6));
        public static final Item NANO_BOOTS = registerItem("nano_boots", SimpleElectricItem::new, nanoArmorProperties(ArmorType.BOOTS, 3));

        private static Item.Properties quantumArmorProperties(ArmorType type, int armor) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .rarity(Rarity.RARE) //
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()) //
                            .setAsset(modKey(EquipmentAssets.ROOT_ID, "quantum")) //
                            .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(1_000_000, 512)) //
                    .component(EnergyComponents.ATTRIBUTES_WHEN_CHARGED, AttributesWhenCharged.armor(type, armor, 4, 100)) //
                    .component(EnergyComponents.ENERGY_ARMOR, new EnergyArmor(200)) //
                    .component(Components.QUANTUM_ARMOR, Unit.INSTANCE);
        }

        public static final Item QUANTUM_HELMET = registerItem("quantum_helmet", SimpleElectricItem::new, quantumArmorProperties(ArmorType.HELMET, 3));
        public static final Item QUANTUM_CHESTPLATE = registerItem("quantum_chestplate", SimpleElectricItem::new, quantumArmorProperties(ArmorType.CHESTPLATE, 8));
        public static final Item QUANTUM_LEGGINGS = registerItem("quantum_leggings", SimpleElectricItem::new, quantumArmorProperties(ArmorType.LEGGINGS, 6));
        public static final Item QUANTUM_BOOTS = registerItem("quantum_boots", SimpleElectricItem::new, quantumArmorProperties(ArmorType.BOOTS, 3));

        public static final Item WOOD_DUST = registerItem("wood_dust");
        public static final Item STONE_DUST = registerItem("stone_dust");
        public static final Item COAL_DUST = registerItem("coal_dust");
        public static final Item COPPER_DUST = registerItem("copper_dust");
        public static final Item IRON_DUST = registerItem("iron_dust");
        public static final Item GOLD_DUST = registerItem("gold_dust");
        public static final Item DIAMOND_DUST = registerItem("diamond_dust");
        public static final Item ENDER_PEARL_DUST = registerItem("ender_pearl_dust");
        public static final Item NETHER_STAR_DUST = registerItem("nether_star_dust", new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).rarity(Rarity.UNCOMMON));
        public static final Item SILICON_DUST = registerItem("silicon_dust");
        public static final Item SILICON_BRONZE_DUST = registerItem("silicon_bronze_dust");

        public static final Item RAW_SILICON = registerItem("raw_silicon");
        public static final Item REFINED_IRON_INGOT = registerItem("refined_iron_ingot");
        public static final Item SILICON_BRONZE_INGOT = registerItem("silicon_bronze_ingot");

        public static final Item STICKY_RESIN = registerItem("sticky_resin");
        public static final Item RUBBER = registerItem("rubber");
        public static final Item COPPER_WIRE = registerItem("copper_wire");
        public static final Item CIRCUIT = registerItem("circuit");
        public static final Item ADVANCED_CIRCUIT = registerItem("advanced_circuit", new Item.Properties().rarity(Rarity.RARE));
        public static final Item ELECTRIC_MOTOR = registerItem("electric_motor");
        public static final Item MIXED_METAL_INGOT = registerItem("mixed_metal_ingot");
        public static final Item COMPOSITE_PLATE = registerItem("composite_plate", new Item.Properties().rarity(Rarity.RARE));
        public static final Item CARBON_MESH = registerItem("carbon_mesh");
        public static final Item CARBON_PLATE = registerItem("carbon_plate", new Item.Properties().rarity(Rarity.RARE));

        public static final Item QUANTUM_PLATE = registerItem("quantum_plate", new Item.Properties().rarity(Rarity.UNCOMMON).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
        public static final Item COMPRESSED_PLANTS = registerItem("compressed_plants");
        public static final Item CANISTER = registerItem("canister");
        public static final Item NUTRIENT_PASTE = registerItem("nutrient_paste", new Item.Properties().food( //
                new FoodProperties(4, 8F, false), //
                Consumable.builder().onConsume( //
                        new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 300), 0.15F) //
                ).build()) //
        );

        public static final Item OVERCLOCK_UPGRADE = registerItem("overclock_upgrade", //
                props -> new ItemWithTooltip( //
                        props, //
                        Component.empty(), //
                        Component.translatable("hayo.upgrades.when_in_a_valid_machine").withStyle(ChatFormatting.GRAY), //
                        Component.translatable("hayo.upgrades.crafting_speed", "+100%").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.recipe_cost", "+25%").withStyle(ChatFormatting.DARK_AQUA) //
                ), new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static final Item CAPACITOR_UPGRADE = registerItem("capacitor_upgrade", //
                props -> new ItemWithTooltip( //
                        props, //
                        Component.empty(), //
                        Component.translatable("hayo.upgrades.when_in_a_valid_machine").withStyle(ChatFormatting.GRAY), //
                        Component.translatable("hayo.upgrades.energy_capacity", "+10000").withStyle(ChatFormatting.DARK_AQUA) //
                ), new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static final Item STREAMLINE_OVERHAUL_UPGRADE = registerItem("streamline_overhaul_upgrade", //
                props -> new ItemWithTooltip( //
                        props, //
                        Component.empty(), //
                        Component.translatable("hayo.upgrades.when_in_a_valid_machine").withStyle(ChatFormatting.GRAY), //
                        Component.translatable("hayo.upgrades.crafting_speed", "+300%").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.heat1").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.heat2").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.heat3").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.heat4").withStyle(ChatFormatting.DARK_AQUA) //
                ), new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static final Item BLASTING_UPGRADE = registerItem("blasting_upgrade", //
                props -> new ItemWithTooltip( //
                        props, //
                        Component.empty(), //
                        Component.translatable("hayo.upgrades.when_in_machine", Component.translatable("block.hayo.electric_furnace")).withStyle(ChatFormatting.GRAY), //
                        Component.translatable("hayo.upgrades.use_blasting_recipes").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.crafting_speed", "+100%").withStyle(ChatFormatting.DARK_AQUA) //
                ), new Item.Properties().rarity(Rarity.RARE).stacksTo(16) //
        );
        public static final Item SMOKING_UPGRADE = registerItem("smoking_upgrade", //
                props -> new ItemWithTooltip( //
                        props, //
                        Component.empty(), //
                        Component.translatable("hayo.upgrades.when_in_machine", Component.translatable("block.hayo.electric_furnace")).withStyle(ChatFormatting.GRAY), //
                        Component.translatable("hayo.upgrades.use_smoking_recipes").withStyle(ChatFormatting.DARK_AQUA), //
                        Component.translatable("hayo.upgrades.crafting_speed", "+100%").withStyle(ChatFormatting.DARK_AQUA) //
                ), //
                new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static void initialize() {
            CreativeModeTabEvents.modifyOutputEvent(modKey(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.accept(ELECTRIC_FURNACE);
                entries.accept(MACERATOR);
                entries.accept(COMPRESSOR);
                entries.accept(EXTRACTOR);
                entries.accept(MATTER_GENERATOR);

                entries.accept(GENERATOR);

                entries.accept(BATTERY_BOX);
                entries.accept(CRYSTAL_ENERGY_STORAGE);
                entries.accept(LAPOTRON_ENERGY_STORAGE);

                entries.accept(CABLE);
                entries.accept(POWER_CABLE);
                entries.accept(ENERGY_BUS);

                entries.accept(RUBBER_LOG);
                entries.accept(RESIN_PRODUCING_RUBBER_LOG);
                entries.accept(RUBBER_WOOD);
                entries.accept(STRIPPED_RUBBER_LOG);
                entries.accept(STRIPPED_RUBBER_WOOD);
                entries.accept(RUBBER_LEAVES);
                entries.accept(RUBBER_SAPLING);
                entries.accept(RUBBER_PLANKS);
                entries.accept(RUBBER_STAIRS);
                entries.accept(RUBBER_SLAB);
                entries.accept(RUBBER_DOOR);

                entries.accept(MACHINE_BLOCK);
                entries.accept(ADVANCED_MACHINE_BLOCK);
                entries.accept(SILICON_BRONZE_BLOCK);
                entries.accept(COMPOSITE_PLATE_BLOCK);
                entries.accept(RAW_SILICON_BLOCK);
                entries.accept(CHIPBOARD);
                entries.accept(CHIPBOARD_DOOR);
                entries.accept(REINFORCED_STONE);
                entries.accept(REINFORCED_STONE_STAIRS);
                entries.accept(REINFORCED_STONE_SLAB);
                entries.accept(REINFORCED_GLASS);
                entries.accept(REINFORCED_DOOR);
                entries.accept(REINFORCED_TRAPDOOR);

                entries.accept(WRENCH);
                entries.accept(SILICON_BRONZE_SWORD);
                entries.accept(SILICON_BRONZE_SHOVEL);
                entries.accept(SILICON_BRONZE_PICKAXE);
                entries.accept(SILICON_BRONZE_AXE);
                entries.accept(SILICON_BRONZE_HOE);
                entries.accept(FLAK_CHESTPLATE);

                entries.accept(BATTERY);
                entries.accept(EnergyComponents.withFullEnergy(BATTERY));
                entries.accept(ENERGY_CRYSTAL);
                entries.accept(EnergyComponents.withFullEnergy(ENERGY_CRYSTAL));
                entries.accept(LAPOTRON_CRYSTAL);
                entries.accept(EnergyComponents.withFullEnergy(LAPOTRON_CRYSTAL));

                entries.accept(CHAINSAW, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(CHAINSAW));
                entries.accept(DRILL, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(DRILL));
                entries.accept(DIAMOND_DRILL, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(DIAMOND_DRILL));

                entries.accept(BATTERY_PACK, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(BATTERY_PACK));
                entries.accept(ADVANCED_BATTERY_PACK, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(ADVANCED_BATTERY_PACK));

                entries.accept(NANO_HELMET, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(NANO_HELMET));
                entries.accept(NANO_CHESTPLATE, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(NANO_CHESTPLATE));
                entries.accept(NANO_LEGGINGS, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(NANO_LEGGINGS));
                entries.accept(NANO_BOOTS, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(NANO_BOOTS));

                entries.accept(QUANTUM_HELMET, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(QUANTUM_HELMET));
                entries.accept(QUANTUM_CHESTPLATE, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(QUANTUM_CHESTPLATE));
                entries.accept(QUANTUM_LEGGINGS, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(QUANTUM_LEGGINGS));
                entries.accept(QUANTUM_BOOTS, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(QUANTUM_BOOTS));

                entries.accept(WOOD_DUST);
                entries.accept(STONE_DUST);
                entries.accept(COAL_DUST);
                entries.accept(COPPER_DUST);
                entries.accept(IRON_DUST);
                entries.accept(GOLD_DUST);
                entries.accept(DIAMOND_DUST);
                entries.accept(ENDER_PEARL_DUST);
                entries.accept(NETHER_STAR_DUST);
                entries.accept(SILICON_DUST);
                entries.accept(SILICON_BRONZE_DUST);

                entries.accept(RAW_SILICON);
                entries.accept(REFINED_IRON_INGOT);
                entries.accept(SILICON_BRONZE_INGOT);
                entries.accept(STICKY_RESIN);
                entries.accept(RUBBER);
                entries.accept(COPPER_WIRE);
                entries.accept(CIRCUIT);
                entries.accept(ADVANCED_CIRCUIT);
                entries.accept(ELECTRIC_MOTOR);

                entries.accept(MIXED_METAL_INGOT);
                entries.accept(COMPOSITE_PLATE);
                entries.accept(CARBON_MESH);
                entries.accept(CARBON_PLATE);
                entries.accept(QUANTUM_PLATE);
                entries.accept(COMPRESSED_PLANTS);
                entries.accept(CANISTER);
                entries.accept(NUTRIENT_PASTE);

                entries.accept(OVERCLOCK_UPGRADE);
                entries.accept(CAPACITOR_UPGRADE);
                entries.accept(STREAMLINE_OVERHAUL_UPGRADE);
                entries.accept(BLASTING_UPGRADE);
                entries.accept(SMOKING_UPGRADE);
            });
        }

        public static Item registerBlock(Block block) {
            return registerBlock(block, BlockItem::new);
        }

        public static <T extends Block> Item registerBlock(T block, BiFunction<T, Item.Properties, Item> constructor) {
            return registerBlock(block, constructor, new Item.Properties());
        }

        public static Item registerBlock(Block block, Item.Properties properties) {
            return registerBlock(block, BlockItem::new, properties);
        }

        public static <T extends Block> Item registerBlock(T block, BiFunction<T, Item.Properties, Item> constructor, Item.Properties properties) {
            @SuppressWarnings("deprecation") var key = ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().identifier());

            return Registry.register(BuiltInRegistries.ITEM, key, constructor.apply(block, properties.setId(key).useBlockDescriptionPrefix()));
        }

        public static Item registerItem(String name) {
            return registerItem(name, Item::new);
        }

        public static Item registerItem(String name, Function<Item.Properties, Item> constructor) {
            return registerItem(name, constructor, new Item.Properties());
        }

        public static Item registerItem(String name, Item.Properties properties) {
            return registerItem(name, Item::new, properties);
        }

        public static Item registerItem(String name, Function<Item.Properties, Item> constructor, Item.Properties properties) {
            var key = ResourceKey.create(Registries.ITEM, modId(name));
            return Registry.register(BuiltInRegistries.ITEM, key, constructor.apply(properties.setId(key)));
        }
    }

    public static class ItemTags {
        public static final TagKey<Item> SILICON_BRONZE_MATERIALS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/silicon_bronze"));
        public static final TagKey<Item> FLAK_MATERIALS = TagKey.create(Registries.ITEM, modId("flak_materials"));
        public static final TagKey<Item> DISABLED_GENERATOR_FUELS = TagKey.create(Registries.ITEM, modId("disabled_generator_fuels"));

        public static final TagKey<Item> ELECTRIC_FURNACE_UPGRADES = TagKey.create(Registries.ITEM, modId("electric_furnace_upgrades"));
        public static final TagKey<Item> MACERATOR_UPGRADES = TagKey.create(Registries.ITEM, modId("macerator_upgrades"));
        public static final TagKey<Item> COMPRESSOR_UPGRADES = TagKey.create(Registries.ITEM, modId("compressor_upgrades"));
        public static final TagKey<Item> EXTRACTOR_UPGRADES = TagKey.create(Registries.ITEM, modId("extractor_upgrades"));

        public static final TagKey<Item> BATTERY_BOX_BATTERIES = TagKey.create(Registries.ITEM, modId("battery_box_batteries"));
    }

    public static class BlockEntityTypes {
        public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = register("generator", GeneratorBlockEntity::new, Blocks.GENERATOR);
        public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlockEntity::new, Blocks.ELECTRIC_FURNACE);
        public static final BlockEntityType<MaceratorBlockEntity> MACERATOR = register("macerator", MaceratorBlockEntity::new, Blocks.MACERATOR);
        public static final BlockEntityType<CompressorBlockEntity> COMPRESSOR = register("compressor", CompressorBlockEntity::new, Blocks.COMPRESSOR);
        public static final BlockEntityType<ExtractorBlockEntity> EXTRACTOR = register("extractor", ExtractorBlockEntity::new, Blocks.EXTRACTOR);
        public static final BlockEntityType<MatterGeneratorBlockEntity> MATTER_GENERATOR = register("matter_generator", MatterGeneratorBlockEntity::new, Blocks.MATTER_GENERATOR);
        public static final BlockEntityType<BatteryBoxBlockEntity> BATTERY_BOX = register("battery_box", BatteryBoxBlockEntity::new, Blocks.BATTERY_BOX);
        public static final BlockEntityType<CrystalEnergyStorageBlockEntity> CRYSTAL_ENERGY_STORAGE = register("crystal_energy_storage", CrystalEnergyStorageBlockEntity::new, Blocks.CRYSTAL_ENERGY_STORAGE);
        public static final BlockEntityType<LapotronEnergyStorageBlockEntity> LAPOTRON_ENERGY_STORAGE = register("lapotron_energy_storage", LapotronEnergyStorageBlockEntity::new, Blocks.LAPOTRON_ENERGY_STORAGE);

        public static void initialize() {
        }

        public static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<T> constructor, Block... blocks) {
            var type = FabricBlockEntityTypeBuilder.create(constructor, blocks).build();
            return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, modId(name), type);
        }
    }

    public static class MenuTypes {
        @SuppressWarnings("DataFlowIssue")
        public static final ExtendedMenuType<UniversalContainerMenu, BlockPos> UNIVERSAL = register("universal", (containerId, inventory, pos) -> {
            var level = inventory.player.level();
            var blockState = level.getBlockState(pos);
            var provider = blockState.getMenuProvider(level, pos);
            if (provider == null) {
                return null;
            }

            return (UniversalContainerMenu) provider.createMenu(containerId, inventory, inventory.player);
        }, BlockPos.STREAM_CODEC);

        public static final MenuType<GeneratorMenu> GENERATOR = register("generator", GeneratorMenu::new);
        public static final MenuType<MatterGeneratorMenu> MATTER_GENERATOR = register("matter_generator", MatterGeneratorMenu::new);
        public static final MenuType<EnergyStorageMenu> ENERGY_STORAGE = register("energy_storage", EnergyStorageMenu::new);

        public static void initialize() {
        }

        public static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> constructor) {
            return Registry.register(BuiltInRegistries.MENU, modId(name), new MenuType<>(constructor, FeatureFlags.VANILLA_SET));
        }

        private static <T extends AbstractContainerMenu, D> ExtendedMenuType<T, D> register(String name, ExtendedMenuType.ExtendedFactory<T, D> constructor, StreamCodec<? super RegistryFriendlyByteBuf, D> codec) {
            return Registry.register(BuiltInRegistries.MENU, modId(name), new ExtendedMenuType<>(constructor, codec));
        }

        public static void initializeClient() {
            MenuScreens.register(UNIVERSAL, UniversalContainerScreen::new);

            MenuScreens.register(GENERATOR, GeneratorScreen::new);
            MenuScreens.register(MATTER_GENERATOR, MatterGeneratorScreen::new);
            MenuScreens.register(ENERGY_STORAGE, EnergyStorageScreen::new);
        }
    }

    public static class FoliagePlacerTypes {
        public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER = register("rubber_foliage_placer", RubberFoliagePlacer.CODEC);

        public static void initialize() {
        }

        public static <T extends FoliagePlacer> FoliagePlacerType<T> register(String name, MapCodec<T> codec) {
            var type = new FoliagePlacerType<>(codec);

            return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, modId(name), type);
        }
    }

    public static class RecipeTypes {
        public static final RecipeType<MaceratingRecipe> MACERATING = register("macerating");
        public static final RecipeType<CompressingRecipe> COMPRESSING = register("compressing");
        public static final RecipeType<ExtractingRecipe> EXTRACTING = register("extracting");
        public static final RecipeType<MatterGeneratingRecipe> MATTER_GENERATING = register("matter_generating");

        public static void initialize() {
        }

        private static <T extends Recipe<?>> RecipeType<T> register(String name) {
            var type = new RecipeType<T>() {
                @Override
                public String toString() {
                    return MOD_ID + ":" + name;
                }
            };

            return Registry.register(BuiltInRegistries.RECIPE_TYPE, modId(name), type);
        }
    }

    public static class RecipeSerializers {

        public static final RecipeSerializer<MaceratingRecipe> MACERATING = register("macerating", ClassicMachineRecipe.createCodec(MaceratingRecipe::new, MaceratingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<CompressingRecipe> COMPRESSING = register("compressing", ClassicMachineRecipe.createCodec(CompressingRecipe::new, CompressorBlockEntity.DEFAULT_RECIPE_ENERGY));
        public static final RecipeSerializer<ExtractingRecipe> EXTRACTING = register("extracting", ClassicMachineRecipe.createCodec(ExtractingRecipe::new, ExtractingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<MatterGeneratingRecipe> MATTER_GENERATING = register("matter_generating", new RecipeSerializer<>(MatterGeneratingRecipe.CODEC, MatterGeneratingRecipe.STREAM_CODEC));

        public static void initialize() {
        }

        private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
            return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modId(name), serializer);
        }
    }

    public static final class SoundEvents {
        public static final SoundEvent STICKY_RESIN_GATHER = register("sticky_resin_gather");
        public static final SoundEvent WRENCH = register("wrench");

        public static void initialize() {
        }

        private static SoundEvent register(String name) {
            return Registry.register(BuiltInRegistries.SOUND_EVENT, modId(name), SoundEvent.createVariableRangeEvent(modId(name)));
        }
    }
}