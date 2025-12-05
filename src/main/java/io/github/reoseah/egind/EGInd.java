package io.github.reoseah.egind;

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

public class EGInd implements ModInitializer {
    public static final String MOD_ID = "egind";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final CreativeModeTab TAB = FabricItemGroup.builder().title(Component.translatable("itemGroup.egind")).icon(() -> new ItemStack(Blocks.MACHINE_BLOCK)).build();

    @Override
    public void onInitialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("main"), TAB);

        Blocks.initialize();
        Items.initialize();
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static <T> ResourceKey<T> key(ResourceKey<Registry<T>> registryKey, String location) {
        return ResourceKey.create(registryKey, id(location));
    }

    public static class Blocks {
        public static final Block MACHINE_BLOCK = new Block(properties("machine_block").strength(3F).sound(SoundType.METAL));

        public static void initialize() {
            register("machine_block", MACHINE_BLOCK);
        }

        private static BlockBehaviour.Properties properties(String name) {
            return BlockBehaviour.Properties.of().setId(key(Registries.BLOCK, name));
        }

        private static void register(String name, Block entry) {
            Registry.register(BuiltInRegistries.BLOCK, id(name), entry);
        }
    }

    public static class Items {
        public static final Item MACHINE_BLOCK = new BlockItem(Blocks.MACHINE_BLOCK, properties("machine_block").useBlockDescriptionPrefix());

        public static void initialize() {
            register("machine_block", MACHINE_BLOCK);
        }

        private static void register(String name, Item entry) {
            Registry.register(BuiltInRegistries.ITEM, id(name), entry);

            ItemGroupEvents.modifyEntriesEvent(key(Registries.CREATIVE_MODE_TAB, "main")).register((entries) -> {
                entries.prepend(MACHINE_BLOCK);
            });
        }

        private static Item.Properties properties(String name) {
            return new Item.Properties().setId(key(Registries.ITEM, name));
        }
    }
}