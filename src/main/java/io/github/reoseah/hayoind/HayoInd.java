package io.github.reoseah.hayoind;

import io.github.reoseah.hayoind.block.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public class HayoInd {
    public static final String MOD_ID = "hayoind";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final CreativeModeTab TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.hayoind")).icon(() -> new ItemStack(Blocks.MACERATOR)).build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modLocation("main"), TAB);

        Blocks.initialize();
        Items.initialize();
    }


    @Environment(EnvType.CLIENT)
    public static void initializeClient() {
        BlockRenderLayerMap.putBlocks(ChunkSectionLayer.CUTOUT, Blocks.REINFORCED_GLASS, Blocks.FERRU);
    }


    public static ResourceLocation modLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, modLocation(location));
    }

    public static class Blocks {
        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block ADVANCED_MACHINE_BLOCK = register("advanced_machine_block", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block CHIPBOARD = register("chipboard", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.WOOD));
        public static final Block REINFORCED_STONE = register("reinforced_stone", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.STONE));
        public static final Block REINFORCED_STONE_TILES = register("reinforced_stone_tiles", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.STONE));
        public static final Block REINFORCED_GLASS = register("reinforced_glass", TransparentBlock::new, BlockBehaviour.Properties.of().strength(3F).noOcclusion().sound(SoundType.GLASS));
        public static final Block ELECTRIC_FURNACE = register("electric_furnace", ElectricFurnaceBlock::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block MACERATOR = register("macerator", MaceratorBlock::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block EXTRACTOR = register("extractor", ExtractorBlock::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block AUTOMATED_FERTILIZER = register("automated_fertilizer", AutomatedFertilizerBlock::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));

        public static final Block FERRU = register("ferru", FerruBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT).noCollision().randomTicks().instabreak().sound(SoundType.CROP).pushReaction(PushReaction.DESTROY));

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
        public static final Item REINFORCED_STONE_TILES = registerBlock(Blocks.REINFORCED_STONE_TILES);
        public static final Item REINFORCED_GLASS = registerBlock(Blocks.REINFORCED_GLASS);
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
                entries.accept(CHIPBOARD);
                entries.accept(REINFORCED_STONE);
                entries.accept(REINFORCED_STONE_TILES);
                entries.accept(REINFORCED_GLASS);

                entries.accept(ELECTRIC_FURNACE);
                entries.accept(MACERATOR);
                entries.accept(EXTRACTOR);
                entries.accept(AUTOMATED_FERTILIZER);

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
}