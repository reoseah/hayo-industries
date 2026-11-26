package hayo.common;

import com.mojang.serialization.*;

import java.util.*;
import java.util.stream.Stream;

public class ConfigCodecs {
    private static final String VALUE_KEY = "value";

    public record Meta<A>(
            String key,
            Encoder<A> encoder,
            A value
    ) {
        public Meta {
            Objects.requireNonNull(key);
            Objects.requireNonNull(encoder);
            Objects.requireNonNull(value);

            if (key.equals(VALUE_KEY)) {
                throw new IllegalArgumentException("'value' is reserved for the actual config value");
            }
        }
    }

    public static <A> Meta<A> meta(String key, Encoder<A> codec, A value) {
        return new Meta<>(key, codec, value);
    }

    public static Meta<String> comment(String comment) {
        return new Meta<>("//", Codec.STRING, comment);
    }

    public static Meta<List<String>> comment(String... comment) {
        return new Meta<>("//", Codec.STRING.listOf(), Arrays.asList(comment));
    }

    public static Meta<List<String>> comment(List<String> lines) {
        return new Meta<>("//", Codec.STRING.listOf(), lines);
    }

    public static <A> Meta<A> defaultValue(Codec<A> codec, A value) {
        return new Meta<>("//default", codec, value);
    }

    public static Meta<Integer> defaultValue(int value) {
        return new Meta<>("//default", Codec.INT, value);
    }

    public static Meta<String> range(String range) {
        return new Meta<>("//range", Codec.STRING, range);
    }

    public static Meta<String> regex(String regex) {
        return new Meta<>("//regex", Codec.STRING, regex);
    }

    public static Meta<String> reference(String reference) {
        return new Meta<>("//reference", Codec.STRING, reference);
    }

    public static <A> MapCodec<A> wrap(
            MapCodec<A> valueCodec,
            Meta<?>... metadata
    ) {
        return wrap(valueCodec.codec(), metadata);
    }

    public static <A> MapCodec<A> wrap(
            Codec<A> valueCodec,
            Meta<?>... metadataEntries
    ) {
        Objects.requireNonNull(valueCodec);

        Arrays.sort(metadataEntries, Comparator.comparing(meta -> meta.key));
        var metadata = List.of(metadataEntries);

        var keys = new HashSet<>();
        for (var meta : metadata) {
            if (!keys.add(meta.key())) {
                throw new IllegalArgumentException("Duplicate config metadata key: " + meta.key());
            }
        }

        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.concat(
                        Stream.of(ops.createString(VALUE_KEY)),
                        metadata.stream().map(entry -> ops.createString(entry.key()))
                );
            }

            @Override
            public <T> RecordBuilder<T> encode(
                    A input,
                    DynamicOps<T> ops,
                    RecordBuilder<T> prefix
            ) {
                var builder = prefix;

                for (Meta<?> meta : metadata) {
                    builder = builder.add(
                            meta.key(),
                            ((Encoder<Object>) meta.encoder).encodeStart(ops, (Object) meta.value)
                    );
                }

                builder = builder.add(VALUE_KEY, valueCodec.encodeStart(ops, input));

                return builder;
            }

            @Override
            public <T> DataResult<A> decode(
                    DynamicOps<T> ops,
                    MapLike<T> input
            ) {
                T value = input.get(VALUE_KEY);

                if (value == null) {
                    return DataResult.error(() -> "Missing required property 'value'");
                }

                return valueCodec.parse(ops, value);
            }

            @Override
            public String toString() {
                return "ConfigMapCodec[" + valueCodec + "]";
            }
        };
    }
}
