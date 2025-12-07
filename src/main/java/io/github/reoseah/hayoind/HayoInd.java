package io.github.reoseah.hayoind;

import io.github.reoseah.hayoind.block.*;
import io.github.reoseah.hayoind.feature.RubberFoliagePlacer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static net.minecraft.world.level.block.Blocks.leavesProperties;
import static net.minecraft.world.level.block.Blocks.logProperties;

public class HayoInd {
    public static final String MOD_ID = "hayoind";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final CreativeModeTab TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.hayoind")).icon(() -> new ItemStack(Blocks.MACERATOR)).build();

    public static final ResourceKey<PlacedFeature> RUBBER_TREE_PATCH = key(Registries.PLACED_FEATURE, "rubber_tree_patch");

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modLocation("main"), TAB);

        Blocks.initialize();
        Items.initialize();
        FoliagePlacerTypes.initialize();

        BiomeModifications.create(ResourceLocation.fromNamespaceAndPath("hayoind", "features"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(BiomeTags.IS_FOREST)
                        .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA))
                        .or(BiomeSelectors.includeByKey(Biomes.SWAMP)), (selectionCtx, modificationCtx) -> {
                    modificationCtx.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, RUBBER_TREE_PATCH);
                });
    }

    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, Blocks.REINFORCED_GLASS, Blocks.REINFORCED_DOOR, Blocks.RUBBER_LEAVES, Blocks.RUBBER_SAPLING, Blocks.FERRU);

        ColorProviderRegistry.BLOCK.register((blockState, blockAndTintGetter, blockPos, i) -> blockAndTintGetter != null ? BiomeColors.getAverageFoliageColor(blockAndTintGetter, blockPos) : -12012264, Blocks.RUBBER_LEAVES);
    }

    public static ResourceLocation modLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, modLocation(location));
    }

    public static class Blocks {
        private static final BlockBehaviour.Properties MACHINES = BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL).mapColor(MapColor.METAL);
        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, MACHINES);
        public static final Block ADVANCED_MACHINE_BLOCK = register("advanced_machine_block", Block::new, MACHINES);
        public static final Block ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlock::new, MACHINES);
        public static final Block MACERATOR = register("macerator", MaceratorBlock::new, MACHINES);
        public static final Block EXTRACTOR = register("extractor", ExtractorBlock::new, MACHINES);
        public static final Block AUTOMATED_FERTILIZER = register("automated_fertilizer", AutomatedFertilizerBlock::new, MACHINES);

        public static final Block CHIPBOARD = register("chipboard", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.WOOD));

        private static final BlockBehaviour.Properties REINFORCED_BLOCKS = BlockBehaviour.Properties.of().strength(3F).sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE);
        public static final Block REINFORCED_STONE = register("reinforced_stone", Block::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_GLASS = register("reinforced_glass", TransparentBlock::new, BlockBehaviour.Properties.of().strength(3F).noOcclusion().sound(SoundType.GLASS));
        public static final Block REINFORCED_STONE_STAIRS = register("reinforced_stone_stairs", props -> new StairBlock(REINFORCED_STONE.defaultBlockState(), props), REINFORCED_BLOCKS);
        public static final Block REINFORCED_STONE_SLAB = register("reinforced_stone_slab", SlabBlock::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_DOOR = register("reinforced_door", props -> new DoorBlock(BlockSetType.IRON, props), BlockBehaviour.Properties.of().strength(3F).noOcclusion().sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE));

        public static final Block RUBBER_LOG = register("rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD));
        public static final Block RUBBER_WOOD = register("rubber_wood", RotatedPillarBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
        public static final Block STRIPPED_RUBBER_LOG = register("stripped_rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_WOOD = register("stripped_rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block RUBBER_LEAVES = register(
                "rubber_leaves", properties -> new TintedParticleLeavesBlock(0.01F, properties), leavesProperties(SoundType.GRASS)
        );

        public static final Block RUBBER_PLANKS = register("rubber_planks", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava());
        public static final TreeGrower RUBBER_TREE = new TreeGrower(
                "hayoind:rubber_tree",
                0.5F,
                Optional.empty(),
                Optional.empty(),
                Optional.of(key(Registries.CONFIGURED_FEATURE, "rubber_tree")),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()
        );
        public static final Block RUBBER_SAPLING = register(
                "rubber_sapling",
                properties -> new SaplingBlock(RUBBER_TREE, properties),
                BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY)
        );

        public static final Block FERRU = register("ferru", OreCropBlock.FerruBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));

        public static void initialize() {
        }

        private static Block register(String name, Function<BlockBehaviour.Properties, Block> constructor, BlockBehaviour.Properties properties) {
            var key = ResourceKey.create(Registries.BLOCK, modLocation(name));

            return Registry.register(BuiltInRegistries.BLOCK, key, constructor.apply(properties.setId(key)));
        }
    }

    public static class Items {
        public static final Item MACHINE_BLOCK = registerBlock(Blocks.MACHINE_BLOCK);
        public static final Item ADVANCED_MACHINE_BLOCK = registerBlock(Blocks.ADVANCED_MACHINE_BLOCK);
        public static final Item CHIPBOARD = registerBlock(Blocks.CHIPBOARD);
        public static final Item REINFORCED_STONE = registerBlock(Blocks.REINFORCED_STONE);
        public static final Item REINFORCED_GLASS = registerBlock(Blocks.REINFORCED_GLASS);
        public static final Item REINFORCED_STONE_STAIRS = registerBlock(Blocks.REINFORCED_STONE_STAIRS);
        public static final Item REINFORCED_STONE_SLAB = registerBlock(Blocks.REINFORCED_STONE_SLAB);
        public static final Item REINFORCED_DOOR = registerBlock(Blocks.REINFORCED_DOOR);

        public static final Item RUBBER_LOG = registerBlock(Blocks.RUBBER_LOG);
        public static final Item RUBBER_WOOD = registerBlock(Blocks.RUBBER_WOOD);
        public static final Item STRIPPED_RUBBER_LOG = registerBlock(Blocks.STRIPPED_RUBBER_LOG);
        public static final Item STRIPPED_RUBBER_WOOD = registerBlock(Blocks.STRIPPED_RUBBER_WOOD);
        public static final Item RUBBER_LEAVES = registerBlock(Blocks.RUBBER_LEAVES);
        public static final Item RUBBER_PLANKS = registerBlock(Blocks.RUBBER_PLANKS);
        public static final Item RUBBER_SAPLING = registerBlock(Blocks.RUBBER_SAPLING);

        public static final Item ELECTRIC_FURNACE = registerBlock(Blocks.ELECTRIC_FURNACE);
        public static final Item MACERATOR = registerBlock(Blocks.MACERATOR);
        public static final Item EXTRACTOR = registerBlock(Blocks.EXTRACTOR);
        public static final Item AUTOMATED_FERTILIZER = registerBlock(Blocks.AUTOMATED_FERTILIZER);

        public static final Item WOOD_DUST = registerItem("wood_dust");
        public static final Item STONE_DUST = registerItem("stone_dust");
        public static final Item COAL_DUST = registerItem("coal_dust");
        public static final Item COPPER_DUST = registerItem("copper_dust");
        public static final Item IRON_DUST = registerItem("iron_dust");
        public static final Item GOLD_DUST = registerItem("gold_dust");
        public static final Item DIAMOND_DUST = registerItem("diamond_dust");
        public static final Item QUARTZ_DUST = registerItem("quartz_dust");
        public static final Item NETHERITE_SCRAP_DUST = registerItem("netherite_scrap_dust");
        public static final Item ENDER_PEARL_DUST = registerItem("ender_pearl_dust");
        public static final Item SILICON_DUST = registerItem("silicon_dust");
        public static final Item SILICON_BRONZE_DUST = registerItem("silicon_bronze_dust");

        public static final Item SILICON_BRONZE_INGOT = registerItem("silicon_bronze_ingot");

        public static final Item QUARTZ_COAL_MIXTURE = registerItem("quartz_coal_mixture");
        public static final Item RAW_SILICON = registerItem("raw_silicon");
        public static final Item COMPOSITE_PLATE = registerItem("composite_plate");

        public static final Item SILICON_BRONZE_SWORD = registerItem("silicon_bronze_sword");
        public static final Item SILICON_BRONZE_AXE = registerItem("silicon_bronze_axe");
        public static final Item SILICON_BRONZE_PICKAXE = registerItem("silicon_bronze_pickaxe");
        public static final Item SILICON_BRONZE_SHOVEL = registerItem("silicon_bronze_shovel");
        public static final Item SILICON_BRONZE_HOE = registerItem("silicon_bronze_hoe");

        public static void initialize() {
            ItemGroupEvents.modifyEntriesEvent(key(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.accept(MACHINE_BLOCK);
                entries.accept(ADVANCED_MACHINE_BLOCK);

                entries.accept(ELECTRIC_FURNACE);
                entries.accept(MACERATOR);
                entries.accept(EXTRACTOR);
                entries.accept(AUTOMATED_FERTILIZER);

                entries.accept(RUBBER_LOG);
                entries.accept(RUBBER_WOOD);
                entries.accept(STRIPPED_RUBBER_LOG);
                entries.accept(STRIPPED_RUBBER_WOOD);
                entries.accept(RUBBER_LEAVES);
                entries.accept(RUBBER_PLANKS);
                entries.accept(RUBBER_SAPLING);

                entries.accept(CHIPBOARD);

                entries.accept(REINFORCED_STONE);
                entries.accept(REINFORCED_GLASS);
                entries.accept(REINFORCED_STONE_STAIRS);
                entries.accept(REINFORCED_STONE_SLAB);
                entries.accept(REINFORCED_DOOR);

                entries.accept(WOOD_DUST);
                entries.accept(STONE_DUST);
                entries.accept(COAL_DUST);
                entries.accept(COPPER_DUST);
                entries.accept(IRON_DUST);
                entries.accept(GOLD_DUST);
                entries.accept(DIAMOND_DUST);
                entries.accept(QUARTZ_DUST);
                entries.accept(NETHERITE_SCRAP_DUST);
                entries.accept(ENDER_PEARL_DUST);
                entries.accept(SILICON_DUST);
                entries.accept(SILICON_BRONZE_DUST);

                entries.accept(SILICON_BRONZE_INGOT);

                entries.accept(QUARTZ_COAL_MIXTURE);
                entries.accept(RAW_SILICON);
                entries.accept(COMPOSITE_PLATE);

                entries.accept(SILICON_BRONZE_SWORD);
                entries.accept(SILICON_BRONZE_AXE);
                entries.accept(SILICON_BRONZE_PICKAXE);
                entries.accept(SILICON_BRONZE_SHOVEL);
                entries.accept(SILICON_BRONZE_HOE);
            });
        }

        public static Item registerBlock(Block block) {
            return registerBlock(block, BlockItem::new);
        }

        public static Item registerBlock(Block block, BiFunction<Block, Item.Properties, Item> constructor) {
            return registerBlock(block, constructor, new Item.Properties());
        }

        @SuppressWarnings("deprecation")
        public static Item registerBlock(Block block, BiFunction<Block, Item.Properties, Item> constructor, Item.Properties properties) {
            var key = ResourceKey.create(Registries.ITEM, block.builtInRegistryHolder().key().location());
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
            var key = ResourceKey.create(Registries.ITEM, modLocation(name));
            return Registry.register(BuiltInRegistries.ITEM, key, constructor.apply(properties.setId(key)));
        }
    }

    public static class FoliagePlacerTypes {
        public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER = register("rubber_foliage_placer", new FoliagePlacerType<>(RubberFoliagePlacer.CODEC));

        public static void initialize() {
        }

        public static <T extends FoliagePlacer> FoliagePlacerType<T> register(String name, FoliagePlacerType<T> entry) {
            var key = ResourceKey.create(Registries.FOLIAGE_PLACER_TYPE, modLocation(name));
            return Registry.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, key, entry);
        }
    }
}