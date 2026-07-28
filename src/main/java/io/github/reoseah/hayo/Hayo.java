package io.github.reoseah.hayo;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.base.item.BlockItemWithTooltip;
import io.github.reoseah.hayo.base.item.UpgradeItem;
import io.github.reoseah.hayo.feature.battery_box.BatteryBoxBlock;
import io.github.reoseah.hayo.feature.battery_box.BatteryBoxBlockEntity;
import io.github.reoseah.hayo.feature.battery_box.BatteryBoxMenu;
import io.github.reoseah.hayo.feature.battery_box.BatteryBoxScreen;
import io.github.reoseah.hayo.feature.cable.CableBlock;
import io.github.reoseah.hayo.feature.electric_beacon.*;
import io.github.reoseah.hayo.feature.energy.ElectricShapedRecipe;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricBlockManager;
import io.github.reoseah.hayo.feature.energy.blocks.OverloadCablePayload;
import io.github.reoseah.hayo.feature.energy.item.*;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageScreen;
import io.github.reoseah.hayo.feature.energy_storages.advanced.AdvancedEnergyStorageBlock;
import io.github.reoseah.hayo.feature.energy_storages.advanced.AdvancedEnergyStorageBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.advanced.AdvancedEnergyStorageMenu;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayBlock;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayMenu;
import io.github.reoseah.hayo.feature.generator.GeneratorBlock;
import io.github.reoseah.hayo.feature.generator.GeneratorBlockEntity;
import io.github.reoseah.hayo.feature.generator.GeneratorMenu;
import io.github.reoseah.hayo.feature.generator.GeneratorScreen;
import io.github.reoseah.hayo.feature.machines.ClassicMachineRecipe;
import io.github.reoseah.hayo.feature.machines.ClassicMachineScreen;
import io.github.reoseah.hayo.feature.machines.MachineMenu;
import io.github.reoseah.hayo.feature.machines.compressor.CompressingRecipe;
import io.github.reoseah.hayo.feature.machines.compressor.CompressorBlock;
import io.github.reoseah.hayo.feature.machines.compressor.CompressorBlockEntity;
import io.github.reoseah.hayo.feature.machines.compressor.CompressorMenu;
import io.github.reoseah.hayo.feature.machines.electric_furnace.ElectricFurnaceBlock;
import io.github.reoseah.hayo.feature.machines.electric_furnace.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayo.feature.machines.electric_furnace.ElectricFurnaceMenu;
import io.github.reoseah.hayo.feature.machines.extractor.ExtractingRecipe;
import io.github.reoseah.hayo.feature.machines.extractor.ExtractorBlock;
import io.github.reoseah.hayo.feature.machines.extractor.ExtractorBlockEntity;
import io.github.reoseah.hayo.feature.machines.extractor.ExtractorMenu;
import io.github.reoseah.hayo.feature.machines.macerator.MaceratingRecipe;
import io.github.reoseah.hayo.feature.machines.macerator.MaceratorBlock;
import io.github.reoseah.hayo.feature.machines.macerator.MaceratorBlockEntity;
import io.github.reoseah.hayo.feature.machines.macerator.MaceratorMenu;
import io.github.reoseah.hayo.feature.machines.matter_generator.*;
import io.github.reoseah.hayo.feature.ore_crops.FerruBlock;
import io.github.reoseah.hayo.feature.quantum_armor.QuantumArmorRenderer;
import io.github.reoseah.hayo.feature.rubber_tree.ResinYieldingLogBlock;
import io.github.reoseah.hayo.feature.rubber_tree.RubberFoliagePlacer;
import io.github.reoseah.hayo.feature.wrench.WrenchItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Tool;
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
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.world.level.block.Blocks.leavesProperties;
import static net.minecraft.world.level.block.Blocks.logProperties;

// TODO: rename Battery Box to Battery Buffer?
// TODO: rearrange creative tab entries
// TODO: make resin rubber log "eject" resin item instead of placing it directly into player inventory
public class Hayo {
    public static final String MOD_ID = "hayo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CreativeModeTab TAB = FabricCreativeModeTab.builder().title(Component.translatable("itemGroup.hayo")).icon(() -> new ItemStack(Blocks.ELECTRIC_FURNACE)).build();

    public static final SavedDataType<ElectricBlockManager> ELECTRIC_DATA = new SavedDataType<>(
            modId("electric_data"), //
            ElectricBlockManager::new, //
            MapCodec.unitCodec(ElectricBlockManager::new), //
            null);

    public static final AttachmentType<ElectricBlockManager.ChunkData> CHUNK_ELECTRIC_DATA = AttachmentRegistry.create( //
            modId("electric_blocks"), //
            builder -> builder //
                    .initializer(ElectricBlockManager.ChunkData::new) //
                    .persistent(ElectricBlockManager.ChunkData.CODEC.codec()));

    public static final TreeGrower RUBBER_TREE = new TreeGrower( //
            "hayo:rubber_tree", //
            0F, //
            Optional.empty(), //
            Optional.empty(), //
            Optional.of(modKey(Registries.CONFIGURED_FEATURE, "rubber_tree")), //
            Optional.empty(), //
            Optional.empty(), //
            Optional.empty());

    public static final TagKey<Item> COPPER_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/copper"));

    public static final ModelLayerLocation QUANTUM_ARMOR = new ModelLayerLocation(modId("quantum_armor"), "main");

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modId("main"), TAB);

        EnergyComponents.initialize();

        Blocks.initialize();
        Items.initialize();
        BlockEntityTypes.initialize();
        MenuTypes.initialize();
        FoliagePlacerTypes.initialize();
        RecipeTypes.initialize();
        RecipeSerializers.initialize();
        Particles.initialize();

        CustomPayloads.initialize();
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MACERATING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.COMPRESSING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.EXTRACTING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MATTER_GENERATING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.ELECTRIC_SHAPED_CRAFTING);

        BiomeModifications.create(modId("rubber_trees")) //
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(BiomeTags.IS_FOREST) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE)) //
                                .or(BiomeSelectors.includeByKey(Biomes.SWAMP)), //
                        (selection, modification) -> {
                            var feature = modKey(Registries.PLACED_FEATURE, "rubber_tree_patch");
                            modification.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, feature);
                        });

        ServerChunkEvents.CHUNK_LOAD.register((level, chunk, generated) -> {
            ElectricBlockManager.get(level).onChunkLoad(chunk);
        });
        ServerChunkEvents.CHUNK_UNLOAD.register((level, chunk) -> {
            ElectricBlockManager.get(level).onChunkUnload(chunk);
        });
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            ElectricBlockManager.get(level).onLevelTickEnd();
        });
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
//        ChunkSectionLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, Blocks.REINFORCED_GLASS, Blocks.REINFORCED_DOOR, Blocks.CHIPBOARD_DOOR, Blocks.RUBBER_LEAVES, Blocks.RUBBER_SAPLING, Blocks.FERRU, Blocks.ELECTRIC_BEACON);

        BlockColorRegistry.register((state, level, pos, tintValues) -> tintValues.add(level != null && pos != null ? BiomeColors.getAverageFoliageColor(level, pos) : 0xff48b518), Blocks.RUBBER_LEAVES);

        RangeSelectItemModelProperties.ID_MAPPER.put(modId("energy"), EnergyModelProperty.MAP_CODEC);

        MenuTypes.initializeClient();
        Particles.initializeClient();
        CustomPayloads.initializeClient();

        ModelLayerRegistry.registerModelLayer(QUANTUM_ARMOR, QuantumArmorRenderer.QuantumGlowModel::createLayerDefinition);
        ArmorRenderer.register(QuantumArmorRenderer::new, Items.QUANTUM_CHESTPLATE);
    }

    public static Identifier modId(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> modKey(ResourceKey<? extends Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, modId(location));
    }

    public static class Blocks {
        private static final BlockBehaviour.Properties MACHINES = BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).mapColor(MapColor.METAL);
        public static final Block GENERATOR = register("generator", GeneratorBlock::new, MACHINES);
        public static final Block ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlock::new, MACHINES);
        public static final Block MACERATOR = register("macerator", MaceratorBlock::new, MACHINES);
        public static final Block COMPRESSOR = register("compressor", CompressorBlock::new, MACHINES);
        public static final Block EXTRACTOR = register("extractor", ExtractorBlock::new, MACHINES);
        public static final Block MATTER_GENERATOR = register("matter_generator", MatterGeneratorBlock::new, MACHINES);
        public static final Block BATTERY_BOX = register("battery_box", BatteryBoxBlock::new, MACHINES);
        public static final Block ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayBlock::new, MACHINES);
        public static final Block ADVANCED_ENERGY_STORAGE = register("advanced_energy_storage", AdvancedEnergyStorageBlock::new, MACHINES);
        public static final Block ELECTRIC_BEACON = register("electric_beacon", ElectricBeaconBlock::new, BlockBehaviour.Properties.of().strength(5F, 30F).lightLevel(s -> 15).noOcclusion().sound(SoundType.GLASS));

        public static final CableBlock CABLE = register("cable", properties -> new CableBlock(32, 2, properties), BlockBehaviour.Properties.of().strength(.5F, 3).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final CableBlock POWER_CABLE = register("power_cable", properties -> new CableBlock(128, 3, properties), BlockBehaviour.Properties.of().strength(.75F, 6).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final CableBlock ADVANCED_ENERGY_CONDUIT = register("advanced_energy_conduit", properties -> new CableBlock(512, 5, properties), BlockBehaviour.Properties.of().strength(1F, 15).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY));

        public static final Block RUBBER_LOG = register("rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD));
        public static final Block RESIN_YIELDING_RUBBER_LOG = register("resin_yielding_rubber_log", ResinYieldingLogBlock::new, BlockBehaviour.Properties.of().randomTicks().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
        public static final Block RUBBER_WOOD = register("rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_LOG = register("stripped_rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_WOOD = register("stripped_rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block RUBBER_LEAVES = register("rubber_leaves", properties -> new TintedParticleLeavesBlock(0.01F, properties), leavesProperties(SoundType.GRASS));
        public static final Block RUBBER_SAPLING = register("rubber_sapling", properties -> new SaplingBlock(RUBBER_TREE, properties), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));

        public static final BlockBehaviour.Properties RUBBER_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
        public static final Block RUBBER_PLANKS = register("rubber_planks", Block::new, RUBBER_PROPERTIES);
        public static final Block RUBBER_STAIRS = register("rubber_stairs", props -> new StairBlock(RUBBER_PLANKS.defaultBlockState(), props), RUBBER_PROPERTIES);
        public static final Block RUBBER_SLAB = register("rubber_slab", SlabBlock::new, RUBBER_PROPERTIES);

        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, MACHINES);
        public static final Block ADVANCED_MACHINE_BLOCK = register("advanced_machine_block", Block::new, MACHINES);
        public static final Block SILICON_BRONZE_BLOCK = register("silicon_bronze_block", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).mapColor(MapColor.COLOR_ORANGE));
        public static final Block COMPOSITE_PLATE_BLOCK = register("composite_plate_block", Block::new, BlockBehaviour.Properties.of().strength(5F, 30F).sound(SoundType.METAL).mapColor(MapColor.COLOR_GREEN));
        public static final Block RAW_SILICON_BLOCK = register("raw_silicon_block", Block::new, BlockBehaviour.Properties.of().strength(3F).mapColor(MapColor.COLOR_BLACK));

        public static final Block CHIPBOARD = register("chipboard", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.WOOD).mapColor(MapColor.WOOD));
        public static final Block CHIPBOARD_DOOR = register("chipboard_door", props -> new DoorBlock(BlockSetType.OAK, props), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

        private static final BlockBehaviour.Properties REINFORCED_BLOCKS = BlockBehaviour.Properties.of().strength(3F, 30F).sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE);
        public static final Block REINFORCED_STONE = register("reinforced_stone", Block::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_GLASS = register("reinforced_glass", TransparentBlock::new, BlockBehaviour.Properties.of().strength(3F, 20F).noOcclusion().sound(SoundType.GLASS));
        public static final Block REINFORCED_STONE_STAIRS = register("reinforced_stone_stairs", props -> new StairBlock(REINFORCED_STONE.defaultBlockState(), props), REINFORCED_BLOCKS);
        public static final Block REINFORCED_STONE_SLAB = register("reinforced_stone_slab", SlabBlock::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_DOOR = register("reinforced_door", props -> new DoorBlock(BlockSetType.IRON, props), BlockBehaviour.Properties.of().strength(3F, 20F).noOcclusion().sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE));

        public static final Block FERRU = register("ferru", props -> new FerruBlock(TagKey.create(Registries.ITEM, modId("ferru_fertilizers")), props), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));

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
        public static final TagKey<Block> WRENCHABLE = TagKey.create(Registries.BLOCK, modId("rotatable_with_wrench"));
    }

    public static class Items {
        private static final HolderGetter<Block> BLOCK_LOOKUP = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);

        public static final Item GENERATOR = registerBlock(Blocks.GENERATOR);
        public static final Item ELECTRIC_FURNACE = registerBlock(Blocks.ELECTRIC_FURNACE);
        public static final Item MACERATOR = registerBlock(Blocks.MACERATOR);
        public static final Item COMPRESSOR = registerBlock(Blocks.COMPRESSOR);
        public static final Item EXTRACTOR = registerBlock(Blocks.EXTRACTOR);
        public static final Item MATTER_GENERATOR = registerBlock(Blocks.MATTER_GENERATOR, new Item.Properties().rarity(Rarity.EPIC));
        public static final Item BATTERY_BOX = registerBlock(Blocks.BATTERY_BOX);
        public static final Item ENERGY_CRYSTAL_ARRAY = registerBlock(Blocks.ENERGY_CRYSTAL_ARRAY, new Item.Properties().rarity(Rarity.RARE));
        public static final Item ADVANCED_ENERGY_STORAGE = registerBlock(Blocks.ADVANCED_ENERGY_STORAGE, new Item.Properties().rarity(Rarity.RARE));
        public static final Item ELECTRIC_BEACON = registerBlock(Blocks.ELECTRIC_BEACON, new Item.Properties().rarity(Rarity.RARE));

        public static final Item CABLE = registerBlock(Blocks.CABLE, BlockItemWithTooltip::new);
        public static final Item POWER_CABLE = registerBlock(Blocks.POWER_CABLE, BlockItemWithTooltip::new);
        public static final Item ADVANCED_ENERGY_CONDUIT = registerBlock(Blocks.ADVANCED_ENERGY_CONDUIT, BlockItemWithTooltip::new);

        public static final Item RUBBER_LOG = registerBlock(Blocks.RUBBER_LOG);
        public static final Item RESIN_YIELDING_RUBBER_LOG = registerBlock(Blocks.RESIN_YIELDING_RUBBER_LOG);
        public static final Item RUBBER_WOOD = registerBlock(Blocks.RUBBER_WOOD);
        public static final Item STRIPPED_RUBBER_LOG = registerBlock(Blocks.STRIPPED_RUBBER_LOG);
        public static final Item STRIPPED_RUBBER_WOOD = registerBlock(Blocks.STRIPPED_RUBBER_WOOD);
        public static final Item RUBBER_LEAVES = registerBlock(Blocks.RUBBER_LEAVES);
        public static final Item RUBBER_SAPLING = registerBlock(Blocks.RUBBER_SAPLING);
        public static final Item RUBBER_PLANKS = registerBlock(Blocks.RUBBER_PLANKS);
        public static final Item RUBBER_STAIRS = registerBlock(Blocks.RUBBER_STAIRS);
        public static final Item RUBBER_SLAB = registerBlock(Blocks.RUBBER_SLAB);

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

        public static final Item FERRU_SEEDS = registerItem("ferru_seeds", props -> new BlockItem(Blocks.FERRU, props), new Item.Properties().useItemDescriptionPrefix());

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

        public static final Item CHAINSAW = registerItem("chainsaw", ElectricItem::new, new Item.Properties() //
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
                .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.tool(10, -3, 100)) //
        );

        private static Tool drillTool(TagKey<Block> incorrectBlocks) {
            return new Tool(List.of( //
                    Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(incorrectBlocks)), //
                    Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(HBlockTags.DRILL_MINEABLE), 0.5F) //
            ), 0.5F, 0, true);
        }

        public static final Item DRILL = registerItem("drill", ElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, drillTool(BlockTags.INCORRECT_FOR_IRON_TOOL)) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(10_000, 32)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(7F, 50, 100)) //
                .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.tool(6, -3, 100)));
        public static final Item DIAMOND_DRILL = registerItem("diamond_drill", ElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .rarity(Rarity.RARE) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, drillTool(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(10_000, 32)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(9F, 80, 160)) //
                .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.tool(8, -3, 160)));

        public static final Item ADVANCED_DRILL = registerItem("advanced_drill", ElectricItem::new, new Item.Properties() //
                .stacksTo(1) //
                .rarity(Rarity.RARE) //
                .equippable(EquipmentSlot.MAINHAND) //
                .component(DataComponents.TOOL, drillTool(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)) //
                .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(60_000, 128)) //
                .component(EnergyComponents.ENERGY_TOOL, new EnergyTool(12F, 160, 320)) //
                .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.tool(9, -3, 320)));

        private static final ToolMaterial SILICON_BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, ToolMaterial.DIAMOND.durability(), 7, 2.0F, 10, ItemTags.SILICON_BRONZE_MATERIALS);
        public static final Item SILICON_BRONZE_SWORD = registerItem("silicon_bronze_sword", new Item.Properties().sword(SILICON_BRONZE, 3.0F, -2.4F));
        public static final Item SILICON_BRONZE_SHOVEL = registerItem("silicon_bronze_shovel", properties -> new ShovelItem(SILICON_BRONZE, 1.5F, -3.0F, properties));
        public static final Item SILICON_BRONZE_PICKAXE = registerItem("silicon_bronze_pickaxe", new Item.Properties().pickaxe(SILICON_BRONZE, 1.0F, -2.8F));
        public static final Item SILICON_BRONZE_AXE = registerItem("silicon_bronze_axe", properties -> new AxeItem(SILICON_BRONZE, 6.0F, -3.1F, properties));
        public static final Item SILICON_BRONZE_HOE = registerItem("silicon_bronze_hoe", properties -> new HoeItem(SILICON_BRONZE, -2.0F, -1.0F, properties));

        private static final ArmorMaterial FLAK_ARMOR = new ArmorMaterial( //
                33, ArmorMaterials.makeDefense(3, 6, 8, 3, 11), 10, SoundEvents.ARMOR_EQUIP_GENERIC, 2.0F, 0.0F, ItemTags.FLAK_MATERIALS, modKey(EquipmentAssets.ROOT_ID, "flak") //
        );
        public static final Item FLAK_CHESTPLATE = registerItem("flak_chestplate", Item::new, new Item.Properties().humanoidArmor(FLAK_ARMOR, ArmorType.CHESTPLATE).stacksTo(1));

        private static Item.Properties nanoArmorProperties(ArmorType type, int armor) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .rarity(Rarity.RARE) //
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()) //
                            .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                            .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(100_000, 128)) //
                    .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.armor(type, armor, 3, 100)) //
                    .component(EnergyComponents.ENERGY_ARMOR, new EnergyArmor(100));
        }

        public static final Item NANO_HELMET = registerItem("nano_helmet", ElectricItem::new, nanoArmorProperties(ArmorType.HELMET, 3));
        public static final Item NANO_CHESTPLATE = registerItem("nano_chestplate", ElectricItem::new, nanoArmorProperties(ArmorType.CHESTPLATE, 8));
        public static final Item NANO_LEGGINGS = registerItem("nano_leggings", ElectricItem::new, nanoArmorProperties(ArmorType.LEGGINGS, 6));
        public static final Item NANO_BOOTS = registerItem("nano_boots", ElectricItem::new, nanoArmorProperties(ArmorType.BOOTS, 3));

        private static Item.Properties quantumArmorProperties(ArmorType type, int armor) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .rarity(Rarity.RARE) //
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(type.getSlot()) //
                            .setAsset(modKey(EquipmentAssets.ROOT_ID, "quantum")) //
                            .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(1_000_000, 512)) //
                    .component(EnergyComponents.CHARGED_ATTRIBUTES, ChargedAttributes.armor(type, armor, 4, 100)) //
                    .component(EnergyComponents.ENERGY_ARMOR, new EnergyArmor(200)) //
                    .component(EnergyComponents.QUANTUM_ARMOR, Unit.INSTANCE);
        }

        public static final Item QUANTUM_HELMET = registerItem("quantum_helmet", ElectricItem::new, quantumArmorProperties(ArmorType.HELMET, 3));
        public static final Item QUANTUM_CHESTPLATE = registerItem("quantum_chestplate", ElectricItem::new, quantumArmorProperties(ArmorType.CHESTPLATE, 8));
        public static final Item QUANTUM_LEGGINGS = registerItem("quantum_leggings", ElectricItem::new, quantumArmorProperties(ArmorType.LEGGINGS, 6));
        public static final Item QUANTUM_BOOTS = registerItem("quantum_boots", ElectricItem::new, quantumArmorProperties(ArmorType.BOOTS, 3));

        private static Item.Properties createBatteryPackProperties(int capacity, int transferLimit) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .component(DataComponents.EQUIPPABLE, //
                            Equippable.builder(ArmorType.CHESTPLATE.getSlot()) //
                                    .setAsset(modKey(EquipmentAssets.ROOT_ID, "battery_pack")) //
                                    .build()) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(capacity, transferLimit)) //
                    .component(EnergyComponents.ENERGY_BACKPACK, Unit.INSTANCE);
        }

        public static final Item BATTERY_PACK = registerItem("battery_pack", ElectricItem::new, createBatteryPackProperties(60_000, 32));
        public static final Item ADVANCED_BATTERY_PACK = registerItem("advanced_battery_pack", ElectricItem::new, createBatteryPackProperties(300_000, 128));

        private static Item.Properties createBatteryProperties(int capacity, int transferLimit) {
            return new Item.Properties() //
                    .stacksTo(1) //
                    .component(EnergyComponents.ENERGY_STORAGE, new EnergyStorage(capacity, transferLimit)) //
                    .component(EnergyComponents.BATTERY, Unit.INSTANCE);
        }

        public static final Item BATTERY = registerItem("battery", ElectricItem::new, createBatteryProperties(10_000, 32));
        public static final Item ENERGY_CRYSTAL = registerItem("energy_crystal", ElectricItem::new, createBatteryProperties(100_000, 128));

        public static final Item REFINED_IRON_INGOT = registerItem("refined_iron_ingot");
        public static final Item SILICON_BRONZE_INGOT = registerItem("silicon_bronze_ingot");
        public static final Item RAW_SILICON = registerItem("raw_silicon");

        public static final Item WOOD_DUST = registerItem("wood_dust");
        public static final Item STONE_DUST = registerItem("stone_dust");
        public static final Item COAL_DUST = registerItem("coal_dust");
        public static final Item COPPER_DUST = registerItem("copper_dust");
        public static final Item IRON_DUST = registerItem("iron_dust");
        public static final Item GOLD_DUST = registerItem("gold_dust");
        public static final Item DIAMOND_DUST = registerItem("diamond_dust");
        public static final Item ENDER_PEARL_DUST = registerItem("ender_pearl_dust");
        public static final Item SILICON_DUST = registerItem("silicon_dust");
        public static final Item SILICON_BRONZE_DUST = registerItem("silicon_bronze_dust");

        public static final Item STICKY_RESIN = registerItem("sticky_resin");
        public static final Item RUBBER = registerItem("rubber");
        public static final Item COPPER_WIRE = registerItem("copper_wire");
        public static final Item GLASS_FIBER = registerItem("glass_fiber");
        public static final Item CIRCUIT = registerItem("circuit");
        public static final Item ELECTRIC_MOTOR = registerItem("electric_motor");
        public static final Item DATA_CIRCUIT = registerItem("data_circuit", new Item.Properties().rarity(Rarity.RARE));
        public static final Item ENERGY_FLOW_CIRCUIT = registerItem("energy_flow_circuit", new Item.Properties().rarity(Rarity.RARE));
        public static final Item MIXED_METAL_INGOT = registerItem("mixed_metal_ingot");
        public static final Item COMPOSITE_PLATE = registerItem("composite_plate", new Item.Properties().rarity(Rarity.RARE));
        public static final Item CARBON_REDSTONE_MATRIX = registerItem("carbon_redstone_matrix");
        public static final Item CONDUCTIVE_CARBON = registerItem("conductive_carbon", new Item.Properties().rarity(Rarity.RARE));
        public static final Item FORCICIUM_SUNNARIUM_COMPLEX = registerItem("forcicium_sunnarium_complex", new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).rarity(Rarity.UNCOMMON));
        public static final Item QUANTUM_PLATE = registerItem("quantum_plate", new Item.Properties().rarity(Rarity.UNCOMMON));
        public static final Item COMPRESSED_PLANTS = registerItem("compressed_plants");
        public static final Item CANISTER = registerItem("canister");
        public static final Item DENSE_REFINED_IRON_PLATE = registerItem("dense_refined_iron_plate");
        public static final Item REINFORCED_IRRADIANT_CORE = registerItem("reinforced_irradiant_core");

        public static final Item OVERCLOCK_UPGRADE = registerItem("overclock_upgrade", props -> new UpgradeItem(props, List.of( //
                Component.translatable("hayo.upgrades.crafting_speed", "+100%"), //
                Component.translatable("hayo.upgrades.energy_usage", "+100%"), //
                Component.translatable("hayo.upgrades.recipe_cost", "+25%") //
        )), new Item.Properties().rarity(Rarity.RARE).stacksTo(16));
        public static final Item CAPACITOR_UPGRADE = registerItem("capacitor_upgrade", props -> new UpgradeItem(props, List.of(Component.translatable("hayo.upgrades.energy_capacity", "+10000"))), new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static final Item BLASTING_UPGRADE = registerItem("blasting_upgrade", //
                props -> new UpgradeItem( //
                        props, //
                        Component.translatable("block.hayo.electric_furnace"), //
                        List.of( //
                                Component.translatable("hayo.upgrades.use_blasting_recipes"), //
                                Component.translatable("hayo.upgrades.crafting_speed", "+100%"), //
                                Component.translatable("hayo.upgrades.energy_usage", "+100%") //
                        ) //
                ), //
                new Item.Properties().rarity(Rarity.RARE).stacksTo(16) //
        );
        public static final Item SMOKING_UPGRADE = registerItem("smoking_upgrade", //
                props -> new UpgradeItem( //
                        props, //
                        Component.translatable("block.hayo.electric_furnace"), //
                        List.of( //
                                Component.translatable("hayo.upgrades.use_smoking_recipes"), //
                                Component.translatable("hayo.upgrades.crafting_speed", "+100%"), //
                                Component.translatable("hayo.upgrades.energy_usage", "+100%") //
                        ) //
                ), //
                new Item.Properties().rarity(Rarity.RARE).stacksTo(16));
        public static final Item INDUCTION_UPGRADE = registerItem("induction_upgrade", //
                props -> new UpgradeItem( //
                        props, //
                        Component.translatable("block.hayo.electric_furnace"), //
                        List.of( //
                                Component.translatable("hayo.upgrades.scaling_with_heat"), //
                                Component.translatable("hayo.upgrades.heat_capacity", 10000), //
                                Component.translatable("hayo.upgrades.heat_when_active", "+1"), //
                                Component.translatable("hayo.upgrades.heat_when_idle", "-4"), //
                                Component.translatable("hayo.upgrades.crafting_speed", "+300%"), //
                                Component.translatable("hayo.upgrades.energy_usage", "+300%") //
                        ) //
                ), //
                new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static void initialize() {
            CreativeModeTabEvents.modifyOutputEvent(modKey(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.accept(GENERATOR);
                entries.accept(ELECTRIC_FURNACE);
                entries.accept(MACERATOR);
                entries.accept(COMPRESSOR);
                entries.accept(EXTRACTOR);
                entries.accept(MATTER_GENERATOR);
                entries.accept(BATTERY_BOX);
                entries.accept(ENERGY_CRYSTAL_ARRAY);
                entries.accept(ADVANCED_ENERGY_STORAGE);
                entries.accept(ELECTRIC_BEACON);

                entries.accept(CABLE);
                entries.accept(POWER_CABLE);
                entries.accept(ADVANCED_ENERGY_CONDUIT);

                entries.accept(RUBBER_LOG);
                entries.accept(RESIN_YIELDING_RUBBER_LOG);
                entries.accept(RUBBER_WOOD);
                entries.accept(STRIPPED_RUBBER_LOG);
                entries.accept(STRIPPED_RUBBER_WOOD);
                entries.accept(RUBBER_LEAVES);
                entries.accept(RUBBER_SAPLING);
                entries.accept(RUBBER_PLANKS);
                entries.accept(RUBBER_STAIRS);
                entries.accept(RUBBER_SLAB);

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

                entries.accept(FERRU_SEEDS);

                entries.accept(SILICON_BRONZE_SWORD);
                entries.accept(SILICON_BRONZE_SHOVEL);
                entries.accept(SILICON_BRONZE_PICKAXE);
                entries.accept(SILICON_BRONZE_AXE);
                entries.accept(SILICON_BRONZE_HOE);
                entries.accept(FLAK_CHESTPLATE);
                entries.accept(WRENCH);

                entries.accept(CHAINSAW, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(CHAINSAW));
                entries.accept(DRILL, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(DRILL));
                entries.accept(DIAMOND_DRILL, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(DIAMOND_DRILL));
                entries.accept(ADVANCED_DRILL, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(ADVANCED_DRILL));

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

                entries.accept(BATTERY_PACK, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(BATTERY_PACK));
                entries.accept(ADVANCED_BATTERY_PACK, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(ADVANCED_BATTERY_PACK));

                entries.accept(BATTERY);
                entries.accept(EnergyComponents.withEnergy(BATTERY, 500), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(BATTERY, 2000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(BATTERY, 4000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(BATTERY, 6000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(BATTERY, 8000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(BATTERY));
                entries.accept(ENERGY_CRYSTAL);
                entries.accept(EnergyComponents.withEnergy(ENERGY_CRYSTAL, 5000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(ENERGY_CRYSTAL, 30000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withEnergy(ENERGY_CRYSTAL, 70000), CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                entries.accept(EnergyComponents.withFullEnergy(ENERGY_CRYSTAL));

                entries.accept(REFINED_IRON_INGOT);
                entries.accept(SILICON_BRONZE_INGOT);
                entries.accept(RAW_SILICON);

                entries.accept(WOOD_DUST);
                entries.accept(STONE_DUST);
                entries.accept(COAL_DUST);
                entries.accept(COPPER_DUST);
                entries.accept(IRON_DUST);
                entries.accept(GOLD_DUST);
                entries.accept(DIAMOND_DUST);
                entries.accept(ENDER_PEARL_DUST);
                entries.accept(SILICON_DUST);
                entries.accept(SILICON_BRONZE_DUST);

                entries.accept(STICKY_RESIN);
                entries.accept(RUBBER);
                entries.accept(COPPER_WIRE);
                entries.accept(GLASS_FIBER);
                entries.accept(CIRCUIT);
                entries.accept(ELECTRIC_MOTOR);
                entries.accept(DATA_CIRCUIT);
                entries.accept(ENERGY_FLOW_CIRCUIT);

                entries.accept(MIXED_METAL_INGOT);
                entries.accept(COMPOSITE_PLATE);
                entries.accept(CARBON_REDSTONE_MATRIX);
                entries.accept(CONDUCTIVE_CARBON);
                entries.accept(FORCICIUM_SUNNARIUM_COMPLEX);
                entries.accept(QUANTUM_PLATE);
                entries.accept(COMPRESSED_PLANTS);
                entries.accept(CANISTER);
                entries.accept(DENSE_REFINED_IRON_PLATE);
                entries.accept(REINFORCED_IRRADIANT_CORE);

                entries.accept(OVERCLOCK_UPGRADE);
                entries.accept(CAPACITOR_UPGRADE);
                entries.accept(BLASTING_UPGRADE);
                entries.accept(SMOKING_UPGRADE);
                entries.accept(INDUCTION_UPGRADE);
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
        public static final BlockEntityType<EnergyCrystalArrayBlockEntity> ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayBlockEntity::new, Blocks.ENERGY_CRYSTAL_ARRAY);
        public static final BlockEntityType<AdvancedEnergyStorageBlockEntity> ADVANCED_ENERGY_STORAGE = register("advanced_energy_storage", AdvancedEnergyStorageBlockEntity::new, Blocks.ADVANCED_ENERGY_STORAGE);
        public static final BlockEntityType<ElectricBeaconBlockEntity> ELECTRIC_BEACON = register("electric_beacon", ElectricBeaconBlockEntity::new, Blocks.ELECTRIC_BEACON);

        public static void initialize() {
        }

        public static <T extends BlockEntity> BlockEntityType<T> register(String name, FabricBlockEntityTypeBuilder.Factory<T> constructor, Block... blocks) {
            var key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, modId(name));
            var type = FabricBlockEntityTypeBuilder.create(constructor, blocks).build();

            return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key, type);
        }
    }

    public static class MenuTypes {
        public static final MenuType<GeneratorMenu> GENERATOR = register("generator", GeneratorMenu::new);
        public static final MenuType<MachineMenu> ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceMenu::new);
        public static final MenuType<MachineMenu> MACERATOR = register("macerator", MaceratorMenu::new);
        public static final MenuType<MachineMenu> COMPRESSOR = register("compressor", CompressorMenu::new);
        public static final MenuType<MachineMenu> EXTRACTOR = register("extractor", ExtractorMenu::new);
        public static final MenuType<MatterGeneratorMenu> MATTER_GENERATOR = register("matter_generator", MatterGeneratorMenu::new);
        public static final MenuType<EnergyStorageMenu> ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayMenu::new);
        public static final MenuType<EnergyStorageMenu> ADVANCED_ENERGY_STORAGE = register("advanced_energy_storage", AdvancedEnergyStorageMenu::new);
        public static final MenuType<ElectricBeaconMenu> ELECTRIC_BEACON = register("electric_beacon", ElectricBeaconMenu::new);
        public static final MenuType<BatteryBoxMenu> BATTERY_BOX = register("battery_box", BatteryBoxMenu::new);

        public static void initialize() {
        }

        public static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> constructor) {
            var key = ResourceKey.create(Registries.MENU, modId(name));
            var type = new MenuType<>(constructor, FeatureFlags.VANILLA_SET);

            return Registry.register(BuiltInRegistries.MENU, key, type);
        }

        public static void initializeClient() {
            MenuScreens.register(GENERATOR, GeneratorScreen::new);
            MenuScreens.register(ELECTRIC_FURNACE, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.DEFAULT));
            MenuScreens.register(MACERATOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.MACERATOR));
            MenuScreens.register(COMPRESSOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.COMPRESSOR));
            MenuScreens.register(EXTRACTOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.EXTRACTOR));
            MenuScreens.register(MATTER_GENERATOR, MatterGeneratorScreen::new);
            MenuScreens.register(BATTERY_BOX, BatteryBoxScreen::new);
            MenuScreens.register(ENERGY_CRYSTAL_ARRAY, EnergyStorageScreen::new);
            MenuScreens.register(ADVANCED_ENERGY_STORAGE, EnergyStorageScreen::new);
            MenuScreens.register(ELECTRIC_BEACON, ElectricBeaconScreen::new);
        }
    }

    public static class FoliagePlacerTypes {
        public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER = register("rubber_foliage_placer", RubberFoliagePlacer.CODEC);

        public static void initialize() {
        }

        public static <T extends FoliagePlacer> FoliagePlacerType<T> register(String name, MapCodec<T> codec) {
            var key = ResourceKey.create(Registries.FOLIAGE_PLACER_TYPE, modId(name));
            var type = new FoliagePlacerType<>(codec);

            return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, key, type);
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
        public static final RecipeSerializer<CompressingRecipe> COMPRESSING = register("compressing", ClassicMachineRecipe.createCodec(CompressingRecipe::new, CompressingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<ExtractingRecipe> EXTRACTING = register("extracting", ClassicMachineRecipe.createCodec(ExtractingRecipe::new, ExtractingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<MatterGeneratingRecipe> MATTER_GENERATING = register("matter_generating", new RecipeSerializer<>(MatterGeneratingRecipe.CODEC, MatterGeneratingRecipe.STREAM_CODEC));
        public static final RecipeSerializer<ElectricShapedRecipe> ELECTRIC_SHAPED_CRAFTING = register("electric_shaped_crafting", new RecipeSerializer<>(ElectricShapedRecipe.CODEC, ElectricShapedRecipe.STREAM_CODEC));

        public static void initialize() {
        }

        private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
            return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modId(name), serializer);
        }
    }

    public static class Particles {
        public static final SimpleParticleType ELECTRIC_BEACON = register("electric_beacon");

        public static void initialize() {
        }

        public static void initializeClient() {
            ParticleProviderRegistry.getInstance().register(ELECTRIC_BEACON, ElectricBeaconParticle.Provider::new);
        }

        private static SimpleParticleType register(String name) {
            var type = FabricParticleTypes.simple();
            return Registry.register(BuiltInRegistries.PARTICLE_TYPE, modId(name), type);
        }
    }

    public static class CustomPayloads {
        public static final CustomPacketPayload.Type<OverloadCablePayload> OVERLOAD_CABLE = new CustomPacketPayload.Type<>(modId("overload_cable"));

        public static void initialize() {
            PayloadTypeRegistry.clientboundPlay().register(OVERLOAD_CABLE, OverloadCablePayload.STREAM_CODEC);
        }

        @Environment(EnvType.CLIENT)
        public static void initializeClient() {
            ClientPlayNetworking.registerGlobalReceiver(OVERLOAD_CABLE, OverloadCablePayload::receive);
        }
    }
}