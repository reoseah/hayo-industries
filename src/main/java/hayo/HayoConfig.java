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

import static io.github.reoseah.pricklycodecs.ConfigCodecs.defaultValue;
import static io.github.reoseah.pricklycodecs.ConfigCodecs.wrap;

public record HayoConfig(EnergyStorageConfig energyStorage) {
    public static final Logger LOGGER = LoggerFactory.getLogger("HAYO/Config");
    public static final MapCodec<HayoConfig> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    EnergyStorageConfig.CONFIG_CODEC.fieldOf("energy_storage").forGetter(HayoConfig::energyStorage)
            )
            .apply(instance, HayoConfig::new)
    );

    private static HayoConfig instance;

    public static @NotNull HayoConfig instance() {
        return instance;
    }

    public static void createOrLoad() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("hayo.json");

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

    public record EnergyStorageConfig(int mfeCapacity, int mfeTransferRate, int mfsuCapacity, int mfsuTransferRate) {
        public static final int MFE_DEFAULT_CAPACITY = 400_000;
        public static final int MFE_DEFAULT_TRANSFER_RATE = 128;
        public static final int MFSU_DEFAULT_CAPACITY = 4_000_000;
        public static final int MFSU_DEFAULT_TRANSFER_RATE = 512;

        public static final MapCodec<EnergyStorageConfig> CONFIG_CODEC = RecordCodecBuilder.mapCodec(instance -> instance
                .group(
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(MFE_DEFAULT_CAPACITY)).fieldOf("mfe_capacity").orElse(MFE_DEFAULT_CAPACITY).forGetter(EnergyStorageConfig::mfeCapacity),
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(MFE_DEFAULT_TRANSFER_RATE)).fieldOf("mfe_transfer_rate").orElse(MFE_DEFAULT_TRANSFER_RATE).forGetter(EnergyStorageConfig::mfeTransferRate),

                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(MFSU_DEFAULT_CAPACITY)).fieldOf("mfe_capacity").orElse(MFSU_DEFAULT_CAPACITY).forGetter(EnergyStorageConfig::mfsuCapacity),
                        wrap(ExtraCodecs.POSITIVE_INT, defaultValue(MFSU_DEFAULT_TRANSFER_RATE)).fieldOf("mfe_transfer_rate").orElse(MFSU_DEFAULT_TRANSFER_RATE).forGetter(EnergyStorageConfig::mfsuTransferRate)
                )
                .apply(instance, EnergyStorageConfig::new)
        );
    }
}
