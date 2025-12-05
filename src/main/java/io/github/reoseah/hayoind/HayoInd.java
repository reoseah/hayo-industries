package io.github.reoseah.hayoind;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiFunction;
import java.util.function.Function;

public class HayoInd implements ModInitializer {
    public static final String MOD_ID = "hayoind";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final CreativeModeTab TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.hayoind")).icon(() -> new ItemStack(Blocks.MACHINE_BLOCK)).build();

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, modLocation("main"), TAB);

        Blocks.initialize();
        Items.initialize();
    }

    public static ResourceLocation modLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, modLocation(location));
    }

    public static class Blocks {
        public static final Block MACHINE_BLOCK = register("machine_block", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));
        public static final Block MACERATOR = register("macerator", Block::new, BlockBehaviour.Properties.of().strength(3F).sound(SoundType.METAL));

        public static void initialize() {
        }

        private static Block register(String name, Function<BlockBehaviour.Properties, Block> constructor, BlockBehaviour.Properties properties) {
            var key = ResourceKey.create(Registries.BLOCK, modLocation(name));

            return Registry.register(BuiltInRegistries.BLOCK, key, constructor.apply(properties.setId(key)));
        }
    }

    public static class Items {
        public static final Item MACHINE_BLOCK = registerBlock(Blocks.MACHINE_BLOCK);
        public static final Item MACERATOR = registerBlock(Blocks.MACERATOR);

        public static void initialize() {
            ItemGroupEvents.modifyEntriesEvent(key(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.prepend(MACHINE_BLOCK);
                entries.prepend(MACERATOR);
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