package io.github.reoseah.hayo;

import com.mojang.serialization.MapCodec;
import io.github.reoseah.hayo.base.client.HayoGuiSprites;
import io.github.reoseah.hayo.feature.automated_fertilizer.AutomatedFertilizerBlock;
import io.github.reoseah.hayo.feature.cable.CableBlock;
import io.github.reoseah.hayo.feature.cable.CableItem;
import io.github.reoseah.hayo.feature.energy.ElectricShapedRecipe;
import io.github.reoseah.hayo.feature.energy.blocks.ElectricBlockManager;
import io.github.reoseah.hayo.feature.energy.blocks.OverloadCablePayload;
import io.github.reoseah.hayo.feature.energy.items.*;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageMenu;
import io.github.reoseah.hayo.feature.energy_storages.EnergyStorageScreen;
import io.github.reoseah.hayo.feature.energy_storages.battery_array.BatteryArrayBlock;
import io.github.reoseah.hayo.feature.energy_storages.battery_array.BatteryArrayBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.battery_array.BatteryArrayMenu;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayBlock;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayBlockEntity;
import io.github.reoseah.hayo.feature.energy_storages.crystal_array.EnergyCrystalArrayMenu;
import io.github.reoseah.hayo.feature.generator.GeneratorBlock;
import io.github.reoseah.hayo.feature.generator.GeneratorBlockEntity;
import io.github.reoseah.hayo.feature.generator.GeneratorMenu;
import io.github.reoseah.hayo.feature.generator.GeneratorScreen;
import io.github.reoseah.hayo.feature.ore_crops.FerruBlock;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineRecipe;
import io.github.reoseah.hayo.feature.processing_machines.ClassicMachineScreen;
import io.github.reoseah.hayo.feature.processing_machines.MachineMenu;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressorBlock;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressorBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.compressor.CompressorMenu;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceBlock;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.electric_furnace.ElectricFurnaceMenu;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractorBlock;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractorBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.extractor.ExtractorMenu;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratingRecipe;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratorBlock;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratorBlockEntity;
import io.github.reoseah.hayo.feature.processing_machines.macerator.MaceratorMenu;
import io.github.reoseah.hayo.feature.processing_machines.matter_generator.*;
import io.github.reoseah.hayo.feature.rubber_tree.ResinYieldingLogBlock;
import io.github.reoseah.hayo.feature.rubber_tree.RubberFoliagePlacer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ChunkSectionLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
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

public class Hayo {
    public static final String MOD_ID = "hayo";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final CreativeModeTab TAB = FabricCreativeModeTab.builder().title(Component.translatable("itemGroup.hayo")).icon(() -> new ItemStack(Blocks.ELECTRIC_FURNACE)).build();

    public static final SavedDataType<ElectricBlockManager> ELECTRIC_DATA = new SavedDataType<>("HayoElectricData", //
            ElectricBlockManager::new, //
            MapCodec.unitCodec(ElectricBlockManager::new), //
            null);

    public static final AttachmentType<ElectricBlockManager.ChunkData> CHUNK_ELECTRIC_DATA = AttachmentRegistry.create( //
            modId("electric_blocks"), //
            builder -> builder //
                    .initializer(ElectricBlockManager.ChunkData::new) //
                    .persistent(ElectricBlockManager.ChunkData.CODEC.codec()));

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modId("main"), TAB);
        Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, modId("energy"), SimpleBatteryItem.ENERGY);

        Blocks.initialize();
        Items.initialize();
        BlockEntityTypes.initialize();
        MenuTypes.initialize();
        FoliagePlacerTypes.initialize();
        RecipeTypes.initialize();
        RecipeSerializers.initialize();

        CustomPayloads.initialize();
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MACERATING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.COMPRESSING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.EXTRACTING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.MATTER_GENERATING);
        RecipeSynchronization.synchronizeRecipeSerializer(RecipeSerializers.ELECTRIC_SHAPED_CRAFTING);

        BiomeModifications.create(Identifier.fromNamespaceAndPath("hayo", "features")) //
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(BiomeTags.IS_FOREST) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_TAIGA)) //
                                .or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE)) //
                                .or(BiomeSelectors.includeByKey(Biomes.SWAMP)), //
                        (selectionCtx, modificationCtx) -> {
                            modificationCtx.getGenerationSettings().addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, modKey(Registries.PLACED_FEATURE, "rubber_tree_patch"));
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
        ChunkSectionLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, Blocks.REINFORCED_GLASS, Blocks.REINFORCED_DOOR, Blocks.CHIPBOARD_DOOR, Blocks.RUBBER_LEAVES, Blocks.RUBBER_SAPLING, Blocks.FERRU);
        ColorProviderRegistry.BLOCK.register((state, level, pos, seed) -> level != null ? BiomeColors.getAverageFoliageColor(level, pos) : -12012264, Blocks.RUBBER_LEAVES);

        RangeSelectItemModelProperties.ID_MAPPER.put(modId("energy"), EnergyModelProperty.MAP_CODEC);

        MenuScreens.register(MenuTypes.GENERATOR, GeneratorScreen::new);
        MenuScreens.register(MenuTypes.ELECTRIC_FURNACE, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.DEFAULT));
        MenuScreens.register(MenuTypes.MACERATOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.MACERATOR));
        MenuScreens.register(MenuTypes.COMPRESSOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.COMPRESSOR));
        MenuScreens.register(MenuTypes.EXTRACTOR, ClassicMachineScreen.withArrow(HayoGuiSprites.RecipeArrow.EXTRACTOR));
        MenuScreens.register(MenuTypes.MATTER_GENERATOR, MatterGeneratorScreen::new);
        MenuScreens.register(MenuTypes.BATTERY_ARRAY, EnergyStorageScreen::new);
        MenuScreens.register(MenuTypes.ENERGY_CRYSTAL_ARRAY, EnergyStorageScreen::new);

        CustomPayloads.initializeClient();
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
        public static final Block AUTOMATED_FERTILIZER = register("automated_fertilizer", AutomatedFertilizerBlock::new, MACHINES);
        public static final Block MATTER_GENERATOR = register("matter_generator", MatterGeneratorBlock::new, MACHINES);
        public static final Block BATTERY_ARRAY = register("battery_array", BatteryArrayBlock::new, MACHINES);
        public static final Block ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayBlock::new, MACHINES);

        public static final CableBlock CABLE = register("cable", properties -> new CableBlock(32, 2, properties), BlockBehaviour.Properties.of().strength(.5F, 3).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final CableBlock POWER_CABLE = register("power_cable", properties -> new CableBlock(128, 3, properties), BlockBehaviour.Properties.of().strength(.75F, 6).sound(SoundType.WOOL).pushReaction(PushReaction.DESTROY));
        public static final Block RUBBER_LOG = register("rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.PODZOL, SoundType.WOOD));
        public static final Block RESIN_YIELDING_RUBBER_LOG = register("resin_yielding_rubber_log", ResinYieldingLogBlock::new, BlockBehaviour.Properties.of().randomTicks().instrument(NoteBlockInstrument.BASS).strength(2.0F).sound(SoundType.WOOD).ignitedByLava());
        public static final Block RUBBER_WOOD = register("rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_LOG = register("stripped_rubber_log", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block STRIPPED_RUBBER_WOOD = register("stripped_rubber_wood", RotatedPillarBlock::new, logProperties(MapColor.WOOD, MapColor.WOOD, SoundType.WOOD));
        public static final Block RUBBER_LEAVES = register("rubber_leaves", properties -> new TintedParticleLeavesBlock(0.01F, properties), leavesProperties(SoundType.GRASS));
        public static final TreeGrower RUBBER_TREE = new TreeGrower("hayo:rubber_tree", 0F, Optional.empty(), Optional.empty(), Optional.of(modKey(Registries.CONFIGURED_FEATURE, "rubber_tree")), Optional.empty(), Optional.empty(), Optional.empty());
        public static final Block RUBBER_SAPLING = register("rubber_sapling", properties -> new SaplingBlock(RUBBER_TREE, properties), BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.GRASS).pushReaction(PushReaction.DESTROY));

        public static final BlockBehaviour.Properties RUBBER_PROPERTIES = BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.0F, 3.0F).sound(SoundType.WOOD).ignitedByLava();
        public static final Block RUBBER_PLANKS = register("rubber_planks", Block::new, RUBBER_PROPERTIES);
        public static final Block RUBBER_STAIRS = register("rubber_stairs", props -> new StairBlock(RUBBER_PLANKS.defaultBlockState(), props), RUBBER_PROPERTIES);
        public static final Block RUBBER_SLAB = register("rubber_slab", SlabBlock::new, RUBBER_PROPERTIES);

        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, MACHINES);
        public static final Block ADVANCED_MACHINE_BLOCK = register("advanced_machine_block", Block::new, MACHINES);

        public static final Block CHIPBOARD = register("chipboard", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.WOOD).mapColor(MapColor.WOOD));
        public static final Block CHIPBOARD_DOOR = register("chipboard_door", props -> new DoorBlock(BlockSetType.OAK, props), BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(3.0F).noOcclusion().ignitedByLava().pushReaction(PushReaction.DESTROY));

        public static final Block RUBBER_BLOCK = register("rubber_block", Block::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(3));

        private static final BlockBehaviour.Properties REINFORCED_BLOCKS = BlockBehaviour.Properties.of().strength(3F, 30F).sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE);
        public static final Block REINFORCED_STONE = register("reinforced_stone", Block::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_GLASS = register("reinforced_glass", TransparentBlock::new, BlockBehaviour.Properties.of().strength(3F, 15F).noOcclusion().sound(SoundType.GLASS));
        public static final Block REINFORCED_STONE_STAIRS = register("reinforced_stone_stairs", props -> new StairBlock(REINFORCED_STONE.defaultBlockState(), props), REINFORCED_BLOCKS);
        public static final Block REINFORCED_STONE_SLAB = register("reinforced_stone_slab", SlabBlock::new, REINFORCED_BLOCKS);
        public static final Block REINFORCED_DOOR = register("reinforced_door", props -> new DoorBlock(BlockSetType.IRON, props), BlockBehaviour.Properties.of().strength(3F, 15F).noOcclusion().sound(SoundType.STONE).mapColor(MapColor.DEEPSLATE));

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

    public static class Items {
        public static final Item GENERATOR = registerBlock(Blocks.GENERATOR);
        public static final Item ELECTRIC_FURNACE = registerBlock(Blocks.ELECTRIC_FURNACE);
        public static final Item MACERATOR = registerBlock(Blocks.MACERATOR);
        public static final Item COMPRESSOR = registerBlock(Blocks.COMPRESSOR);
        public static final Item EXTRACTOR = registerBlock(Blocks.EXTRACTOR);
        public static final Item AUTOMATED_FERTILIZER = registerBlock(Blocks.AUTOMATED_FERTILIZER);
        public static final Item MATTER_GENERATOR = registerBlock(Blocks.MATTER_GENERATOR, new Item.Properties().rarity(Rarity.EPIC));
        public static final Item BATTERY_ARRAY = registerBlock(Blocks.BATTERY_ARRAY);
        public static final Item ENERGY_CRYSTAL_ARRAY = registerBlock(Blocks.ENERGY_CRYSTAL_ARRAY, new Item.Properties().rarity(Rarity.RARE));

        public static final Item CABLE = registerBlock(Blocks.CABLE, CableItem::new);
        public static final Item POWER_CABLE = registerBlock(Blocks.POWER_CABLE, CableItem::new);

        public static final Item RUBBER_LOG = registerBlock(Blocks.RUBBER_LOG);
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

        public static final Item CHIPBOARD = registerBlock(Blocks.CHIPBOARD);
        public static final Item CHIPBOARD_DOOR = registerBlock(Blocks.CHIPBOARD_DOOR);

        public static final Item RUBBER_BLOCK = registerBlock(Blocks.RUBBER_BLOCK);

        public static final Item REINFORCED_STONE = registerBlock(Blocks.REINFORCED_STONE);
        public static final Item REINFORCED_GLASS = registerBlock(Blocks.REINFORCED_GLASS);
        public static final Item REINFORCED_STONE_STAIRS = registerBlock(Blocks.REINFORCED_STONE_STAIRS);
        public static final Item REINFORCED_STONE_SLAB = registerBlock(Blocks.REINFORCED_STONE_SLAB);
        public static final Item REINFORCED_DOOR = registerBlock(Blocks.REINFORCED_DOOR);

        public static final Item FERRU_SEEDS = registerItem("ferru_seeds", props -> new BlockItem(Blocks.FERRU, props), new Item.Properties().useItemDescriptionPrefix());

        public static final Item WRENCH = registerItem("wrench");

        public static ItemAttributeModifiers createChargedAttributes(float attackDamage, float attackSpeed) {
            var builder = ItemAttributeModifiers.builder();
            builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
            return builder.build();
        }

        private static final HolderGetter<Block> BLOCK_LOOKUP = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.BLOCK);
        public static final TagKey<Block> CHAINSAW_MINEABLE = TagKey.create(Registries.BLOCK, modId("mineable/chainsaw"));

        public static final Item CHAINSAW = registerItem("chainsaw", props -> new SimpleElectricToolItem(props, createChargedAttributes(12, -3), 50, 10000, 32), new Item.Properties() //
                .component(DataComponents.TOOL, new Tool( //
                        List.of( //
                                Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)), //
                                Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(CHAINSAW_MINEABLE), 9F) //
                        ), 0.5F, 0, false) //
                ) //
                .equippable(EquipmentSlot.MAINHAND) //
                .stacksTo(1));

        public static final TagKey<Block> DRILL_MINEABLE = TagKey.create(Registries.BLOCK, modId("mineable/drill"));

        public static final Item DRILL = registerItem("drill", props -> new SimpleElectricToolItem(props, createChargedAttributes(6, -3), 50, 10000, 32), new Item.Properties() //
                .component(DataComponents.TOOL, new Tool( //
                        List.of( //
                                Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(BlockTags.INCORRECT_FOR_IRON_TOOL)), //
                                Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(DRILL_MINEABLE), 7F) //
                        ), 0.5F, 0, true) //
                ) //
                .equippable(EquipmentSlot.MAINHAND) //
                .stacksTo(1));
        public static final Item DIAMOND_DRILL = registerItem("diamond_drill", props -> new SimpleElectricToolItem(props, createChargedAttributes(8, -3), 80, 10000, 32), new Item.Properties() //
                .component(DataComponents.TOOL, new Tool( //
                        List.of( //
                                Tool.Rule.deniesDrops(BLOCK_LOOKUP.getOrThrow(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)), //
                                Tool.Rule.minesAndDrops(BLOCK_LOOKUP.getOrThrow(DRILL_MINEABLE), 9F) //
                        ), 0.5F, 0, true) //
                ) //
                .equippable(EquipmentSlot.MAINHAND) //
                .rarity(Rarity.RARE) //
                .stacksTo(1));

        private static final TagKey<Item> SILICON_BRONZE_MATERIALS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/silicon_bronze"));
        private static final ToolMaterial SILICON_BRONZE = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, ToolMaterial.DIAMOND.durability(), 7, 2.0F, 10, SILICON_BRONZE_MATERIALS);

        public static final Item SILICON_BRONZE_SWORD = registerItem("silicon_bronze_sword", new Item.Properties().sword(SILICON_BRONZE, 3.0F, -2.4F));
        public static final Item SILICON_BRONZE_SHOVEL = registerItem("silicon_bronze_shovel", properties -> new ShovelItem(SILICON_BRONZE, 1.5F, -3.0F, properties));
        public static final Item SILICON_BRONZE_PICKAXE = registerItem("silicon_bronze_pickaxe", new Item.Properties().pickaxe(SILICON_BRONZE, 1.0F, -2.8F));
        public static final Item SILICON_BRONZE_AXE = registerItem("silicon_bronze_axe", properties -> new AxeItem(SILICON_BRONZE, 6.0F, -3.1F, properties));
        public static final Item SILICON_BRONZE_HOE = registerItem("silicon_bronze_hoe", properties -> new HoeItem(SILICON_BRONZE, -2.0F, -1.0F, properties));

        public static final TagKey<Item> FLAK_MATERIALS = TagKey.create(Registries.ITEM, modId("flak_materials"));
        private static final ArmorMaterial FLAK = new ArmorMaterial( //
                33, ArmorMaterials.makeDefense(3, 6, 8, 3, 11), 10, SoundEvents.ARMOR_EQUIP_DIAMOND, 2.0F, 0.0F, FLAK_MATERIALS, modKey(EquipmentAssets.ROOT_ID, "flak") //
        );
        public static final Item FLAK_CHESTPLATE = registerItem("flak_chestplate", Item::new, new Item.Properties().humanoidArmor(FLAK, ArmorType.CHESTPLATE).stacksTo(1));

        public static ItemAttributeModifiers createUnchargedAttributes(ArmorType type) {
            var builder = ItemAttributeModifiers.builder();
            var slot = EquipmentSlotGroup.bySlot(type.getSlot());
            var modifierId = Identifier.withDefaultNamespace("armor." + type.getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(modifierId, 0, AttributeModifier.Operation.ADD_VALUE), slot);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, 0, AttributeModifier.Operation.ADD_VALUE), slot);
            return builder.build();
        }

        public static ItemAttributeModifiers createChargedAttributes(ArmorType type, int armor, int toughness) {
            var builder = ItemAttributeModifiers.builder();
            var slot = EquipmentSlotGroup.bySlot(type.getSlot());
            var modifierId = Identifier.withDefaultNamespace("armor." + type.getName());
            builder.add(Attributes.ARMOR, new AttributeModifier(modifierId, armor, AttributeModifier.Operation.ADD_VALUE), slot);
            builder.add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(modifierId, toughness, AttributeModifier.Operation.ADD_VALUE), slot);
            return builder.build();
        }

        public static final Item NANO_HELMET = registerItem("nano_helmet", //
                props -> new SimpleElectricArmorItem(props, createChargedAttributes(ArmorType.HELMET, 3, 3), 100, 100_000, 128), //
                new Item.Properties() //
                        .attributes(createUnchargedAttributes(ArmorType.HELMET)) //
                        .component(DataComponents.EQUIPPABLE, //
                                Equippable.builder(ArmorType.HELMET.getSlot()) //
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_DIAMOND) //
                                        .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                                        .build()) //
                        .rarity(Rarity.RARE) //
                        .stacksTo(1));
        public static final Item NANO_CHESTPLATE = registerItem("nano_chestplate", //
                props -> new SimpleElectricArmorItem(props, createChargedAttributes(ArmorType.CHESTPLATE, 8, 3), 100, 100_000, 128), //
                new Item.Properties() //
                        .attributes(createUnchargedAttributes(ArmorType.CHESTPLATE)) //
                        .component(DataComponents.EQUIPPABLE, //
                                Equippable.builder(ArmorType.CHESTPLATE.getSlot()) //
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_DIAMOND) //
                                        .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                                        .build())//
                        .rarity(Rarity.RARE) //
                        .stacksTo(1));
        public static final Item NANO_LEGGINGS = registerItem("nano_leggings", //
                props -> new SimpleElectricArmorItem(props, createChargedAttributes(ArmorType.LEGGINGS, 6, 3), 100, 100_000, 128), //
                new Item.Properties() //
                        .attributes(createUnchargedAttributes(ArmorType.LEGGINGS)) //
                        .component(DataComponents.EQUIPPABLE, //
                                Equippable.builder(ArmorType.LEGGINGS.getSlot()) //
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_DIAMOND) //
                                        .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                                        .build()) //
                        .rarity(Rarity.RARE) //
                        .stacksTo(1));
        public static final Item NANO_BOOTS = registerItem("nano_boots", //
                props -> new SimpleElectricArmorItem(props, createChargedAttributes(ArmorType.BOOTS, 3, 3), 100, 100_000, 128), //
                new Item.Properties() //
                        .attributes(createUnchargedAttributes(ArmorType.BOOTS)) //
                        .component(DataComponents.EQUIPPABLE, //
                                Equippable.builder(ArmorType.BOOTS.getSlot()) //
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_DIAMOND) //
                                        .setAsset(modKey(EquipmentAssets.ROOT_ID, "nano")) //
                                        .build()) //
                        .rarity(Rarity.RARE) //
                        .stacksTo(1));

        public static final Item BATTERY_PACK = registerItem("battery_pack", //
                props -> new SimpleBatteryItem(props, 60_000, 32), //
                new Item.Properties() //
                        .attributes(createUnchargedAttributes(ArmorType.CHESTPLATE)) //
                        .component(DataComponents.EQUIPPABLE, //
                                Equippable.builder(ArmorType.CHESTPLATE.getSlot()) //
                                        .setEquipSound(SoundEvents.ARMOR_EQUIP_DIAMOND) //
                                        .setAsset(modKey(EquipmentAssets.ROOT_ID, "battery_pack")) //
                                        .build()) //
                        .stacksTo(1));

        public static final Item CANISTER = registerItem("canister");

        public static final Item BATTERY = registerItem("battery", properties -> new SimpleBatteryItem(properties, 10000, 10));
        public static final Item ENERGY_CRYSTAL = registerItem("energy_crystal", properties -> new SimpleBatteryItem(properties, 100000, 100), new Item.Properties().rarity(Rarity.RARE));

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
        public static final Item QUARTZ_DUST = registerItem("quartz_dust");
        public static final Item NETHERITE_SCRAP_DUST = registerItem("netherite_scrap_dust");
        public static final Item ENDER_PEARL_DUST = registerItem("ender_pearl_dust");
        public static final Item SILICON_DUST = registerItem("silicon_dust");
        public static final Item SILICON_BRONZE_DUST = registerItem("silicon_bronze_dust");

        public static final Item STICKY_RESIN = registerItem("sticky_resin");
        public static final Item RUBBER = registerItem("rubber");
        public static final Item REFINED_IRON_FOIL = registerItem("refined_iron_foil");
        public static final Item COPPER_WIRE = registerItem("copper_wire");
        public static final Item CIRCUIT = registerItem("circuit");
        public static final Item ELECTRIC_MOTOR = registerItem("electric_motor");
        public static final Item TRANSFORMER = registerItem("transformer");
        public static final Item REDSTONE_FLUX_LASER = registerItem("redstone_flux_laser", new Item.Properties().rarity(Rarity.RARE));
        public static final Item MIXED_METAL_INGOT = registerItem("mixed_metal_ingot");
        public static final Item COMPOSITE_PLATE = registerItem("composite_plate", new Item.Properties().rarity(Rarity.RARE));
        public static final Item CARBON_REDSTONE_MATRIX = registerItem("carbon_redstone_matrix");
        public static final Item CONDUCTIVE_CARBON = registerItem("conductive_carbon", new Item.Properties().rarity(Rarity.RARE));
        public static final Item REINFORCED_SUPERMETAL = registerItem("reinforced_supermetal", new Item.Properties().rarity(Rarity.RARE));

        public static final Item OVERCLOCK_UPGRADE = registerItem("overclock_upgrade", new Item.Properties().rarity(Rarity.RARE).stacksTo(16));
        public static final Item CAPACITOR_UPGRADE = registerItem("capacitor_upgrade", new Item.Properties().rarity(Rarity.RARE).stacksTo(16));
        public static final Item BLASTING_UPGRADE = registerItem("blasting_upgrade", new Item.Properties().rarity(Rarity.RARE).stacksTo(16));
        public static final Item SMOKING_UPGRADE = registerItem("smoking_upgrade", new Item.Properties().rarity(Rarity.RARE).stacksTo(16));

        public static void initialize() {
            CreativeModeTabEvents.modifyOutputEvent(modKey(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.accept(GENERATOR);
                entries.accept(ELECTRIC_FURNACE);
                entries.accept(MACERATOR);
                entries.accept(COMPRESSOR);
                entries.accept(EXTRACTOR);
//                entries.accept(AUTOMATED_FERTILIZER);
                entries.accept(MATTER_GENERATOR);
                entries.accept(BATTERY_ARRAY);
                entries.accept(ENERGY_CRYSTAL_ARRAY);

                entries.accept(CABLE);
                entries.accept(POWER_CABLE);

                entries.accept(RUBBER_LOG);
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
                entries.accept(CHIPBOARD);
                entries.accept(CHIPBOARD_DOOR);
                entries.accept(RUBBER_BLOCK);
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

                entries.accept(CHAINSAW);
                entries.accept(ElectricItems.withFullCharge(CHAINSAW));
                entries.accept(DRILL);
                entries.accept(ElectricItems.withFullCharge(DRILL));
                entries.accept(DIAMOND_DRILL);
                entries.accept(ElectricItems.withFullCharge(DIAMOND_DRILL));

                entries.accept(NANO_HELMET);
                entries.accept(ElectricItems.withFullCharge(NANO_HELMET));
                entries.accept(NANO_CHESTPLATE);
                entries.accept(ElectricItems.withFullCharge(NANO_CHESTPLATE));
                entries.accept(NANO_LEGGINGS);
                entries.accept(ElectricItems.withFullCharge(NANO_LEGGINGS));
                entries.accept(NANO_BOOTS);
                entries.accept(ElectricItems.withFullCharge(NANO_BOOTS));

                entries.accept(BATTERY_PACK);
                entries.accept(ElectricItems.withFullCharge(BATTERY_PACK));

                entries.accept(BATTERY);
                entries.accept(ElectricItems.withFullCharge(BATTERY));
                entries.accept(ENERGY_CRYSTAL);
                entries.accept(ElectricItems.withFullCharge(ENERGY_CRYSTAL));

                entries.accept(CANISTER);
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
                entries.accept(QUARTZ_DUST);
                entries.accept(NETHERITE_SCRAP_DUST);
                entries.accept(ENDER_PEARL_DUST);
                entries.accept(SILICON_DUST);
                entries.accept(SILICON_BRONZE_DUST);

                entries.accept(STICKY_RESIN);
                entries.accept(RUBBER);
                entries.accept(REFINED_IRON_FOIL);
                entries.accept(COPPER_WIRE);
                entries.accept(CIRCUIT);
                entries.accept(ELECTRIC_MOTOR);
                entries.accept(TRANSFORMER);
                entries.accept(REDSTONE_FLUX_LASER);
                entries.accept(MIXED_METAL_INGOT);
                entries.accept(COMPOSITE_PLATE);
                entries.accept(CARBON_REDSTONE_MATRIX);
                entries.accept(CONDUCTIVE_CARBON);
                entries.accept(REINFORCED_SUPERMETAL);

                entries.accept(OVERCLOCK_UPGRADE);
                entries.accept(CAPACITOR_UPGRADE);
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
        public static final TagKey<Item> ELECTRIC_FURNACE_UPGRADES = TagKey.create(Registries.ITEM, modId("electric_furnace_upgrades"));
        public static final TagKey<Item> MACERATOR_UPGRADES = TagKey.create(Registries.ITEM, modId("macerator_upgrades"));
        public static final TagKey<Item> COMPRESSOR_UPGRADES = TagKey.create(Registries.ITEM, modId("compressor_upgrades"));
        public static final TagKey<Item> EXTRACTOR_UPGRADES = TagKey.create(Registries.ITEM, modId("extractor_upgrades"));
    }

    public static class BlockEntityTypes {
        public static final BlockEntityType<GeneratorBlockEntity> GENERATOR = register("generator", GeneratorBlockEntity::new, Blocks.GENERATOR);
        public static final BlockEntityType<ElectricFurnaceBlockEntity> ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlockEntity::new, Blocks.ELECTRIC_FURNACE);
        public static final BlockEntityType<MaceratorBlockEntity> MACERATOR = register("macerator", MaceratorBlockEntity::new, Blocks.MACERATOR);
        public static final BlockEntityType<CompressorBlockEntity> COMPRESSOR = register("compressor", CompressorBlockEntity::new, Blocks.COMPRESSOR);
        public static final BlockEntityType<ExtractorBlockEntity> EXTRACTOR = register("extractor", ExtractorBlockEntity::new, Blocks.EXTRACTOR);
        public static final BlockEntityType<MatterGeneratorBlockEntity> MATTER_GENERATOR = register("matter_generator", MatterGeneratorBlockEntity::new, Blocks.MATTER_GENERATOR);

        public static final BlockEntityType<BatteryArrayBlockEntity> BATTERY_ARRAY = register("battery_array", BatteryArrayBlockEntity::new, Blocks.BATTERY_ARRAY);
        public static final BlockEntityType<EnergyCrystalArrayBlockEntity> ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayBlockEntity::new, Blocks.ENERGY_CRYSTAL_ARRAY);

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

        public static final MenuType<BatteryArrayMenu> BATTERY_ARRAY = register("battery_array", BatteryArrayMenu::new);
        public static final MenuType<EnergyStorageMenu> ENERGY_CRYSTAL_ARRAY = register("energy_crystal_array", EnergyCrystalArrayMenu::new);

        public static void initialize() {
        }

        public static <T extends AbstractContainerMenu> MenuType<T> register(String name, MenuType.MenuSupplier<T> constructor) {
            var key = ResourceKey.create(Registries.MENU, modId(name));
            var type = new MenuType<>(constructor, FeatureFlags.VANILLA_SET);

            return Registry.register(BuiltInRegistries.MENU, key, type);
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
        public static final RecipeSerializer<MaceratingRecipe> MACERATING = register("macerating", new ClassicMachineRecipe.Serializer<>(MaceratingRecipe::new, MaceratingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<CompressingRecipe> COMPRESSING = register("compressing", new ClassicMachineRecipe.Serializer<>(CompressingRecipe::new, CompressingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<ExtractingRecipe> EXTRACTING = register("extracting", new ClassicMachineRecipe.Serializer<>(ExtractingRecipe::new, ExtractingRecipe.DEFAULT_ENERGY));
        public static final RecipeSerializer<MatterGeneratingRecipe> MATTER_GENERATING = register("matter_generating", new MatterGeneratingRecipe.Serializer());
        public static final RecipeSerializer<ElectricShapedRecipe> ELECTRIC_SHAPED_CRAFTING = register("electric_shaped_crafting", new ElectricShapedRecipe.Serializer());

        public static void initialize() {
        }

        private static <T extends Recipe<?>> RecipeSerializer<T> register(String name, RecipeSerializer<T> serializer) {
            return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, modId(name), serializer);
        }
    }

    public static class CustomPayloads {
        public static final CustomPacketPayload.Type<OverloadCablePayload> OVERLOAD_CABLE = new CustomPacketPayload.Type<>(modId("overload_cable"));

        public static void initialize() {
            PayloadTypeRegistry.clientboundPlay().register(OVERLOAD_CABLE, OverloadCablePayload.STREAM_CODEC);
        }

        @Environment(EnvType.CLIENT)
        public static void initializeClient() {
            ClientPlayNetworking.registerGlobalReceiver(OVERLOAD_CABLE, (payload, context) -> {
                var level = context.client().level;

                for (var destructionEntry : payload.destructionProgress().object2IntEntrySet()) {
                    var pos = destructionEntry.getKey();
                    var value = destructionEntry.getIntValue();

                    level.destroyBlockProgress(-Math.abs(pos.hashCode()), pos, value);
                    if (value > 0) {
                        for (int i = 0; i < 2; i++) {
                            float x = pos.getX() + 0.25F + level.getRandom().nextFloat() * 0.5F;
                            float y = pos.getY() + 0.25F + level.getRandom().nextFloat() * 0.5F;
                            float z = pos.getZ() + 0.25F + level.getRandom().nextFloat() * 0.5F;

                            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
                            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
                        }
                    }
                }
            });
        }
    }
}