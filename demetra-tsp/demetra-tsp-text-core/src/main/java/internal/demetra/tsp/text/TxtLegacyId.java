package internal.demetra.tsp.text;

import demetra.design.DemetraPlusLegacy;
import nbbrd.io.text.Parser;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Demortier Jeremy
 */
@DemetraPlusLegacy
@lombok.Value
public class TxtLegacyId {

    private static final String SEP = "@";

    @lombok.NonNull
    private String file;

    private int seriesIndex;

    public static @NonNull TxtLegacyId collection(@NonNull String fileName) {
        return new TxtLegacyId(fileName, -1);
    }

    public static @NonNull TxtLegacyId series(@NonNull String fileName, int indexSeries) {
        return new TxtLegacyId(fileName, indexSeries);
    }

    public static @Nullable TxtLegacyId parse(@NonNull String monikerId) {
        String[] parts = monikerId.split(SEP, -1);

        try {
            switch (parts.length) {
                case 2:
                    return Parser.onInteger()
                            .parseValue(parts[1])
                            .map(index -> series(parts[0], index))
                            .orElse(null);
                case 1:
                    return collection(parts[0]);
                default:
                    return null;
            }
        } catch (NumberFormatException err) {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(file);
        if (isSeries()) {
            builder.append(SEP).append(seriesIndex);
        }
        return builder.toString();
    }

    public boolean isSeries() {
        return seriesIndex >= 0;
    }
}
