package demetra.timeseries;

import demetra.data.HasEmptyCause;
import demetra.data.Seq;
import demetra.util.List2;
import lombok.AccessLevel;
import nbbrd.design.StaticFactoryMethod;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@lombok.Value
@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class TsSeq implements Seq<Ts>, HasEmptyCause {

    private static final String NO_DATA_CAUSE = "No data available";

    public static TsSeq EMPTY = new TsSeq(Collections.emptyList(), NO_DATA_CAUSE);

    @StaticFactoryMethod
    public static @NonNull TsSeq of(@NonNull Ts... seq) {
        return seq.length == 0 ? EMPTY : new TsSeq(List2.of(seq), null);
    }

    @StaticFactoryMethod
    public static @NonNull TsSeq of(@NonNull List<Ts> seq) {
        return seq.isEmpty() ? EMPTY : new TsSeq(List2.copyOf(seq), null);
    }

    @StaticFactoryMethod
    public static @NonNull TsSeq ofInternal(@NonNull List<Ts> seq) {
        return seq.isEmpty() ? EMPTY : new TsSeq(seq, null);
    }

    @StaticFactoryMethod
    public static @NonNull TsSeq empty(@NonNull String cause) {
        Objects.requireNonNull(cause);
        return new TsSeq(Collections.emptyList(), cause);
    }

    @lombok.NonNull
    private final List<Ts> items;

    @lombok.Getter
    private final String emptyCause;

    @Override
    public @NonNegative int length() {
        return items.size();
    }

    @Override
    public Ts get(@NonNegative int index) throws IndexOutOfBoundsException {
        return items.get(index);
    }

    public static @NonNull Collector<Ts, ?, TsSeq> toTsSeq() {
        return Collectors.collectingAndThen(Collectors.toList(), TsSeq::ofInternal);
    }
}
