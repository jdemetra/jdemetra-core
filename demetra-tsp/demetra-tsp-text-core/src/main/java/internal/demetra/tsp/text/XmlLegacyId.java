package internal.demetra.tsp.text;

import demetra.design.DemetraPlusLegacy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author Demortier Jeremy
 */
@DemetraPlusLegacy
@lombok.Value
public class XmlLegacyId {

    private static final String SEP = "@";
    private static final int NO_INDEX = -1;

    @lombok.NonNull
    private String file;

    private int collectionIndex;

    private int seriesIndex;

    public static @NonNull XmlLegacyId collection(@NonNull String sfile, int pos) {
        return new XmlLegacyId(sfile, pos, NO_INDEX);
    }

    public static @NonNull XmlLegacyId series(@NonNull String sfile, int cpos, int spos) {
        return new XmlLegacyId(sfile, cpos, spos);
    }

    public static @Nullable XmlLegacyId parse(@NonNull String monikerId) {
        String[] parts = monikerId.split(SEP);

        if (parts.length > 3) {
            return null;
        }

        try {
            // No break on purpose : a moniker with x parts has indeed all parts from 0 to x-1
            switch (parts.length) {
                case 3:
                    return series(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                case 2:
                    return collection(parts[0], Integer.parseInt(parts[1]));
                case 1:
                    return new XmlLegacyId(parts[0], NO_INDEX, NO_INDEX);
            }
        } catch (NumberFormatException err) {
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(file).append(SEP).append(collectionIndex);
        if (isSeries()) {
            builder.append(SEP).append(seriesIndex);
        }
        return builder.toString();
    }

    public boolean isCollection() {
        return NO_INDEX == seriesIndex;
    }

    public boolean isSeries() {
        return seriesIndex >= 0;
    }
}
