/*
 * Decompiled with CFR 0.2.2 (FabricMC 7c48b8c4).
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.NbtByteArray;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.nbt.NbtPrimitive;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.nbt.visitor.NbtOrderedStringFormatter;
import net.minecraft.nbt.visitor.NbtTextFormatter;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.state.State;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Property;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

public final class NbtHelper {
    private static final Comparator<NbtList> BLOCK_POS_COMPARATOR = Comparator.comparingInt(nbt -> nbt.getInt(1, 0)).thenComparingInt(nbt -> nbt.getInt(0, 0)).thenComparingInt(nbt -> nbt.getInt(2, 0));
    private static final Comparator<NbtList> ENTITY_POS_COMPARATOR = Comparator.comparingDouble(nbt -> nbt.getDouble(1, 0.0)).thenComparingDouble(nbt -> nbt.getDouble(0, 0.0)).thenComparingDouble(nbt -> nbt.getDouble(2, 0.0));
    private static final Codec<RegistryKey<Block>> BLOCK_KEY_CODEC = RegistryKey.createCodec(RegistryKeys.BLOCK);
    public static final String DATA_KEY = "data";
    private static final char LEFT_CURLY_BRACKET = '{';
    private static final char RIGHT_CURLY_BRACKET = '}';
    private static final String COMMA = ",";
    private static final char COLON = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on(",");
    private static final Splitter COLON_SPLITTER = Splitter.on(':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_33229 = 2;
    private static final int field_33230 = -1;

    private NbtHelper() {
    }

    @VisibleForTesting
    public static boolean matches(@Nullable NbtElement standard, @Nullable NbtElement subject, boolean ignoreListOrder) {
        if (standard == subject) {
            return true;
        }
        if (standard == null) {
            return true;
        }
        if (subject == null) {
            return false;
        }
        if (!standard.getClass().equals(subject.getClass())) {
            return false;
        }
        if (standard instanceof NbtCompound) {
            NbtCompound lv = (NbtCompound)standard;
            NbtCompound lv2 = (NbtCompound)subject;
            if (lv2.getSize() < lv.getSize()) {
                return false;
            }
            for (Map.Entry<String, NbtElement> entry : lv.entrySet()) {
                NbtElement lv3 = entry.getValue();
                if (NbtHelper.matches(lv3, lv2.get(entry.getKey()), ignoreListOrder)) continue;
                return false;
            }
            return true;
        }
        if (standard instanceof NbtList) {
            NbtList lv4 = (NbtList)standard;
            if (ignoreListOrder) {
                NbtList lv5 = (NbtList)subject;
                if (lv4.isEmpty()) {
                    return lv5.isEmpty();
                }
                if (lv5.size() < lv4.size()) {
                    return false;
                }
                for (NbtElement lv6 : lv4) {
                    boolean bl2 = false;
                    for (NbtElement lv7 : lv5) {
                        if (!NbtHelper.matches(lv6, lv7, ignoreListOrder)) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2) continue;
                    return false;
                }
                return true;
            }
        }
        return standard.equals(subject);
    }

    public static BlockState toBlockState(RegistryEntryLookup<Block> blockLookup, NbtCompound nbt) {
        Optional optional = nbt.get("Name", BLOCK_KEY_CODEC).flatMap(blockLookup::getOptional);
        if (optional.isEmpty()) {
            return Blocks.AIR.getDefaultState();
        }
        Block lv = (Block)((RegistryEntry)optional.get()).value();
        BlockState lv2 = lv.getDefaultState();
        Optional<NbtCompound> optional2 = nbt.getCompound("Properties");
        if (optional2.isPresent()) {
            StateManager<Block, BlockState> lv3 = lv.getStateManager();
            for (String string : optional2.get().getKeys()) {
                Property<?> lv4 = lv3.getProperty(string);
                if (lv4 == null) continue;
                lv2 = NbtHelper.withProperty(lv2, lv4, string, optional2.get(), nbt);
            }
        }
        return lv2;
    }

    private static <S extends State<?, S>, T extends Comparable<T>> S withProperty(S state, Property<T> property, String key, NbtCompound properties, NbtCompound root) {
        Optional optional = properties.getString(key).flatMap(property::parse);
        if (optional.isPresent()) {
            return (S)((State)state.with(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", key, properties.get(key), root);
        return state;
    }

    public static NbtCompound fromBlockState(BlockState state) {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Registries.BLOCK.getId(state.getBlock()).toString());
        Map<Property<?>, Comparable<?>> map = state.getEntries();
        if (!map.isEmpty()) {
            NbtCompound lv2 = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> lv3 = entry.getKey();
                lv2.putString(lv3.getName(), NbtHelper.nameValue(lv3, entry.getValue()));
            }
            lv.put("Properties", lv2);
        }
        return lv;
    }

    public static NbtCompound fromFluidState(FluidState state) {
        NbtCompound lv = new NbtCompound();
        lv.putString("Name", Registries.FLUID.getId(state.getFluid()).toString());
        Map<Property<?>, Comparable<?>> map = state.getEntries();
        if (!map.isEmpty()) {
            NbtCompound lv2 = new NbtCompound();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> lv3 = entry.getKey();
                lv2.putString(lv3.getName(), NbtHelper.nameValue(lv3, entry.getValue()));
            }
            lv.put("Properties", lv2);
        }
        return lv;
    }

    private static <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
        return property.name(value);
    }

    public static String toFormattedString(NbtElement nbt) {
        return NbtHelper.toFormattedString(nbt, false);
    }

    public static String toFormattedString(NbtElement nbt, boolean withArrayContents) {
        return NbtHelper.appendFormattedString(new StringBuilder(), nbt, 0, withArrayContents).toString();
    }

    public static StringBuilder appendFormattedString(StringBuilder stringBuilder, NbtElement nbt, int depth, boolean withArrayContents) {
        NbtElement nbtElement = nbt;
        Objects.requireNonNull(nbtElement);
        NbtElement nbtElement2 = nbtElement;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtPrimitive.class, NbtEnd.class, NbtByteArray.class, NbtList.class, NbtIntArray.class, NbtCompound.class, NbtLongArray.class}, (Object)nbtElement2, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                NbtPrimitive lv = (NbtPrimitive)nbtElement2;
                yield stringBuilder.append(lv);
            }
            case 1 -> {
                NbtEnd lv2 = (NbtEnd)nbtElement2;
                yield stringBuilder;
            }
            case 2 -> {
                NbtByteArray lv3 = (NbtByteArray)nbtElement2;
                byte[] bs = lv3.getByteArray();
                int j = bs.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("byte[").append(j).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (int k = 0; k < bs.length; ++k) {
                        if (k != 0) {
                            stringBuilder.append(',');
                        }
                        if (k % 16 == 0 && k / 16 > 0) {
                            stringBuilder.append('\n');
                            if (k < bs.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (k != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%02X", bs[k] & 0xFF));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 3 -> {
                NbtList lv4 = (NbtList)nbtElement2;
                int j = lv4.size();
                NbtHelper.appendIndent(depth, stringBuilder).append("list").append("[").append(j).append("] [");
                if (j != 0) {
                    stringBuilder.append('\n');
                }
                for (int k = 0; k < j; ++k) {
                    if (k != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    NbtHelper.appendFormattedString(stringBuilder, lv4.get(k), depth + 1, withArrayContents);
                }
                if (j != 0) {
                    stringBuilder.append('\n');
                }
                NbtHelper.appendIndent(depth, stringBuilder).append(']');
                yield stringBuilder;
            }
            case 4 -> {
                NbtIntArray lv5 = (NbtIntArray)nbtElement2;
                int[] is = lv5.getIntArray();
                int l = 0;
                for (int m : is) {
                    l = Math.max(l, String.format(Locale.ROOT, "%X", m).length());
                }
                int n = is.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("int[").append(n).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (int o = 0; o < is.length; ++o) {
                        if (o != 0) {
                            stringBuilder.append(',');
                        }
                        if (o % 16 == 0 && o / 16 > 0) {
                            stringBuilder.append('\n');
                            if (o < is.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (o != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + l + "X", is[o]));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 5 -> {
                NbtCompound lv6 = (NbtCompound)nbtElement2;
                ArrayList<String> list = Lists.newArrayList(lv6.getKeys());
                Collections.sort(list);
                NbtHelper.appendIndent(depth, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (depth + 1)) {
                    stringBuilder.append('\n');
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                }
                int n = list.stream().mapToInt(String::length).max().orElse(0);
                String string = Strings.repeat(" ", n);
                for (int p = 0; p < list.size(); ++p) {
                    if (p != 0) {
                        stringBuilder.append(",\n");
                    }
                    String string2 = (String)list.get(p);
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append('\"').append(string2).append('\"').append(string, 0, string.length() - string2.length()).append(": ");
                    NbtHelper.appendFormattedString(stringBuilder, lv6.get(string2), depth + 1, withArrayContents);
                }
                if (!list.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 6 -> {
                NbtLongArray lv7 = (NbtLongArray)nbtElement2;
                long[] ls = lv7.getLongArray();
                long q = 0L;
                for (long r : ls) {
                    q = Math.max(q, (long)String.format(Locale.ROOT, "%X", r).length());
                }
                long s = ls.length;
                NbtHelper.appendIndent(depth, stringBuilder).append("long[").append(s).append("] {\n");
                if (withArrayContents) {
                    NbtHelper.appendIndent(depth + 1, stringBuilder);
                    for (int t = 0; t < ls.length; ++t) {
                        if (t != 0) {
                            stringBuilder.append(',');
                        }
                        if (t % 16 == 0 && t / 16 > 0) {
                            stringBuilder.append('\n');
                            if (t < ls.length) {
                                NbtHelper.appendIndent(depth + 1, stringBuilder);
                            }
                        } else if (t != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + q + "X", ls[t]));
                    }
                } else {
                    NbtHelper.appendIndent(depth + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtHelper.appendIndent(depth, stringBuilder).append('}');
                yield stringBuilder;
            }
        };
    }

    private static StringBuilder appendIndent(int depth, StringBuilder stringBuilder) {
        int j = stringBuilder.lastIndexOf("\n") + 1;
        int k = stringBuilder.length() - j;
        for (int l = 0; l < 2 * depth - k; ++l) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static Text toPrettyPrintedText(NbtElement element) {
        return new NbtTextFormatter("").apply(element);
    }

    public static String toNbtProviderString(NbtCompound compound) {
        return new NbtOrderedStringFormatter().apply(NbtHelper.toNbtProviderFormat(compound));
    }

    public static NbtCompound fromNbtProviderString(String string) throws CommandSyntaxException {
        return NbtHelper.fromNbtProviderFormat(StringNbtReader.readCompound(string));
    }

    @VisibleForTesting
    static NbtCompound toNbtProviderFormat(NbtCompound compound) {
        NbtList lv4;
        Optional<NbtList> optional2;
        Optional<NbtList> optional = compound.getList("palettes");
        NbtList lv = optional.isPresent() ? optional.get().getListOrEmpty(0) : compound.getListOrEmpty("palette");
        NbtList lv2 = lv.streamCompounds().map(NbtHelper::toNbtProviderFormattedPalette).map(NbtString::of).collect(Collectors.toCollection(NbtList::new));
        compound.put("palette", lv2);
        if (optional.isPresent()) {
            NbtList lv3 = new NbtList();
            optional.get().stream().flatMap(nbt -> nbt.asNbtList().stream()).forEach(nbt -> {
                NbtCompound lv = new NbtCompound();
                for (int i = 0; i < nbt.size(); ++i) {
                    lv.putString(lv2.getString(i).orElseThrow(), NbtHelper.toNbtProviderFormattedPalette(nbt.getCompound(i).orElseThrow()));
                }
                lv3.add(lv);
            });
            compound.put("palettes", lv3);
        }
        if ((optional2 = compound.getList("entities")).isPresent()) {
            lv4 = optional2.get().streamCompounds().sorted(Comparator.comparing(nbt -> nbt.getList("pos"), Comparators.emptiesLast(ENTITY_POS_COMPARATOR))).collect(Collectors.toCollection(NbtList::new));
            compound.put("entities", lv4);
        }
        lv4 = compound.getList("blocks").stream().flatMap(NbtList::streamCompounds).sorted(Comparator.comparing(nbt -> nbt.getList("pos"), Comparators.emptiesLast(BLOCK_POS_COMPARATOR))).peek(nbt -> nbt.putString("state", lv2.getString(nbt.getInt("state", 0)).orElseThrow())).collect(Collectors.toCollection(NbtList::new));
        compound.put(DATA_KEY, lv4);
        compound.remove("blocks");
        return compound;
    }

    @VisibleForTesting
    static NbtCompound fromNbtProviderFormat(NbtCompound compound) {
        NbtList lv = compound.getListOrEmpty("palette");
        Map map = lv.stream().flatMap(nbt -> nbt.asString().stream()).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtHelper::fromNbtProviderFormattedPalette));
        Optional<NbtList> optional = compound.getList("palettes");
        if (optional.isPresent()) {
            compound.put("palettes", optional.get().streamCompounds().map(nbt -> map.keySet().stream().map(key -> nbt.getString((String)key).orElseThrow()).map(NbtHelper::fromNbtProviderFormattedPalette).collect(Collectors.toCollection(NbtList::new))).collect(Collectors.toCollection(NbtList::new)));
            compound.remove("palette");
        } else {
            compound.put("palette", map.values().stream().collect(Collectors.toCollection(NbtList::new)));
        }
        Optional<NbtList> optional2 = compound.getList(DATA_KEY);
        if (optional2.isPresent()) {
            Object2IntOpenHashMap<String> object2IntMap = new Object2IntOpenHashMap<String>();
            object2IntMap.defaultReturnValue(-1);
            for (int i = 0; i < lv.size(); ++i) {
                object2IntMap.put(lv.getString(i).orElseThrow(), i);
            }
            NbtList lv2 = optional2.get();
            for (int j = 0; j < lv2.size(); ++j) {
                NbtCompound lv3 = lv2.getCompound(j).orElseThrow();
                String string = lv3.getString("state").orElseThrow();
                int k = object2IntMap.getInt(string);
                if (k == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                lv3.putInt("state", k);
            }
            compound.put("blocks", lv2);
            compound.remove(DATA_KEY);
        }
        return compound;
    }

    @VisibleForTesting
    static String toNbtProviderFormattedPalette(NbtCompound compound) {
        StringBuilder stringBuilder = new StringBuilder(compound.getString("Name").orElseThrow());
        compound.getCompound("Properties").ifPresent(properties -> {
            String string = properties.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> (String)entry.getKey() + ":" + ((NbtElement)entry.getValue()).asString().orElseThrow()).collect(Collectors.joining(COMMA));
            stringBuilder.append('{').append(string).append('}');
        });
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static NbtCompound fromNbtProviderFormattedPalette(String string) {
        String string2;
        NbtCompound lv = new NbtCompound();
        int i = string.indexOf(123);
        if (i >= 0) {
            string2 = string.substring(0, i);
            NbtCompound lv2 = new NbtCompound();
            if (i + 2 <= string.length()) {
                String string3 = string.substring(i + 1, string.indexOf(125, i));
                COMMA_SPLITTER.split(string3).forEach(property -> {
                    List<String> list = COLON_SPLITTER.splitToList((CharSequence)property);
                    if (list.size() == 2) {
                        lv2.putString(list.get(0), list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                lv.put("Properties", lv2);
            }
        } else {
            string2 = string;
        }
        lv.putString("Name", string2);
        return lv;
    }

    public static NbtCompound putDataVersion(NbtCompound nbt) {
        int i = SharedConstants.getGameVersion().dataVersion().id();
        return NbtHelper.putDataVersion(nbt, i);
    }

    public static NbtCompound putDataVersion(NbtCompound nbt, int dataVersion) {
        nbt.putInt("DataVersion", dataVersion);
        return nbt;
    }

    public static Dynamic<NbtElement> putDataVersion(Dynamic<NbtElement> dynamic) {
        int i = SharedConstants.getGameVersion().dataVersion().id();
        return NbtHelper.putDataVersion(dynamic, i);
    }

    public static Dynamic<NbtElement> putDataVersion(Dynamic<NbtElement> dynamic, int dataVersion) {
        return dynamic.set("DataVersion", dynamic.createInt(dataVersion));
    }

    public static void writeDataVersion(WriteView view) {
        int i = SharedConstants.getGameVersion().dataVersion().id();
        NbtHelper.writeDataVersion(view, i);
    }

    public static void writeDataVersion(WriteView view, int dataVersion) {
        view.putInt("DataVersion", dataVersion);
    }

    public static int getDataVersion(NbtCompound nbt, int fallback) {
        return nbt.getInt("DataVersion", fallback);
    }

    public static int getDataVersion(Dynamic<?> dynamic, int fallback) {
        return dynamic.get("DataVersion").asInt(fallback);
    }
}

