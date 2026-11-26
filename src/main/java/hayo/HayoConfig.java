package hayo;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StrictJsonParser;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;

import static hayo.common.PrickleCodecs.*;

public record HayoConfig(GenerationConfig generation, EnergyStorageConfig energyStorage) {
    public static final Logger LOGGER = LoggerFactory.getLogger("HAYO/Config");
    public static final MapCodec<HayoConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    GenerationConfig.CONFIG_CODEC.fieldOf("generation").orElseGet(GenerationConfig::new).forGetter(HayoConfig::generation),
                    EnergyStorageConfig.CONFIG_CODEC.fieldOf("energy_storage").orElseGet(EnergyStorageConfig::new).forGetter(HayoConfig::energyStorage)
            )
            .apply(instance, HayoConfig::new)
    );

    // TODO: sync server config to client
    private static HayoConfig instance;

    public static @NotNull HayoConfig instance() {
        return instance;
    }

    public static void createOrLoad() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("hayo-industries.json");

        if (Files.exists(path)) {
            try (var reader = Files.newBufferedReader(path)) {
                instance = CODEC.codec().parse(JsonOps.INSTANCE, StrictJsonParser.parse(reader)).result().orElseThrow();
            } catch (Exception e) {
                throw new RuntimeException("Error while reading config", e);
            }
        } else {
            LOGGER.info("Config file doesn't exist, creating default one...");

            instance = CODEC.codec().parse(JsonOps.INSTANCE, new JsonObject()).result().orElseThrow();

            try (var writer = Files.newBufferedWriter(path)) {
                var builder = CODEC.encode(instance, JsonOps.INSTANCE, JsonOps.INSTANCE.mapBuilder());
                var json = builder.build(JsonOps.INSTANCE.empty()).getOrThrow();

                var gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(json, gson.newJsonWriter(writer));
            } catch (Exception e) {
                throw new RuntimeException("Error while writing default config", e);
            }
        }
    }

    public record GenerationConfig(int generatorFuelRate, int generatorOutput) {
        public static final int DEFAULT_GENERATOR_FUEL_RATE = 5;
        public static final int DEFAULT_GENERATOR_OUTPUT = 10;

        public GenerationConfig() {
            this(
                    DEFAULT_GENERATOR_FUEL_RATE, DEFAULT_GENERATOR_OUTPUT
            );
        }

        public static final MapCodec<GenerationConfig> CONFIG_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        wrap(
                                ExtraCodecs.POSITIVE_INT,
                                defaultValue(DEFAULT_GENERATOR_FUEL_RATE),
                                comment("Energy that generator creates per tick of fuel.")
                        )
                                .fieldOf("generator_fuel_rate")
                                .orElse(DEFAULT_GENERATOR_FUEL_RATE)
                                .forGetter(GenerationConfig::generatorFuelRate),
                        wrap(
                                ExtraCodecs.POSITIVE_INT,
                                defaultValue(DEFAULT_GENERATOR_OUTPUT),
                                comment("Energy that generator can create per tick (or more precisely, move from a hidden fuel energy to visible energy).")
                        )
                                .fieldOf("generator_output")
                                .orElse(DEFAULT_GENERATOR_OUTPUT)
                                .forGetter(GenerationConfig::generatorOutput)
                )
                .apply(instance, GenerationConfig::new)
        );
    }

    public record EnergyStorageConfig(int batboxCapacity, int batboxTransferRate, int mfeCapacity, int mfeTransferRate,
                                      int mfsuCapacity, int mfsuTransferRate) {
        public static final int DEFAULT_BATBOX_CAPACITY = 40_000;
        public static final int DEFAULT_BATBOX_TRANSFER_RATE = 32;
        public static final int DEFAULT_MFE_CAPACITY = 400_000;
        public static final int DEFAULT_MFE_TRANSFER_RATE = 128;
        public static final int DEFAULT_MFSU_CAPACITY = 4_000_000;
        public static final int DEFAULT_MFSU_TRANSFER_RATE = 512;

        public EnergyStorageConfig() {
            this(
                    DEFAULT_BATBOX_CAPACITY, DEFAULT_BATBOX_TRANSFER_RATE,
                    DEFAULT_MFE_CAPACITY, DEFAULT_MFE_TRANSFER_RATE,
                    DEFAULT_MFSU_CAPACITY, DEFAULT_MFSU_TRANSFER_RATE
            );
        }

        public static final MapCodec<EnergyStorageConfig> CONFIG_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_BATBOX_CAPACITY)).fieldOf("batbox_capacity").orElse(DEFAULT_BATBOX_CAPACITY).forGetter(EnergyStorageConfig::batboxCapacity),
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_BATBOX_TRANSFER_RATE)).fieldOf("batbox_transfer_rate").orElse(DEFAULT_BATBOX_TRANSFER_RATE).forGetter(EnergyStorageConfig::batboxTransferRate),

                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_MFE_CAPACITY)).fieldOf("mfe_capacity").orElse(DEFAULT_MFE_CAPACITY).forGetter(EnergyStorageConfig::mfeCapacity),
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_MFE_TRANSFER_RATE)).fieldOf("mfe_transfer_rate").orElse(DEFAULT_MFE_TRANSFER_RATE).forGetter(EnergyStorageConfig::mfeTransferRate),

                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_MFSU_CAPACITY)).fieldOf("mfsu_capacity").orElse(DEFAULT_MFSU_CAPACITY).forGetter(EnergyStorageConfig::mfsuCapacity),
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(DEFAULT_MFSU_TRANSFER_RATE)).fieldOf("mfsu_transfer_rate").orElse(DEFAULT_MFSU_TRANSFER_RATE).forGetter(EnergyStorageConfig::mfsuTransferRate)
                )
                .apply(instance, EnergyStorageConfig::new)
        );
    }
}
