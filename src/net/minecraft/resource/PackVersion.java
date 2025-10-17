/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.resource;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiFunction;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.dynamic.Range;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public record PackVersion(int major, int minor) implements Comparable<PackVersion>
{
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<PackVersion> CODEC = PackVersion.createCodec(0);
    public static final Codec<PackVersion> ANY_CODEC = PackVersion.createCodec(Integer.MAX_VALUE);

    private static Codec<PackVersion> createCodec(int impliedMinorVersion) {
        return Codecs.listOrSingle(Codecs.NON_NEGATIVE_INT, Codecs.NON_NEGATIVE_INT.listOf(1, 256)).xmap(list -> list.size() > 1 ? PackVersion.of((Integer)list.getFirst(), (Integer)list.get(1)) : PackVersion.of((Integer)list.getFirst(), impliedMinorVersion), version -> version.minor != impliedMinorVersion ? List.of(Integer.valueOf(version.major()), Integer.valueOf(version.minor())) : List.of(Integer.valueOf(version.major())));
    }

    public static <ResultType, HolderType extends FormatHolder> DataResult<List<ResultType>> validate(List<HolderType> holders, int lastOldPackVersion, BiFunction<HolderType, Range<PackVersion>, ResultType> toResult) {
        int j = holders.stream().map(FormatHolder::format).mapToInt(Format::minMajor).min().orElse(Integer.MAX_VALUE);
        ArrayList<ResultType> list2 = new ArrayList<ResultType>(holders.size());
        for (FormatHolder lv : holders) {
            Format lv2 = lv.format();
            if (lv2.min().isEmpty() && lv2.max().isEmpty() && lv2.supported().isEmpty()) {
                LOGGER.warn("Unknown or broken overlay entry " + String.valueOf(lv));
                continue;
            }
            DataResult<Range<PackVersion>> dataResult = lv2.validate(lastOldPackVersion, false, j <= lastOldPackVersion, "Overlay \"" + String.valueOf(lv) + "\"", "formats");
            if (dataResult.isSuccess()) {
                list2.add(toResult.apply(lv, dataResult.getOrThrow()));
                continue;
            }
            return DataResult.error(dataResult.error().get()::message);
        }
        return DataResult.success(List.copyOf(list2));
    }

    @VisibleForTesting
    public static int getLastOldPackVersion(ResourceType type) {
        return switch (type) {
            default -> throw new MatchException(null, null);
            case ResourceType.CLIENT_RESOURCES -> 64;
            case ResourceType.SERVER_DATA -> 81;
        };
    }

    public static MapCodec<Range<PackVersion>> createRangeCodec(ResourceType type) {
        int i = PackVersion.getLastOldPackVersion(type);
        return Format.PACK_CODEC.flatXmap(format -> format.validate(i, true, false, "Pack", "supported_formats"), range -> DataResult.success(Format.ofRange(range, i)));
    }

    public static PackVersion of(int major, int minor) {
        return new PackVersion(major, minor);
    }

    public static PackVersion of(int major) {
        return new PackVersion(major, 0);
    }

    public Range<PackVersion> majorRange() {
        return new Range<PackVersion>(this, PackVersion.of(this.major, Integer.MAX_VALUE));
    }

    @Override
    public int compareTo(PackVersion arg) {
        int i = Integer.compare(this.major(), arg.major());
        if (i != 0) {
            return i;
        }
        return Integer.compare(this.minor(), arg.minor());
    }

    @Override
    public String toString() {
        if (this.minor == Integer.MAX_VALUE) {
            return String.format(Locale.ROOT, "%d.*", this.major());
        }
        return String.format(Locale.ROOT, "%d.%d", this.major(), this.minor());
    }

    @Override
    public /* synthetic */ int compareTo(Object o) {
        return this.compareTo((PackVersion)o);
    }

    public static interface FormatHolder {
        public Format format();
    }

    public record Format(Optional<PackVersion> min, Optional<PackVersion> max, Optional<Integer> format, Optional<Range<Integer>> supported) {
        static final MapCodec<Format> PACK_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(CODEC.optionalFieldOf("min_format").forGetter(Format::min), ANY_CODEC.optionalFieldOf("max_format").forGetter(Format::max), Codec.INT.optionalFieldOf("pack_format").forGetter(Format::format), Range.createCodec(Codec.INT).optionalFieldOf("supported_formats").forGetter(Format::supported)).apply((Applicative<Format, ?>)instance, Format::new));
        public static final MapCodec<Format> OVERLAY_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(CODEC.optionalFieldOf("min_format").forGetter(Format::min), ANY_CODEC.optionalFieldOf("max_format").forGetter(Format::max), Range.createCodec(Codec.INT).optionalFieldOf("formats").forGetter(Format::supported)).apply((Applicative<Format, ?>)instance, (min, max, supported) -> new Format((Optional<PackVersion>)min, (Optional<PackVersion>)max, min.map(PackVersion::major), (Optional<Range<Integer>>)supported)));

        public static Format ofRange(Range<PackVersion> range, int lastOldPackVersion) {
            Range<Integer> lv = range.map(PackVersion::major);
            return new Format(Optional.of(range.minInclusive()), Optional.of(range.maxInclusive()), lv.contains(lastOldPackVersion) ? Optional.of(lv.minInclusive()) : Optional.empty(), lv.contains(lastOldPackVersion) ? Optional.of(new Range<Integer>(lv.minInclusive(), lv.maxInclusive())) : Optional.empty());
        }

        public int minMajor() {
            if (this.min.isPresent()) {
                if (this.supported.isPresent()) {
                    return Math.min(this.min.get().major(), this.supported.get().minInclusive());
                }
                return this.min.get().major();
            }
            if (this.supported.isPresent()) {
                return this.supported.get().minInclusive();
            }
            return Integer.MAX_VALUE;
        }

        public DataResult<Range<PackVersion>> validate(int lastOldPackVersion, boolean pack, boolean supportsOld, String packDescriptor, String supportedFormatsKey) {
            if (this.min.isPresent() != this.max.isPresent()) {
                return DataResult.error(() -> packDescriptor + " missing field, must declare both min_format and max_format");
            }
            if (supportsOld && this.supported.isEmpty()) {
                return DataResult.error(() -> packDescriptor + " missing required field " + supportedFormatsKey + ", must be present in all overlays for any overlays to work across game versions");
            }
            if (this.min.isPresent()) {
                return this.validateVersions(lastOldPackVersion, pack, supportsOld, packDescriptor, supportedFormatsKey);
            }
            if (this.supported.isPresent()) {
                return this.validateSupportedFormats(lastOldPackVersion, pack, packDescriptor, supportedFormatsKey);
            }
            if (pack && this.format.isPresent()) {
                int j = this.format.get();
                if (j > lastOldPackVersion) {
                    return DataResult.error(() -> packDescriptor + " declares support for version newer than " + lastOldPackVersion + ", but is missing mandatory fields min_format and max_format");
                }
                return DataResult.success(new Range<PackVersion>(PackVersion.of(j)));
            }
            return DataResult.error(() -> packDescriptor + " could not be parsed, missing format version information");
        }

        private DataResult<Range<PackVersion>> validateVersions(int lastOldPackVersion, boolean pack, boolean supportsOld, String packDescriptor, String supportedFormatsKey) {
            int j = this.min.get().major();
            int k = this.max.get().major();
            if (this.min.get().compareTo(this.max.get()) > 0) {
                return DataResult.error(() -> packDescriptor + " min_format (" + String.valueOf(this.min.get()) + ") is greater than max_format (" + String.valueOf(this.max.get()) + ")");
            }
            if (j > lastOldPackVersion && !supportsOld) {
                String string3;
                if (this.supported.isPresent()) {
                    return DataResult.error(() -> packDescriptor + " key " + supportedFormatsKey + " is deprecated starting from pack format " + (lastOldPackVersion + 1) + ". Remove " + supportedFormatsKey + " from your pack.mcmeta.");
                }
                if (pack && this.format.isPresent() && (string3 = this.validateMainFormat(j, k)) != null) {
                    return DataResult.error(() -> string3);
                }
            } else {
                if (this.supported.isPresent()) {
                    Range<Integer> lv = this.supported.get();
                    if (lv.minInclusive() != j) {
                        return DataResult.error(() -> packDescriptor + " version declaration mismatch between " + supportedFormatsKey + " (from " + String.valueOf(lv.minInclusive()) + ") and min_format (" + String.valueOf(this.min.get()) + ")");
                    }
                    if (lv.maxInclusive() != k && lv.maxInclusive() != lastOldPackVersion) {
                        return DataResult.error(() -> packDescriptor + " version declaration mismatch between " + supportedFormatsKey + " (up to " + String.valueOf(lv.maxInclusive()) + ") and max_format (" + String.valueOf(this.max.get()) + ")");
                    }
                } else {
                    return DataResult.error(() -> packDescriptor + " declares support for format " + j + ", but game versions supporting formats 17 to " + lastOldPackVersion + " require a " + supportedFormatsKey + " field. Add \"" + supportedFormatsKey + "\": [" + j + ", " + lastOldPackVersion + "] or require a version greater or equal to " + (lastOldPackVersion + 1) + ".0.");
                }
                if (pack) {
                    if (this.format.isPresent()) {
                        String string3 = this.validateMainFormat(j, k);
                        if (string3 != null) {
                            return DataResult.error(() -> string3);
                        }
                    } else {
                        return DataResult.error(() -> packDescriptor + " declares support for formats up to " + lastOldPackVersion + ", but game versions supporting formats 17 to " + lastOldPackVersion + " require a pack_format field. Add \"pack_format\": " + j + " or require a version greater or equal to " + (lastOldPackVersion + 1) + ".0.");
                    }
                }
            }
            return DataResult.success(new Range<PackVersion>(this.min.get(), this.max.get()));
        }

        private DataResult<Range<PackVersion>> validateSupportedFormats(int lastOldPackVersion, boolean pack, String packDescriptor, String supportedFormatsKey) {
            Range<Integer> lv = this.supported.get();
            int j = lv.minInclusive();
            int k = lv.maxInclusive();
            if (k > lastOldPackVersion) {
                return DataResult.error(() -> packDescriptor + " declares support for version newer than " + lastOldPackVersion + ", but is missing mandatory fields min_format and max_format");
            }
            if (pack) {
                if (this.format.isPresent()) {
                    String string3 = this.validateMainFormat(j, k);
                    if (string3 != null) {
                        return DataResult.error(() -> string3);
                    }
                } else {
                    return DataResult.error(() -> packDescriptor + " declares support for formats up to " + lastOldPackVersion + ", but game versions supporting formats 17 to " + lastOldPackVersion + " require a pack_format field. Add \"pack_format\": " + j + " or require a version greater or equal to " + (lastOldPackVersion + 1) + ".0.");
                }
            }
            return DataResult.success(new Range<Integer>(j, k).map(PackVersion::of));
        }

        @Nullable
        private String validateMainFormat(int min, int max) {
            int k = this.format.get();
            if (k < min || k > max) {
                return "Pack declared support for versions " + min + " to " + max + " but declared main format is " + k;
            }
            if (k < 15) {
                return "Multi-version packs cannot support minimum version of less than 15, since this will leave versions in range unable to load pack.";
            }
            return null;
        }
    }
}

